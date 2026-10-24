const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated read of incidents fails", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("incidents").get());
});

test("Authenticated citizen can create a valid incident", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const incidentDoc = aliceDb.collection("incidents").doc("inc_001");
  await assertSucceeds(
    incidentDoc.set({
      id: "inc_001",
      incidentNumber: "SAR-2026-0001",
      reporterUserId: ALICE_UID,
      reporterName: "Alice Reporter",
      reporterContact: "alice@example.com",
      animalType: "Dog",
      conditionDescription: "Injured stray near market",
      urgency: "HIGH",
      status: "SUBMITTED",
      locationAddress: "123 Main St",
      latitude: 12.9716,
      longitude: 77.5946,
    })
  );
});

test("Invalid incident schema fails validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const incidentDoc = aliceDb.collection("incidents").doc("inc_bad");
  // Missing animalType and urgency
  await assertFails(
    incidentDoc.set({
      id: "inc_bad",
      incidentNumber: "SAR-BAD",
      reporterUserId: ALICE_UID,
      status: "SUBMITTED",
    })
  );
});

test("Citizen cannot update other user's incident", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("incidents").doc("inc_alice").set({
      id: "inc_alice",
      incidentNumber: "SAR-2026-0002",
      reporterUserId: ALICE_UID,
      reporterName: "Alice",
      reporterContact: "alice@example.com",
      animalType: "Cat",
      urgency: "MEDIUM",
      status: "SUBMITTED",
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(
    bobDb.collection("incidents").doc("inc_alice").update({
      status: "RESCUED",
    })
  );
});

test("Applicant can submit pending rescueRegistrationRequest and citizen cannot approve it", async () => {
  const applicantDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const requestDoc = applicantDb.collection("rescueRegistrationRequests").doc("REQ-1001");

  // Applicant creates pending request
  await assertSucceeds(
    requestDoc.set({
      requestId: "REQ-1001",
      uid: ALICE_UID,
      fullName: "Alice Medic",
      email: "alice@example.com",
      phoneNumber: "555-123-4567",
      status: "pending",
    })
  );

  // Bob (regular citizen) tries to approve Alice's request (should fail)
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(
    bobDb.collection("rescueRegistrationRequests").doc("REQ-1001").update({
      status: "approved",
    })
  );

  // Admin approves Alice's request (should succeed)
  const adminDb = testEnv.authenticatedContext("admin_uid", { role: "ORGANIZATION_ADMIN" }).firestore();
  await assertSucceeds(
    adminDb.collection("rescueRegistrationRequests").doc("REQ-1001").update({
      status: "approved",
      reviewedBy: "Admin HQ",
    })
  );
});

test("Pending applicant cannot access protected rescue_squads or self-approve status", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice Applicant",
      email: "alice@example.com",
      role: "CITIZEN",
      status: "PENDING_APPROVAL",
    });
    await context.firestore().collection("rescue_squads").doc("squad_1").set({
      id: "squad_1",
      name: "Unit Beta-3",
    });
  });

  const applicantDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();

  // Pending applicant CANNOT read rescue squads
  await assertFails(applicantDb.collection("rescue_squads").doc("squad_1").get());

  // Pending applicant CANNOT self-promote status to ACTIVE
  await assertFails(
    applicantDb.collection("users").doc(ALICE_UID).update({
      status: "ACTIVE",
    })
  );

  // Pending applicant CANNOT self-promote role to RESCUE_TEAM
  await assertFails(
    applicantDb.collection("users").doc(ALICE_UID).update({
      role: "RESCUE_TEAM",
    })
  );
});

test("Admin with configured email can approve request and promote user to RESCUE_TEAM", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice Applicant",
      email: "alice@example.com",
      role: "CITIZEN",
      status: "PENDING_APPROVAL",
    });
    await context.firestore().collection("rescueRegistrationRequests").doc("REQ-1002").set({
      requestId: "REQ-1002",
      uid: ALICE_UID,
      fullName: "Alice Applicant",
      email: "alice@example.com",
      phoneNumber: "555-123-4567",
      status: "pending",
    });
    await context.firestore().collection("rescue_squads").doc("squad_1").set({
      id: "squad_1",
      name: "Unit Beta-3",
    });
  });

  // Admin authenticates via email
  const adminDb = testEnv.authenticatedContext("admin_email_uid", { email: "jayasuryageetha@gmail.com" }).firestore();

  // Admin approves the registration request
  await assertSucceeds(
    adminDb.collection("rescueRegistrationRequests").doc("REQ-1002").update({
      status: "approved",
      reviewedBy: "Jayasurya (Org Administrator)",
    })
  );

  // Admin updates user doc to RESCUE_TEAM and APPROVED
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).set({
      id: ALICE_UID,
      name: "Alice Applicant",
      email: "alice@example.com",
      role: "RESCUE_TEAM",
      status: "APPROVED",
    }, { merge: true })
  );

  // Now Alice (as approved rescue team with role and status in users document) can access rescue_squads
  const approvedAliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await assertSucceeds(approvedAliceDb.collection("rescue_squads").doc("squad_1").get());
});
