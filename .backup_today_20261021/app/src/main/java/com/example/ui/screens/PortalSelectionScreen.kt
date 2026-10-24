package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.IncidentRepository
import com.example.model.AuthResult
import com.example.model.ImageHotlinks
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondarySlate
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TertiaryBlue

enum class PortalView {
    PORTAL_CHOOSER,
    CITIZEN_LOGIN,
    CITIZEN_SIGNUP,
    RESCUE_TEAM_LOGIN,
    ORG_ADMIN_LOGIN
}

@Composable
fun PortalSelectionScreen(
    onLoginSuccess: (UserSession) -> Unit,
    onSosClick: () -> Unit
) {
    var currentView by remember { mutableStateOf(PortalView.PORTAL_CHOOSER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Intercept back button when inside a login/signup screen to return to Portal Chooser
    BackHandler(enabled = currentView != PortalView.PORTAL_CHOOSER) {
        errorMessage = null
        infoMessage = null
        currentView = PortalView.PORTAL_CHOOSER
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentView) {
            PortalView.PORTAL_CHOOSER -> {
                PortalChooserScreen(
                    onSelectPortal = { portal ->
                        errorMessage = null
                        infoMessage = null
                        currentView = when (portal) {
                            UserRole.CITIZEN -> PortalView.CITIZEN_LOGIN
                            UserRole.RESCUE_TEAM -> PortalView.RESCUE_TEAM_LOGIN
                            UserRole.ORGANIZATION_ADMIN -> PortalView.ORG_ADMIN_LOGIN
                        }
                    },
                    onSosClick = onSosClick
                )
            }

            PortalView.CITIZEN_LOGIN -> {
                CitizenLoginView(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage,
                    onBack = {
                        errorMessage = null
                        currentView = PortalView.PORTAL_CHOOSER
                    },
                    onLogin = { identifier, password ->
                        val result = IncidentRepository.authenticate(UserRole.CITIZEN, identifier, password)
                        when (result) {
                            is AuthResult.Success -> {
                                errorMessage = null
                                onLoginSuccess(result.session)
                            }
                            is AuthResult.WrongPortalError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.AccountInactiveError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.InvalidCredentialsError -> {
                                errorMessage = result.message
                            }
                        }
                    },
                    onCreateAccount = {
                        errorMessage = null
                        infoMessage = null
                        currentView = PortalView.CITIZEN_SIGNUP
                    },
                    onForgotPassword = {
                        infoMessage = "Password reset instructions have been dispatched to your email/mobile."
                    }
                )
            }

            PortalView.CITIZEN_SIGNUP -> {
                CitizenSignupView(
                    errorMessage = errorMessage,
                    onBack = {
                        errorMessage = null
                        currentView = PortalView.CITIZEN_LOGIN
                    },
                    onSignUp = { name, contact, password ->
                        val result = IncidentRepository.signUpCitizen(name, contact, password)
                        when (result) {
                            is AuthResult.Success -> {
                                errorMessage = null
                                onLoginSuccess(result.session)
                            }
                            is AuthResult.InvalidCredentialsError -> {
                                errorMessage = result.message
                            }
                            else -> {
                                errorMessage = "Unable to register account at this time."
                            }
                        }
                    }
                )
            }

            PortalView.RESCUE_TEAM_LOGIN -> {
                RescueTeamLoginView(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage,
                    onBack = {
                        errorMessage = null
                        currentView = PortalView.PORTAL_CHOOSER
                    },
                    onLogin = { identifier, password ->
                        val result = IncidentRepository.authenticate(UserRole.RESCUE_TEAM, identifier, password)
                        when (result) {
                            is AuthResult.Success -> {
                                errorMessage = null
                                onLoginSuccess(result.session)
                            }
                            is AuthResult.WrongPortalError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.AccountInactiveError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.InvalidCredentialsError -> {
                                errorMessage = result.message
                            }
                        }
                    },
                    onForgotPassword = {
                        infoMessage = "Password reset for field members must be approved by Organization Dispatch HQ."
                    }
                )
            }

            PortalView.ORG_ADMIN_LOGIN -> {
                OrganizationAdminLoginView(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage,
                    onBack = {
                        errorMessage = null
                        currentView = PortalView.PORTAL_CHOOSER
                    },
                    onLogin = { identifier, password ->
                        val result = IncidentRepository.authenticate(UserRole.ORGANIZATION_ADMIN, identifier, password)
                        when (result) {
                            is AuthResult.Success -> {
                                errorMessage = null
                                onLoginSuccess(result.session)
                            }
                            is AuthResult.WrongPortalError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.AccountInactiveError -> {
                                errorMessage = result.message
                            }
                            is AuthResult.InvalidCredentialsError -> {
                                errorMessage = result.message
                            }
                        }
                    },
                    onForgotPassword = {
                        infoMessage = "Master security passkeys can only be reset via Hardware Security Module (HSM)."
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. FIRST APP OPEN: Clean professional screen
// Title: "Welcome to PawRescue"
// Subtitle: "Choose your portal to continue"
// Exactly 3 portal cards:
// - Citizen Portal ("Report animals and track rescue requests.") [Citizen Login]
// - Rescue Team Portal ("Manage assigned rescue missions.") [Rescue Team Login]
// - Organization Admin Portal ("Manage reports, teams and rescue operations.") [Organization Login]
// ---------------------------------------------------------------------------

@Composable
private fun PortalChooserScreen(
    onSelectPortal: (UserRole) -> Unit,
    onSosClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // App Emblem
        item {
            Image(
                painter = painterResource(id = R.drawable.pawrescue_official_logo),
                contentDescription = "PawRescue Official Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        // Title and Subtitle
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Welcome to PawRescue",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Choose your portal to continue",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Card 1: Citizen Portal
        item {
            PortalChooserCard(
                title = "Citizen Portal",
                description = "Report animals and track rescue requests.",
                badgeText = "PUBLIC ACCESS",
                buttonText = "Citizen Login",
                buttonColor = PrimaryEmerald,
                onButtonClick = { onSelectPortal(UserRole.CITIZEN) },
                testTag = "portal_citizen"
            )
        }

        // Card 2: Rescue Team Portal
        item {
            PortalChooserCard(
                title = "Rescue Team Portal",
                description = "Manage assigned rescue missions.",
                badgeText = "FIELD SQUADS",
                buttonText = "Rescue Team Login",
                buttonColor = Color(0xFF0F766E), // Deep Teal
                onButtonClick = { onSelectPortal(UserRole.RESCUE_TEAM) },
                testTag = "portal_rescue_team"
            )
        }

        // Card 3: Organization Admin Portal
        item {
            PortalChooserCard(
                title = "Organization Admin Portal",
                description = "Manage reports, teams and rescue operations.",
                badgeText = "RESTRICTED HQ",
                buttonText = "Organization Login",
                buttonColor = TertiaryBlue,
                onButtonClick = { onSelectPortal(UserRole.ORGANIZATION_ADMIN) },
                testTag = "portal_org_admin"
            )
        }

        // Direct SOS emergency hotline
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSosClick)
                    .testTag("sos_direct_link")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ErrorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Immediate Life Threat Distress?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                        Text("Tap to trigger 24/7 Emergency Siren & hotline", fontSize = 11.sp, color = SecondarySlate)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun PortalChooserCard(
    title: String,
    description: String,
    badgeText: String,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SurfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondarySlate,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("${testTag}_login_btn")
            ) {
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. CITIZEN LOGIN SCREEN
// Fields: Email / Mobile, Password, Remember Me, Forgot Password, Login, Create Citizen Account
// ---------------------------------------------------------------------------

@Composable
private fun CitizenLoginView(
    errorMessage: String?,
    infoMessage: String?,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
    ) {
        // Decorative background elements: soft mint curves and paw print at bottom
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle mint wave at the bottom
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.45f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.2f,
                        size.width * 0.7f, size.height * 0.75f,
                        size.width, size.height * 0.35f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color(0xFFE8F6ED))
            }

            // Decorative soft mint paw print at bottom left
            Image(
                painter = painterResource(id = R.drawable.ic_paw_print),
                contentDescription = null,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFD6F2E2)),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            )
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // 1. TOP HEADER: ← Citizen Portal (minimal, clean)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("citizen_login_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Citizen Portal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. BRAND / HERO SECTION (Left text + Right pet visual overlapping)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp)
            ) {
                // Right: Hero animal visual (golden retriever & cat with mint background blob)
                Image(
                    painter = painterResource(id = R.drawable.ic_hero_pets),
                    contentDescription = "PawRescue animals",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(width = 175.dp, height = 150.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = (-6).dp)
                )

                // Left: PawRescue Brand & Citizen Login heading
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .padding(top = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pawrescue_official_logo),
                            contentDescription = "PawRescue Official Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = "PAWRESCUE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp,
                            color = Color(0xFF0F766E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Citizen Login",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign in to report distressed animals and track rescue operations in real time.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error / Info banners if present
            if (errorMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SecurityAlertBanner(message = errorMessage)
                }
            }
            if (infoMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    InfoBanner(message = infoMessage)
                }
            }

            // 3. LOGIN CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email / Mobile Number
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Email / Mobile Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            placeholder = {
                                Text(
                                    "Enter your email or mobile number",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("citizen_input_identifier")
                        )
                    }

                    // Password
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    "Enter your password",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("citizen_input_password")
                        )
                    }

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF00684A),
                                    uncheckedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("citizen_remember_me")
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Remember me",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00684A),
                            modifier = Modifier
                                .clickable(onClick = onForgotPassword)
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Primary Login Button
                    Button(
                        onClick = { onLogin(identifier, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("citizen_submit_login")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Login to Citizen Portal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "OR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // Secondary: Create Citizen Account Button
                    OutlinedButton(
                        onClick = onCreateAccount,
                        border = BorderStroke(1.5.dp, Color(0xFF00684A)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00684A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("citizen_create_account_btn")
                    ) {
                        Text(
                            text = "Create a New Citizen Account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00684A)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CITIZEN SIGNUP VIEW
// ---------------------------------------------------------------------------

@Composable
private fun CitizenSignupView(
    errorMessage: String?,
    onBack: () -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Back to Citizen Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pawrescue_official_logo),
                        contentDescription = "PawRescue Official Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Text(
                        text = "PAWRESCUE CITIZEN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = Color(0xFF0F766E)
                    )
                }
                Text("Create Citizen Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Join our network of compassionate community reporters. Save lives in your neighborhood.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (errorMessage != null || localError != null) {
            item {
                SecurityAlertBanner(message = errorMessage ?: localError ?: "")
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Email or Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        localError = "Passwords do not match."
                    } else if (password.length < 4) {
                        localError = "Password must be at least 4 characters."
                    } else {
                        localError = null
                        onSignUp(name, contact, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Register & Access Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ---------------------------------------------------------------------------
// 5. RESCUE TEAM LOGIN SCREEN
// Fields: Team Member ID / Email, Password, Remember Me, Forgot Password, Login
// (No public signup - approved by Org Admin)
// ---------------------------------------------------------------------------

@Composable
private fun RescueTeamLoginView(
    errorMessage: String?,
    infoMessage: String?,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
    ) {
        // Decorative background elements: soft mint curves and paw print at bottom
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle mint wave at the bottom
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.45f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.2f,
                        size.width * 0.7f, size.height * 0.75f,
                        size.width, size.height * 0.35f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color(0xFFE8F6ED))
            }

            // Decorative soft mint paw print at bottom left
            Image(
                painter = painterResource(id = R.drawable.ic_paw_print),
                contentDescription = null,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFD6F2E2)),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            )
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // 1. TOP HEADER: ← Rescue Team Portal (minimal, clean)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("rescue_team_login_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Rescue Team Portal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. BRAND / HERO SECTION (Left text + Right rescue vehicle visual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp)
            ) {
                // Right: Hero rescue vehicle visual (ambulance + map beacon)
                Image(
                    painter = painterResource(id = R.drawable.ic_hero_rescue),
                    contentDescription = "Rescue team vehicle",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(width = 175.dp, height = 150.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = (-6).dp)
                )

                // Left: PawRescue Brand & Rescue Team Login heading
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .padding(top = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pawrescue_official_logo),
                            contentDescription = "PawRescue Official Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = "PAWRESCUE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp,
                            color = Color(0xFF0F766E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rescue Team Login",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Field access for designated rescue teams and veterinary response units.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ACCESS INFORMATION PANEL
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Rescue Team accounts are created and activated by the Organization Admin.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error / Info banners if present
            if (errorMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SecurityAlertBanner(message = errorMessage)
                }
            }
            if (infoMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    InfoBanner(message = infoMessage)
                }
            }

            // 4. LOGIN CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Team Member ID / Email
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Team Member ID / Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            placeholder = {
                                Text(
                                    "Enter your team ID or email",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("rescue_input_identifier")
                        )
                    }

                    // Password
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    "Enter your password",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("rescue_input_password")
                        )
                    }

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF00684A),
                                    uncheckedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("rescue_remember_me")
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Remember me",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00684A),
                            modifier = Modifier
                                .clickable(onClick = onForgotPassword)
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Primary Login Button
                    Button(
                        onClick = { onLogin(identifier, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("rescue_submit_login")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Login to Rescue Team Dashboard",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 7. ORGANIZATION ADMIN LOGIN SCREEN
// Fields: Organization Email / Admin ID, Password, Remember Me, Forgot Password, Login
// (No public signup - admin accounts created securely)
// ---------------------------------------------------------------------------

@Composable
private fun OrganizationAdminLoginView(
    errorMessage: String?,
    infoMessage: String?,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
    ) {
        // Decorative background elements: soft mint curves and paw print at bottom
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle mint wave at the bottom
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.45f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.2f,
                        size.width * 0.7f, size.height * 0.75f,
                        size.width, size.height * 0.35f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color(0xFFE8F6ED))
            }

            // Decorative soft mint paw print at bottom left
            Image(
                painter = painterResource(id = R.drawable.ic_paw_print),
                contentDescription = null,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFD6F2E2)),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            )
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // 1. TOP HEADER: ← Organization Admin Portal (minimal, clean)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("admin_login_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Organization Admin Portal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. BRAND / HERO SECTION (Left text + Right HQ building visual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp)
            ) {
                // Right: Hero admin HQ visual
                Image(
                    painter = painterResource(id = R.drawable.ic_hero_admin_hq),
                    contentDescription = "Command headquarters",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(width = 175.dp, height = 150.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = (-6).dp)
                )

                // Left: PawRescue Brand & Org Admin Login heading
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .padding(top = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pawrescue_official_logo),
                            contentDescription = "PawRescue Official Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = "PAWRESCUE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp,
                            color = Color(0xFF0F766E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Organization Admin Login",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Command headquarters authentication for dispatch triage, fleet squads, and analytics.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ACCESS INFORMATION PANEL
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Organization Admin accounts are cryptographically provisioned and managed by your system administrator.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error / Info banners if present
            if (errorMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SecurityAlertBanner(message = errorMessage)
                }
            }
            if (infoMessage != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    InfoBanner(message = infoMessage)
                }
            }

            // 4. LOGIN CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Organization Email / Admin ID
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Organization Email / Admin ID",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            placeholder = {
                                Text(
                                    "Enter your organization email or admin ID",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CorporateFare,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("admin_input_identifier")
                        )
                    }

                    // Password
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    "Enter your password",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00684A),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                cursorColor = Color(0xFF00684A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("admin_input_password")
                        )
                    }

                    // 2FA Security Guard Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF00684A),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "2FA Command Security Guard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "ENFORCED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00684A)
                        )
                    }

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF00684A),
                                    uncheckedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("admin_remember_me")
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Remember me",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00684A),
                            modifier = Modifier
                                .clickable(onClick = onForgotPassword)
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Primary Login Button
                    Button(
                        onClick = { onLogin(identifier, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("admin_submit_login")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Login to Organization Admin Dashboard",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Common Helper UI Components
// ---------------------------------------------------------------------------

@Composable
private fun SecurityAlertBanner(message: String) {
    Surface(
        color = ErrorContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_error_banner")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = ErrorRed, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Security & Role Guard", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ErrorRed)
                Text(message, fontSize = 12.sp, color = OnErrorContainer, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun InfoBanner(message: String) {
    Surface(
        color = SurfaceContainerHigh,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}
