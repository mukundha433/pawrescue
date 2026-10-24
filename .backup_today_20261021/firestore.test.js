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

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("incidents").doc("inc_alice").update({
      status: "RESCUED",
    })
  );
});
