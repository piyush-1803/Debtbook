package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.DbEntity
import com.example.data.DbTransaction
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtBookApp(viewModel: MainViewModel) {
    val isSignedUp by viewModel.isSignedUp.collectAsState()

    // Screen navigation state
    var currentTab by remember { mutableStateOf("dashboard") } // "dashboard", "transactions", "entities", "settings"

    // FAB Dialog State
    var showQuickAdd by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = isSignedUp,
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            },
            label = "ScreenTransition"
        ) { signed ->
            if (!signed) {
                OnboardingScreen(viewModel = viewModel, onComplete = { name, phone, avatar ->
                    viewModel.completeSignUp(name, phone, avatar)
                })
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "DebtBook",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            actions = {
                                val avatarUrl by viewModel.avatarUrl.collectAsState()
                                val name by viewModel.userName.collectAsState()
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .clickable { currentTab = "settings" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = name.firstOrNull()?.uppercase() ?: "U",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "dashboard",
                                onClick = { currentTab = "dashboard" },
                                icon = { Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentTab == "transactions",
                                onClick = { currentTab = "transactions" },
                                icon = { Icon(Icons.Outlined.SwapHoriz, contentDescription = "Transactions") },
                                label = { Text("Transactions") },
                                modifier = Modifier.testTag("nav_transactions")
                            )
                            NavigationBarItem(
                                selected = currentTab == "entities",
                                onClick = { currentTab = "entities" },
                                icon = { Icon(Icons.Outlined.Group, contentDescription = "People") },
                                label = { Text("People") },
                                modifier = Modifier.testTag("nav_entities")
                            )
                            NavigationBarItem(
                                selected = currentTab == "settings",
                                onClick = { currentTab = "settings" },
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab != "settings") {
                            FloatingActionButton(
                                onClick = {
                                    showQuickAdd = true
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .testTag("fab_quick_add")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Quick Add Transaction",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                            },
                            label = "MainTabs"
                        ) { tab ->
                            when (tab) {
                                "dashboard" -> DashboardTab(viewModel)
                                "transactions" -> TransactionsTab(viewModel)
                                "entities" -> EntitiesTab(viewModel)
                                "settings" -> SettingsTab(viewModel)
                            }
                        }

                        if (showQuickAdd) {
                            QuickAddDialog(
                                viewModel = viewModel,
                                onDismiss = { showQuickAdd = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: MainViewModel, onComplete: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Firebase auth inputs
    var isCloudMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authLoading by remember { mutableStateOf(false) }

    // Firebase config inputs (in case dynamic setup is requested)
    val firebaseApiKey by viewModel.firebaseApiKey.collectAsState()
    val firebaseProjectId by viewModel.firebaseProjectId.collectAsState()
    val firebaseAppId by viewModel.firebaseAppId.collectAsState()
    val firebaseStatus by viewModel.firebaseStatus.collectAsState()

    var configApiKey by remember { mutableStateOf(firebaseApiKey) }
    var configProjectId by remember { mutableStateOf(firebaseProjectId) }
    var configAppId by remember { mutableStateOf(firebaseAppId) }
    var showConfigForm by remember { mutableStateOf(false) }

    // Prepopulate profile avatars with the requested picture as the top choice!
    val avatarPresets = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCIKE6CqDsIbDpr2len6sq2UGJjVfSWk6cQKh5wJq36SrY6yicBX9ZLkUjNeVV7PoH1eh4KkSGnEOcxp68iAa2MWTJ9dZg4Gu0O0CGsaC7E4NCDXRUZCSigoR9FZM7-AWQkPP5OQUNqGgFAMkvzIpPA4yts612XMZoVTH-NFX1hacuwVQQTHH1OCb0rIfdD_tjbSiDoByaif2mB1W1vjZqnmGQtVpgsgk4S9UhZEXr7bbzWbaS2sGN91mvYSO-tgSxNrrpWtTEUTHr0",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80"
    )
    var selectedAvatar by remember { mutableStateOf(avatarPresets[0]) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val appPath = viewModel.copyUriToInternal(uri)
            if (appPath != null) {
                selectedAvatar = appPath
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large title
        Text(
            text = "DebtBook",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Zenith Personal Debt Management",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Custom Card Design
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let's Get Started!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Avatar display
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedAvatar,
                        contentDescription = "Avatar Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar choices list + Gallery option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Choose Profile Accent Photo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        avatarPresets.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedAvatar == url) 2.5.dp else 1.dp,
                                        color = if (selectedAvatar == url) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAvatar = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "preset",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Gallery Button choice
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery Input",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selection Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (!isCloudMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { isCloudMode = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Local Offline",
                            color = if (!isCloudMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isCloudMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { isCloudMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Firebase Sync",
                            color = if (isCloudMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isCloudMode) {
                    // LOCAL PROFILE FLOW
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("Enter your full name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("Optional contact phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Currency warning
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Configured to Indian Rupee (₹) securely",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onComplete(name.trim(), phone.trim(), selectedAvatar)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_button"),
                        enabled = name.trim().isNotEmpty(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Complete Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    // FIREBASE AUTH FLOW
                    if (firebaseStatus == "NOT_INITIALIZED" || firebaseStatus == "ERROR") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Firebase is Not Connected", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                Text(
                                    "Dynamic Firebase client can be initiated securely on your device for cloud backup. Toggle config below to enter your API credentials.",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(onClick = { showConfigForm = !showConfigForm }) {
                                    Text(if (showConfigForm) "Hide Client Config" else "Configure Connection Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        if (showConfigForm) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = configApiKey,
                                onValueChange = { configApiKey = it },
                                label = { Text("Web API Key", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = configProjectId,
                                onValueChange = { configProjectId = it },
                                label = { Text("Project ID", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = configAppId,
                                onValueChange = { configAppId = it },
                                label = { Text("App ID", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.saveFirebaseConfig(configApiKey, configProjectId, configAppId)
                                    showConfigForm = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save and Authenticate", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // CLIENT READY -> Show login/register form
                        Text(
                            "Sign In or Register with Firebase Sync Server",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; authError = null },
                            label = { Text("Firebase Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; authError = null },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        if (authError != null) {
                            Text(
                                text = authError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (authLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (email.contains("@") && password.length >= 6) {
                                            authLoading = true
                                            viewModel.firebaseSignIn(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    authLoading = false
                                                    onComplete(email.trim().substringBefore("@"), "", selectedAvatar)
                                                },
                                                onFailure = { err ->
                                                    authLoading = false
                                                    authError = err
                                                }
                                            )
                                        } else {
                                            authError = "Please enter valid email and password (min 6 chars)"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Log In")
                                }

                                Button(
                                    onClick = {
                                        if (email.contains("@") && password.length >= 6) {
                                            authLoading = true
                                            viewModel.firebaseSignUp(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    authLoading = false
                                                    onComplete(email.trim().substringBefore("@"), "", selectedAvatar)
                                                },
                                                onFailure = { err ->
                                                    authLoading = false
                                                    authError = err
                                                }
                                            )
                                        } else {
                                            authError = "Please enter valid email and password (min 6 chars)"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Register")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTab(viewModel: MainViewModel) {
    val totalReceivable by viewModel.totalReceivable.collectAsState()
    val totalPayable by viewModel.totalPayable.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val entities by viewModel.allEntities.collectAsState()
    val symbol by viewModel.currencySymbol.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Hello card
        val currentName by viewModel.userName.collectAsState()
        Text(
            text = "Namaste, $currentName",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Total Net Balance Dashboard Card - Styled with Clean Minimalism Navy Palette
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$symbol${String.format(Locale.getDefault(), "%,.2f", netBalance)}",
                    fontSize = 32.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lent (Receivables)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$symbol${String.format(Locale.getDefault(), "%,.2f", totalReceivable)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Borrowed (Payables)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$symbol${String.format(Locale.getDefault(), "%,.2f", totalPayable)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar matching the mockup
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search transactions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filters scroll row as specified in the mockup
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                val isFilterActive = showFilters
                FilterChip(
                    selected = isFilterActive,
                    onClick = { showFilters = !showFilters },
                    label = { Text("Filters") },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(99.dp)
                )
            }
            item {
                val currentType by viewModel.filterType.collectAsState()
                val isSelected = currentType != "ALL"
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = {
                        val next = when (currentType) {
                            "ALL" -> "RECEIVABLE"
                            "RECEIVABLE" -> "PAYABLE"
                            else -> "ALL"
                        }
                        viewModel.filterType.value = next
                    },
                    label = {
                        Text(
                            when (currentType) {
                                "RECEIVABLE" -> "Receivables"
                                "PAYABLE" -> "Payables"
                                else -> "All Types"
                            }
                        )
                    },
                    trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(99.dp)
                )
            }
            item {
                val currentEntityId by viewModel.filterEntityId.collectAsState()
                ElevatedFilterChip(
                    selected = currentEntityId != null,
                    onClick = {
                        if (currentEntityId != null) {
                            viewModel.filterEntityId.value = null
                        } else if (entities.isNotEmpty()) {
                            viewModel.filterEntityId.value = entities.first().id
                        }
                    },
                    label = {
                        val nameStr = if (currentEntityId != null) {
                            entities.find { it.id == currentEntityId }?.name ?: "Person"
                        } else {
                            "Entity"
                        }
                        Text(nameStr)
                    },
                    trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(99.dp)
                )
            }
        }

        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Entity / Person Filter:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            SuggestionChip(
                                onClick = { viewModel.filterEntityId.value = null },
                                label = { Text("All People") }
                            )
                        }
                        items(entities) { entity ->
                            SuggestionChip(
                                onClick = { viewModel.filterEntityId.value = entity.id },
                                label = { Text(entity.name) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fresh Empty Dashboard and List States matching the Clean Minimalism style
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Transactions Yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Your financial journey starts here. Make your first payment to see your history.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp)
                    )
                }
            }
        } else {
            // Group transactions by date
            val groupedTransactions = transactions.groupBy { tx ->
                val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                sdf.format(Date(tx.dateStamp))
            }

            groupedTransactions.forEach { (dateStr, txList) ->
                Text(
                    text = dateStr.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                txList.forEach { tx ->
                    val entity = entities.find { it.id == tx.entityId }
                    TransactionRow(
                        transaction = tx,
                        entity = entity,
                        currencySymbol = symbol,
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TransactionRow(
    transaction: DbTransaction,
    entity: DbEntity?,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category icon with responsive background accent color safely matched from style
                val (icon, color) = getCategoryIconAndColor(transaction.category)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = entity?.name?.uppercase() ?: "EXTERNAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Money and timestamp metadata
            Column(horizontalAlignment = Alignment.End) {
                val isReceivable = transaction.type == "RECEIVABLE"
                val amountText = (if (isReceivable) "+" else "-") + currencySymbol + String.format(Locale.getDefault(), "%,.2f", transaction.amount)
                Text(
                    text = amountText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isReceivable) Color(0xFF005A32) else Color(0xFF601410)
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.dateStamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("Transaction Details") },
            text = {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = "Description: ${transaction.description}", fontWeight = FontWeight.Medium)
                    Text(text = "Amount: $currencySymbol${transaction.amount}", fontWeight = FontWeight.Medium)
                    Text(text = "Type: ${transaction.type}", fontWeight = FontWeight.Medium)
                    Text(text = "Category: ${transaction.category}", fontWeight = FontWeight.Medium)
                    Text(text = "Associated with: ${entity?.name ?: "Unknown"}", fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete log")
                }
            }
        )
    }
}

@Composable
fun TransactionsTab(viewModel: MainViewModel) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val symbol by viewModel.currencySymbol.collectAsState()
    val rawEntities by viewModel.allEntities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("All Active Ledger Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("A chronological sequence of active credit loans & debits.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(14.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching transaction logs found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { tx ->
                    val entity = rawEntities.find { it.id == tx.entityId }
                    TransactionRow(
                        transaction = tx,
                        entity = entity,
                        currencySymbol = symbol,
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
fun EntitiesTab(viewModel: MainViewModel) {
    val entitiesWithBalances by viewModel.entitiesWithBalances.collectAsState()
    val symbol by viewModel.currencySymbol.collectAsState()

    var showAddPersonDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("People & Entities", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Manage personal debt balances of related individuals.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = { showAddPersonDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Person", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (entitiesWithBalances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Group, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No people added yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Add your first contact to map debt credits and debits.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entitiesWithBalances) { item ->
                    var showDetail by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDetail = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.entity.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(item.entity.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "${item.entity.relationship} • ${item.transactionCount} transactions",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Net Balance Indicator side
                            Column(horizontalAlignment = Alignment.End) {
                                val bal = item.netBalance
                                val balColor = when {
                                    bal > 0 -> Color(0xFF006C49) // they owe us (Receivable - Green)
                                    bal < 0 -> Color(0xFFBA1A1A) // we owe them (Payable - Red)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val textLabel = when {
                                    bal > 0 -> "Owes You"
                                    bal < 0 -> "You Owe"
                                    else -> "Settled Up"
                                }
                                val formattedBal = (if (bal > 0) "+" else "") + symbol + String.format(Locale.getDefault(), "%,.2f", bal)

                                Text(
                                    text = formattedBal,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = balColor,
                                    fontSize = 15.sp
                                )
                                Text(textLabel, fontSize = 10.sp, color = balColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (showDetail) {
                        EntityDetailDialog(
                            item = item,
                            viewModel = viewModel,
                            currencySymbol = symbol,
                            onDismiss = { showDetail = false }
                        )
                    }
                }
            }
        }

        if (showAddPersonDialog) {
            AddPersonDialog(
                onDismiss = { showAddPersonDialog = false },
                onAdd = { name, phone, rel ->
                    viewModel.addEntity(name, phone, rel)
                    showAddPersonDialog = false
                }
            )
        }
        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
fun EntityDetailDialog(
    item: EntityWithBalance,
    viewModel: MainViewModel,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    var settleAmount by remember { mutableStateOf("") }
    var settleType by remember { mutableStateOf("PAYBACK_TAKEN") } // PAYBACK_TAKEN or PAYBACK_GIVEN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.entity.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Relationship: ${item.entity.relationship}", fontSize = 13.sp)
                if (item.entity.phone.isNotEmpty()) {
                    Text("Contact: ${item.entity.phone}", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                Text("Record Quick Settling Entry:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { settleType = "PAYBACK_TAKEN" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settleType == "PAYBACK_TAKEN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (settleType == "PAYBACK_TAKEN") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Paid Me Back", fontSize = 11.sp, maxLines = 1)
                    }
                    Button(
                        onClick = { settleType = "PAYBACK_GIVEN" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settleType == "PAYBACK_GIVEN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (settleType == "PAYBACK_GIVEN") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I Settled Debt", fontSize = 11.sp, maxLines = 1)
                    }
                }

                OutlinedTextField(
                    value = settleAmount,
                    onValueChange = { settleAmount = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = settleAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        // "PAYBACK_TAKEN" means they settled some of what they owed us (so we got cash, which is a payment, so reducing receivable -> we add a payable tx or reverse receivable)
                        // If they paid us back, it reduces receivable: so standard type is PAYABLE (adds negative balance offset)
                        // If we settled what we owe them, it reduces payable: so standard type is RECEIVABLE (adds positive balance offset)
                        val type = if (settleType == "PAYBACK_TAKEN") "PAYABLE" else "RECEIVABLE"
                        val desc = if (settleType == "PAYBACK_TAKEN") "Debt Paid Back" else "Settle Payment"
                        viewModel.addTransaction(
                            amount = amt,
                            description = desc,
                            type = type,
                            category = "Other",
                            entityId = item.entity.id
                        )
                    }
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    viewModel.deleteEntity(item.entity.id)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Person")
            }
        }
    )
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Friend") }

    val options = listOf("Friend", "Client", "Supplier", "Family")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person / Entity") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Relationship Tag:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { opt ->
                        val isSel = relOf(relationship, opt)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { relationship = opt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onAdd(name.trim(), phone.trim(), relationship)
                    }
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun relOf(current: String, selection: String): Boolean = current == selection

@Composable
fun SettingsTab(viewModel: MainViewModel) {
    val name by viewModel.userName.collectAsState()
    val phone by viewModel.userPhone.collectAsState()
    val avatar by viewModel.avatarUrl.collectAsState()

    // Firebase live states
    val firebaseApiKey by viewModel.firebaseApiKey.collectAsState()
    val firebaseProjectId by viewModel.firebaseProjectId.collectAsState()
    val firebaseAppId by viewModel.firebaseAppId.collectAsState()
    val firebaseStatus by viewModel.firebaseStatus.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()

    var editApiKey by remember { mutableStateOf(firebaseApiKey) }
    var editProjectId by remember { mutableStateOf(firebaseProjectId) }
    var editAppId by remember { mutableStateOf(firebaseAppId) }
    var isFirebaseConfigExpanded by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.setCustomPfp(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your Profile & Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Manage local preferences, presets, and sync logs.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable Avatar with edit icon overlay
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { galleryLauncher.launch("image/*") }
                ) {
                    if (avatar.isNotEmpty()) {
                        AsyncImage(model = avatar, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(12.dp))
                    }
                    
                    // Edit Overlay Badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (phone.isNotEmpty()) {
                        Text(phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFF6CF8BB).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE PROFILE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00714D)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // FIREBASE CLOUD SYNC SETTINGS CARD
        Text("FIREBASE CLOUD SYNC", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (firebaseUser != null) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (firebaseUser != null) Color(0xFF00AD7C) else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Firebase Sync Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when (firebaseStatus) {
                                    "AUTHENTICATED" -> "Connected as ${firebaseUser?.email}"
                                    "INITIALIZED" -> "Firebase Engine Ready"
                                    "ERROR" -> "Initialization Error"
                                    else -> "Local Offline Mode"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (firebaseUser != null) {
                        TextButton(
                            onClick = { viewModel.firebaseSignOut() }
                        ) {
                            Text("Disconnect", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    } else {
                        IconButton(
                            onClick = { isFirebaseConfigExpanded = !isFirebaseConfigExpanded }
                        ) {
                            Icon(
                                imageVector = if (isFirebaseConfigExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle configuration settings"
                            )
                        }
                    }
                }

                if (isFirebaseConfigExpanded && firebaseUser == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initialize Custom credentials to setup real Firebase synchronization flow:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editApiKey,
                        onValueChange = { editApiKey = it },
                        label = { Text("Web API Key", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editProjectId,
                        onValueChange = { editProjectId = it },
                        label = { Text("Project ID", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editAppId,
                        onValueChange = { editAppId = it },
                        label = { Text("App ID", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.saveFirebaseConfig(editApiKey, editProjectId, editAppId)
                            isFirebaseConfigExpanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect Firebase Server", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("DATA CONTROLS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("button_reset_app"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset App Data", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("ABOUT DEBTBOOK", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Version: Zenith Release v1.0.2", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Powered by safe Android Room Local DB persistence with high-fidelity material indicators.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Entire Application?") },
                text = { Text("This operation is irreversible! It will erase all contacts, entities, payables, receivables, and your user profile settings safely from the device database.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetApp()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun QuickAddDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val entities by viewModel.allEntities.collectAsState()
    val symbol by viewModel.currencySymbol.collectAsState()

    var isExpense by remember { mutableStateOf(true) } // Expense (PAYABLE / I Owe Them) vs Income (RECEIVABLE / They Owe Me)
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEntityId by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf("Groceries") }

    var showAddPersonInline by remember { mutableStateOf(false) }
    var inlineName by remember { mutableStateOf("") }

    val categories = listOf("Groceries", "Dining", "Travel", "Rent", "Fun", "Health", "Bills", "Other")

    // Auto select newly inline-created entity
    LaunchedEffect(entities) {
        if (inlineName.trim().isNotEmpty()) {
            val newlyAdded = entities.find { it.name.equals(inlineName.trim(), ignoreCase = true) }
            if (newlyAdded != null) {
                selectedEntityId = newlyAdded.id
                inlineName = ""
                showAddPersonInline = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                        .padding(bottom = 12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Add", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Expense / Income Toggler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isExpense = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isExpense) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("You Borrowed (Payable)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { isExpense = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (!isExpense) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("You Lent (Receivable)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Amount inputs matching mockup
                Text("AMOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = symbol, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(200.dp)
                            .testTag("amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Entity select element
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ENTITY / PERSON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(
                            onClick = { showAddPersonInline = !showAddPersonInline },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (showAddPersonInline) "Back to List" else "+ Add New Person", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (showAddPersonInline || entities.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Add New Contact & Assign to Entry", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                var tempName by remember { mutableStateOf("") }
                                var tempPhone by remember { mutableStateOf("") }
                                var tempRel by remember { mutableStateOf("Friend") }
                                
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    label = { Text("Full Name", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = tempPhone,
                                    onValueChange = { tempPhone = it },
                                    label = { Text("Phone Number", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Relationship Tag", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                val relOptions = listOf("Friend", "Client", "Supplier", "Family")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    relOptions.forEach { opt ->
                                        val isSel = tempRel == opt
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { tempRel = opt }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = opt,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (tempName.trim().isNotEmpty()) {
                                            inlineName = tempName.trim()
                                            viewModel.addEntity(tempName.trim(), tempPhone.trim(), tempRel)
                                        }
                                    },
                                    enabled = tempName.trim().isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add & Select Person", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        val currentText = entities.find { it.id == selectedEntityId }?.name ?: "Choose Entity"

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currentText, color = MaterialTheme.colorScheme.onSurface)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                entities.forEach { ent ->
                                    DropdownMenuItem(
                                        text = { Text(ent.name) },
                                        onClick = {
                                            selectedEntityId = ent.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("What was this for?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("description_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category Grid with custom icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        val (icon, color) = getCategoryIconAndColor(cat)
                        val isSelected = selectedCategory == cat

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .background(
                                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val numAmount = amount.toDoubleOrNull() ?: 0.0
                        val entId = selectedEntityId
                        if (numAmount > 0 && entId != null && description.trim().isNotEmpty()) {
                            viewModel.addTransaction(
                                amount = numAmount,
                                description = description.trim(),
                                type = if (isExpense) "PAYABLE" else "RECEIVABLE",
                                category = selectedCategory,
                                entityId = entId
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_transaction_submit"),
                    shape = RoundedCornerShape(16.dp),
                    enabled = amount.isNotEmpty() && selectedEntityId != null && description.trim().isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Transaction", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Icon mapper corresponding to standard mockup category lists styled with palette colors
fun getCategoryIconAndColor(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "Groceries" -> Icons.Default.ShoppingCart to Color(0xFF041E49)     // Primary Deep Navy
        "Dining" -> Icons.Default.Restaurant to Color(0xFF005A32)         // Soft Emerald
        "Travel" -> Icons.Default.DirectionsCar to Color(0xFF601410)       // Dark Pink/Rose Tertiary
        "Rent" -> Icons.Default.Home to Color(0xFF354458)                 // Slate Minimalist Grey
        "Fun" -> Icons.Default.Movie to Color(0xFF5B3C88)                 // Royal Muted Purple
        "Health" -> Icons.Default.HealthAndSafety to Color(0xFF0F4C81)    // Classic Classic Blue
        "Bills" -> Icons.Default.Payments to Color(0xFF8D5B4C)            // Terracotta Minimal
        else -> Icons.Default.MoreHoriz to Color(0xFF44474E)              // Accent Grey
    }
}
