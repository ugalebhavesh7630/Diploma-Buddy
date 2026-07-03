@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui

import com.example.ui.theme.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Announcement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

// Screen Enumeration to build a robust, compile-safe local navigation manager
enum class HelperScreen {
    Login,
    Register,
    MainFrame, // Houses bottom navigation tabs
    AdminControl,
    PdfViewer,
    FeedbackSupport
}

enum class BottomTab {
    Dashboard,
    Materials,
    Community,
    Alerts,
    Profile
}

@Composable
fun AppContent(viewModel: MainViewModel) {
    val activeUser by viewModel.activeUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    
    // Bottom nav and flow manager
    var currentScreen by remember { mutableStateOf(HelperScreen.Login) }
    var currentTab by remember { mutableStateOf(BottomTab.Dashboard) }
    
    // PDF Viewer transition param
    var activeContentIdForPdf by remember { mutableIntStateOf(0) }

    // Edge alignment check on user session
    LaunchedEffect(activeUser) {
        if (activeUser == null) {
            currentScreen = HelperScreen.Login
        } else {
            if (currentScreen == HelperScreen.Login || currentScreen == HelperScreen.Register) {
                currentScreen = HelperScreen.MainFrame
            }
        }
    }

    // Compose Application Themes support (Light & Dark)
    MyApplicationTheme(
        darkTheme = when (themeMode) {
            "Light" -> false
            "Dark" -> true
            else -> isSystemInDarkTheme()
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                HelperScreen.Login -> LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { currentScreen = HelperScreen.Register }
                )
                HelperScreen.Register -> RegisterScreen(
                    viewModel = viewModel,
                    onBackToLogin = { currentScreen = HelperScreen.Login }
                )
                HelperScreen.MainFrame -> MainNavigationFrame(
                    viewModel = viewModel,
                    currentTab = currentTab,
                    onChangeTab = { currentTab = it },
                    onNavigateToAdmin = { currentScreen = HelperScreen.AdminControl },
                    onNavigateToPdf = { contentId ->
                        activeContentIdForPdf = contentId
                        currentScreen = HelperScreen.PdfViewer
                    },
                    onNavigateToFeedback = { currentScreen = HelperScreen.FeedbackSupport }
                )
                HelperScreen.AdminControl -> AdminDashboardScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = HelperScreen.MainFrame }
                )
                HelperScreen.PdfViewer -> EmbedPdfViewerScreen(
                    viewModel = viewModel,
                    contentId = activeContentIdForPdf,
                    onBack = { currentScreen = HelperScreen.MainFrame }
                )
                HelperScreen.FeedbackSupport -> HelpSupportScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = HelperScreen.MainFrame }
                )
            }
        }
    }
}

// ==========================================
// 1. AUTHENTICATION SCREENS (LOGIN/REGISTER)
// ==========================================

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit
) {
    var loginType by remember { mutableStateOf("Mobile") } // Mobile, Email, Google
    var inputVal by remember { mutableStateOf("") }
    var passwordVal by remember { mutableStateOf("") }
    var showIncorrectError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Brand Display Logo Header
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Icon",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = viewModel.translate("app_title"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Maharashtra State Board of Technical Education Support",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Toggle Selection buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            listOf("Mobile", "Email", "Google").forEach { type ->
                val isSelected = loginType == type
                Button(
                    onClick = {
                        loginType = type
                        inputVal = ""
                        passwordVal = ""
                        showIncorrectError = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("login_mode_$type"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = when (type) {
                            "Mobile" -> "Mobile"
                            "Email" -> "Email"
                            else -> "Google"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when (loginType) {
                    "Mobile" -> {
                        Text(
                            text = viewModel.translate("enter_mob"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputVal,
                            onValueChange = { inputVal = it },
                            placeholder = { Text("Enter 10-digit Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mobile_input"),
                            singleLine = true
                        )
                    }
                    "Email" -> {
                        Text(
                            text = viewModel.translate("enter_email"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputVal,
                            onValueChange = { inputVal = it },
                            placeholder = { Text("Enter Email credentials") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            singleLine = true
                        )
                    }
                    else -> {
                        Text(
                            text = viewModel.translate("google_login"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.performLogin("ugalebhavesh7630@gmail.com", "Google")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_google_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, "Google icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Google Account")
                        }
                    }
                }

                if (loginType != "Google") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = passwordVal,
                        onValueChange = { passwordVal = it },
                        placeholder = { Text("Enter Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Lock") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (loginType != "Google") {
            Button(
                onClick = {
                    if (inputVal.isEmpty()) {
                        showIncorrectError = true
                    } else {
                        viewModel.performLogin(inputVal, loginType)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = viewModel.translate("login_cta"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (showIncorrectError) {
            Text(
                text = "Please enter valid credentials to proceed.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.testTag("register_redirect_btn")
        ) {
            Text(
                text = "New student? Create an account here",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var selectedRegBranch by remember { mutableStateOf("Computer Engineering") }

    val branches = listOf("Computer Engineering", "Mechanical Engineering", "Civil Engineering")
    var isBranchDropdownExp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Student Registration",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Join Maharashtra's largest diploma community",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                Column {
                    Text(text = viewModel.translate("fullName"), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Bhavesh Ugale") },
                        leadingIcon = { Icon(Icons.Default.Person, "Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_fullName"),
                        singleLine = true
                    )
                }

                // Email Field
                Column {
                    Text(text = viewModel.translate("email"), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("student@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, "Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email"),
                        singleLine = true
                    )
                }

                // Mobile Field
                Column {
                    Text(text = viewModel.translate("mobileNumber"), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        placeholder = { Text("98xxxxxxxx") },
                        leadingIcon = { Icon(Icons.Default.Phone, "Phone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_mobile"),
                        singleLine = true
                    )
                }

                // College Name Field
                Column {
                    Text(text = viewModel.translate("collegeName"), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = college,
                        onValueChange = { college = it },
                        placeholder = { Text("Government Polytechnic, Pune") },
                        leadingIcon = { Icon(Icons.Default.School, "College") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_college"),
                        singleLine = true
                    )
                }

                // Branch Field Selector
                Column {
                    Text(text = viewModel.translate("select_branch"), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isBranchDropdownExp = !isBranchDropdownExp },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("reg_branch_dropdown_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = selectedRegBranch, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, "dropdown")
                        }
                        DropdownMenu(
                            expanded = isBranchDropdownExp,
                            onDismissRequest = { isBranchDropdownExp = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            branches.forEach { branchItem ->
                                DropdownMenuItem(
                                    text = { Text(branchItem) },
                                    onClick = {
                                        selectedRegBranch = branchItem
                                        isBranchDropdownExp = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && (email.isNotEmpty() || mobile.isNotEmpty())) {
                    viewModel.performCustomRegister(name, email, mobile, college, selectedRegBranch)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("reg_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = viewModel.translate("register"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.testTag("back_to_login_btn")
        ) {
            Text(text = "Already have an account? Sign In", fontWeight = FontWeight.SemiBold)
        }
    }
}


// ==========================================
// 2. MAIN NAVIGATION FRAME
// ==========================================

@Composable
fun MainNavigationFrame(
    viewModel: MainViewModel,
    currentTab: BottomTab,
    onChangeTab: (BottomTab) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToPdf: (Int) -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val currentBranch by viewModel.selectedBranch.collectAsState()
    val notificationList by viewModel.allNotifications.collectAsState()
    
    val unreadAlerts = notificationList.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Header Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.translate("app_title"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { onChangeTab(BottomTab.Alerts) },
                            modifier = Modifier.testTag("header_notif_btn")
                        ) {
                            Icon(Icons.Filled.NotificationsActive, "Alerts", tint = MaterialTheme.colorScheme.primary)
                            if (unreadAlerts > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Text("$unreadAlerts")
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Config tab items
                val barTabs = listOf(
                    Triple(BottomTab.Dashboard, Icons.Outlined.Home, viewModel.translate("home")),
                    Triple(BottomTab.Materials, Icons.Outlined.Book, viewModel.translate("materials")),
                    Triple(BottomTab.Community, Icons.Outlined.Groups, viewModel.translate("community")),
                    Triple(BottomTab.Alerts, Icons.Outlined.Notifications, viewModel.translate("alerts")),
                    Triple(BottomTab.Profile, Icons.Outlined.AccountCircle, viewModel.translate("profile"))
                )

                barTabs.forEach { (tab, icon, title) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onChangeTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
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
            when (currentTab) {
                BottomTab.Dashboard -> DashboardTab(
                    viewModel = viewModel,
                    onNavigateToPdf = onNavigateToPdf,
                    onChangeTab = onChangeTab,
                    onNavigateToAdmin = onNavigateToAdmin
                )
                BottomTab.Materials -> MaterialsTab(
                    viewModel = viewModel,
                    onNavigateToPdf = onNavigateToPdf
                )
                BottomTab.Community -> CommunityTab(
                    viewModel = viewModel
                )
                BottomTab.Alerts -> AlertsTab(
                    viewModel = viewModel
                )
                BottomTab.Profile -> ProfileTab(
                    viewModel = viewModel,
                    onNavigateToAdmin = onNavigateToAdmin,
                    onNavigateToFeedback = onNavigateToFeedback
                )
            }
        }
    }
}


// ==========================================
// 2A. TAB: DASHBOARD
// ==========================================

@Composable
fun ShortcutButton(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    onNavigateToPdf: (Int) -> Unit,
    onChangeTab: (BottomTab) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val isUserAdmin by viewModel.isAdminMode.collectAsState()
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val noticeList by viewModel.allNotices.collectAsState()
    val materialsList by viewModel.allBranchContents.collectAsState()
    val savedCount by viewModel.bookmarkedContents.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var searchQueryState by remember { mutableStateOf("") }

    // Filter contents to match selected branch
    val filteredBranchContentsByQuery = remember(materialsList, selectedBranch, searchQueryState) {
        materialsList.filter {
            it.branch == selectedBranch &&
            (searchQueryState.isEmpty() || 
             it.title.contains(searchQueryState, ignoreCase = true) || 
             it.subject.contains(searchQueryState, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = activeUser?.fullName ?: viewModel.translate("guest"),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUserAdmin) {
                        IconButton(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier.testTag("admin_dash_quick_btn")
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                "Admin",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8DEF8))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = (activeUser?.fullName ?: "S").split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Branch Selection Pill Row styled with Editorial Aesthetics
        item {
            Column {
                Text(
                    text = viewModel.translate("select_branch").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Computer Engineering", "Mechanical Engineering", "Civil Engineering").forEach { br ->
                        val isSel = selectedBranch == br
                        val containerBg = if (isSel) Color(0xFFF3EDF7) else MaterialTheme.colorScheme.surface
                        val borderCol = if (isSel) Color(0xFFCAC4D0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        
                        Card(
                            modifier = Modifier
                                .clickable { viewModel.changeSelectedBranch(br) }
                                .testTag("branch_chip_${br.split(' ').first().lowercase()}"),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = containerBg),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSel) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Text(
                                    text = br,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar integration with Editorial styling
        item {
            OutlinedTextField(
                value = searchQueryState,
                onValueChange = { searchQueryState = it },
                placeholder = { Text(viewModel.translate("search_hint")) },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQueryState.isNotEmpty()) {
                        IconButton(onClick = { searchQueryState = "" }) {
                            Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color(0xFFCAC4D0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Quick Category Nav Grid (3x2 Editorial Layout)
        item {
            Column {
                Text(
                    text = viewModel.translate("quick_shortcuts").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. NOTES
                        ShortcutButton(
                            title = "Notes",
                            icon = Icons.Default.Book,
                            containerColor = Color(0xFFEADDFF),
                            contentColor = Color(0xFF21005D),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Materials)
                            }
                        )
                        // 2. PAPERS
                        ShortcutButton(
                            title = "Papers",
                            icon = Icons.Default.Description,
                            containerColor = Color(0xFFE8DEF8),
                            contentColor = Color(0xFF1D192B),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Materials)
                            }
                        )
                        // 3. LABS
                        ShortcutButton(
                            title = "Labs",
                            icon = Icons.Default.Terminal,
                            containerColor = Color(0xFFF3EDF7),
                            contentColor = Color(0xFF49454F),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Materials)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 4. MSBTE
                        ShortcutButton(
                            title = "MSBTE",
                            icon = Icons.Default.Gavel,
                            containerColor = Color(0xFFD3E3FD),
                            contentColor = Color(0xFF0842A0),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Alerts)
                            }
                        )
                        // 5. SCHOLAR
                        ShortcutButton(
                            title = "Scholar",
                            icon = Icons.Default.School,
                            containerColor = Color(0xFFC2E7FF),
                            contentColor = Color(0xFF001D35),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Alerts)
                            }
                        )
                        // 6. FORUM (Community)
                        ShortcutButton(
                            title = "Forum",
                            icon = Icons.Default.Groups,
                            containerColor = Color(0xFFF7F2FA),
                            contentColor = EditorialPrimary,
                            borderColor = Color(0xFFCAC4D0),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                onChangeTab(BottomTab.Community)
                            }
                        )
                    }
                }
            }
        }

        // Latest Updates Section
        item {
            val pins = noticeList.filter { it.isPinned }
            val itemNotice = if (pins.isNotEmpty()) pins else noticeList
            if (itemNotice.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Latest Updates",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onChangeTab(BottomTab.Alerts) }
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemNotice.take(3).forEach { notice ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onChangeTab(BottomTab.Alerts) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isScholarship = notice.category == "Scholarship"
                                    val accentBg = if (isScholarship) Color(0xFFFFDADA) else Color(0xFFEADDFF)
                                    val accentText = if (isScholarship) Color(0xFF410002) else Color(0xFF21005D)
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(accentBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (isScholarship) "SCHOLAR" else "BOARD",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = accentText
                                            )
                                            Icon(
                                                imageVector = if (isScholarship) Icons.Default.School else Icons.Default.Campaign,
                                                contentDescription = notice.category,
                                                tint = accentText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = notice.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = notice.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Branch materials list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 Available ${selectedBranch.split(' ').first()} Files",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onChangeTab(BottomTab.Materials) }) {
                    Text("See All")
                }
            }
        }

        if (filteredBranchContentsByQuery.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, "Empty", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translate("no_results"),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(filteredBranchContentsByQuery.take(3)) { material ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPdf(material.id) }
                        .testTag("material_item_${material.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (material.category) {
                                    "Notes" -> Icons.Default.Book
                                    "Previous Year Papers" -> Icons.Default.Assignment
                                    "Lab Manuals" -> Icons.Default.Science
                                    else -> Icons.Default.Attachment
                                },
                                contentDescription = "Type",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = material.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = material.subject,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(material.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Text("Downloads: ${material.downloadCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Bookmark action from dashboard list
                        IconButton(
                            onClick = { viewModel.toggleBookmark(material.id, !material.isBookmarked) },
                            modifier = Modifier.testTag("bookmark_btn_${material.id}")
                        ) {
                            Icon(
                                imageVector = if (material.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (material.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Saved PDFs list
        item {
            Text(
                text = "💾 ${viewModel.translate("saved_content")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (savedCount.isEmpty()) {
            item {
                Text(
                    text = viewModel.translate("saved_empty"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        } else {
            items(savedCount) { saved ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .clickable { onNavigateToPdf(saved.id) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FileCopy,
                        "offline",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = saved.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.toggleBookmark(saved.id, false) }) {
                        Icon(Icons.Default.DeleteOutline, "delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// ==========================================
// 2B. TAB: MATERIALS (BRANCH LIBRARY)
// ==========================================

@Composable
fun MaterialsTab(
    viewModel: MainViewModel,
    onNavigateToPdf: (Int) -> Unit
) {
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val allContents by viewModel.allBranchContents.collectAsState()

    var activeCatFilter by remember { mutableStateOf("All") }
    var queryStr by remember { mutableStateOf("") }

    val categories = listOf("All", "Notes", "Previous Year Papers", "Lab Manuals", "Assignments")

    // Dynamic library search and branch filters
    val filteredContents = remember(allContents, selectedBranch, activeCatFilter, queryStr) {
        allContents.filter {
            it.branch == selectedBranch &&
            (activeCatFilter == "All" || it.category == activeCatFilter) &&
            (queryStr.isEmpty() || 
             it.title.contains(queryStr, ignoreCase = true) || 
             it.subject.contains(queryStr, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "📚 branch library".uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = selectedBranch,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search panel
        OutlinedTextField(
            value = queryStr,
            onValueChange = { queryStr = it },
            placeholder = { Text("Filter by subject code, notes name...") },
            leadingIcon = { Icon(Icons.Default.FilterList, "Filter icon") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("materials_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal filter buttons
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = activeCatFilter == cat
                InputChip(
                    selected = isSelected,
                    onClick = { activeCatFilter = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    modifier = Modifier.testTag("cat_chip_${cat.replace(" ", "_").lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredContents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No files found for $activeCatFilter in this branch.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContents) { mat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPdf(mat.id) }
                            .testTag("materials_card_${mat.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        "pdf",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mat.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = mat.subject,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleBookmark(mat.id, !mat.isBookmarked) },
                                    modifier = Modifier.testTag("book_toggle_${mat.id}")
                                ) {
                                    Icon(
                                        imageVector = if (mat.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (mat.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(mat.category, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, "downloads", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text("${mat.downloadCount} dl", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


// ==========================================
// 2C. TAB: COMMUNITY (DISCUSSIONS/DOUBTS)
// ==========================================

@Composable
fun CommunityTab(
    viewModel: MainViewModel
) {
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val postsList by viewModel.communityPosts.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()

    var activeSectSelected by remember { mutableStateOf("Discussions") } // Discussions, Doubts solver
    var contentInput by remember { mutableStateOf("") }
    var replyingPostId by remember { mutableStateOf<Int?>(null) }
    var replyingPostAuthor by remember { mutableStateOf("") }
    
    val commentsStateList = remember { mutableStateMapOf<Int, List<CommentEntity>>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "🤝 student community".uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = selectedBranch.split(' ').first() + " Hub",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle category
        TabRow(
            selectedTabIndex = if (activeSectSelected == "Discussions") 0 else 1,
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = activeSectSelected == "Discussions",
                onClick = { activeSectSelected = "Discussions" },
                text = { Text("Discussion Board") },
                modifier = Modifier.testTag("community_tab_discussions")
            )
            Tab(
                selected = activeSectSelected == "Doubts",
                onClick = { activeSectSelected = "Doubts" },
                text = { Text("Doubt Solving Area") },
                modifier = Modifier.testTag("community_tab_doubts")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Write Post card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (replyingPostId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Replying to comment thread by $replyingPostAuthor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { replyingPostId = null },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Cancel, "Cancel reply")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    placeholder = { 
                        Text(
                            if (replyingPostId != null) "Type your reply..." 
                            else viewModel.translate("placeholder_content")
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_input_box"),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (contentInput.isNotBlank()) {
                                if (replyingPostId != null) {
                                    viewModel.addComment(
                                        refType = "post",
                                        refId = replyingPostId!!,
                                        content = contentInput
                                    )
                                    replyingPostId = null
                                } else {
                                    val cat = if (activeSectSelected == "Discussions") "Discussion" else "Doubt"
                                    viewModel.addNewPost(contentInput, cat)
                                }
                                contentInput = ""
                            }
                        },
                        modifier = Modifier.testTag("post_publish_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (replyingPostId != null) "Reply Thread" else viewModel.translate("post"))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Community Items list
        val filteredPosts = postsList.filter {
            if (activeSectSelected == "Discussions") it.category == "Discussion" else it.category == "Doubt"
        }

        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ChatBubbleOutline, "Forum Empty", modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Be the first to post a study doubt or start a discussion!", fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPosts) { post ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_post_card_${post.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (post.authorName.firstOrNull()?.toString() ?: "S"),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = post.authorCollege, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (post.isPinned) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.PushPin, "Pinned", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = post.content,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Row
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.togglePostLike(post.id, post.isLiked) },
                                        modifier = Modifier.testTag("like_post_${post.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (post.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = "Like",
                                            tint = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text("${post.likesCount}", fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = {
                                        replyingPostId = post.id
                                        replyingPostAuthor = post.authorName
                                    }
                                ) {
                                    Icon(Icons.Default.Reply, "Reply", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reply thread", fontSize = 12.sp)
                                }

                                if (post.authorName == activeUser?.fullName || viewModel.isAdminMode.value) {
                                    IconButton(onClick = { viewModel.deletePost(post) }) {
                                        Icon(Icons.Default.DeleteOutline, "delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Interactive Comments list nested inside post
                            val comments by viewModel.getCommentsForPost("post", post.id).collectAsState(initial = emptyList())
                            if (comments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        comments.forEach { c ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(Icons.Default.SubdirectoryArrowRight, "nested reply indicator", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(c.authorName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(c.content, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                                }
                                                // Comment like
                                                IconButton(
                                                    onClick = { viewModel.toggleCommentLike(c.id, c.isLiked) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (c.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                        contentDescription = "Like comments",
                                                        tint = if (c.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


// ==========================================
// 2D. TAB: ALERTS (MSBTE CIRCULARS & SCHOLARSHIPS)
// ==========================================

@Composable
fun AlertsTab(
    viewModel: MainViewModel
) {
    val selectedBranch by viewModel.selectedBranch.collectAsState()
    val noticeList by viewModel.allNotices.collectAsState()
    val rawAlertList by viewModel.allNotifications.collectAsState()

    var activeAlertTab by remember { mutableStateOf("Scholarships") } // Scholarships, MSBTE Circulars, In-App Logs

    val activeUserBranch = selectedBranch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "📢 BOARD NOTIFICATION SYSTEM",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Official Board Alerts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Scholarships", "MSBTE Circulars", "Alert Logs").forEach { tab ->
                val isSelected = activeAlertTab == tab
                Button(
                    onClick = { activeAlertTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("alert_tab_$tab"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = tab, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeAlertTab) {
            "Scholarships" -> {
                val scholarshipItems = noticeList.filter { it.category == "Scholarship" }
                if (scholarshipItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No active scholarship notices declared.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(scholarshipItems) { sch ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = sch.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = sch.description, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Metadata
                                    if (sch.eligibility.isNotEmpty()) {
                                        Text(text = "Eligibility: ${sch.eligibility}", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Text(text = "Last Date to Apply: ${sch.lastDate}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    
                                    if (sch.officialLink.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { /* Open WebView/Browser */ },
                                            modifier = Modifier.align(Alignment.End),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Apply on Official Portal")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "MSBTE Circulars" -> {
                val circulars = noticeList.filter { it.category != "Scholarship" }
                if (circulars.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No official MSBTE circulars found.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(circulars) { circ ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, circ.category, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = circ.category.uppercase(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        if (circ.isPinned) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.PushPin, "Pinned", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = circ.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = circ.description, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Important Target Date: ${circ.lastDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }

            "Alert Logs" -> {
                // In App alert logs logger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Received Push Logs (${rawAlertList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row {
                                TextButton(onClick = { viewModel.markNotificationsAsRead() }) {
                                    Text("Mark Read")
                                }
                                TextButton(onClick = { viewModel.clearNotifications() }) {
                                    Text("Clear All")
                                }
                            }
                        }
                        
                        if (rawAlertList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No alerts received.")
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rawAlertList) { alert ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Campaign,
                                                contentDescription = "Alert logo",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(alert.message, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2E. TAB: PROFILE & ACCESSIBILITY SETTINGS
// ==========================================

@Composable
fun ProfileTab(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsState()
    val isUserAdmin by viewModel.isAdminMode.collectAsState()
    val languageActive by viewModel.language.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()

    var isEditProfileExp by remember { mutableStateOf(false) }

    // local field holds
    var nameField by remember { mutableStateOf(activeUser?.fullName ?: "") }
    var collegeField by remember { mutableStateOf(activeUser?.collegeName ?: "") }
    var branchField by remember { mutableStateOf(activeUser?.branch ?: "Computer Engineering") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Round Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activeUser?.fullName?.firstOrNull()?.toString()?.uppercase() ?: "S",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = activeUser?.fullName ?: "Bhavesh Ugale",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = activeUser?.collegeName ?: "Government Polytechnic, Pune",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Badge(modifier = Modifier.padding(top = 8.dp)) {
            Text(activeUser?.branch ?: "Computer Engineering", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile details editor toggle
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditProfileExp = !isEditProfileExp },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, "Edit icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(viewModel.translate("edit_profile"), fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        if (isEditProfileExp) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Expand info"
                    )
                }

                if (isEditProfileExp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = collegeField,
                            onValueChange = { collegeField = it },
                            label = { Text("College Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                viewModel.modifyProfile(
                                    nameField,
                                    activeUser?.email ?: "",
                                    activeUser?.mobileNumber ?: "",
                                    collegeField,
                                    branchField
                                )
                                isEditProfileExp = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update Profile Information")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Language settings toggles (Marathi, Hindi, English changing instantly)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = viewModel.translate("change_branch") + " / " + "Language",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("English", "Marathi", "Hindi").forEach { lang ->
                        val isSel = languageActive == lang
                        Button(
                            onClick = { viewModel.changeLanguage(lang) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lang_toggle_$lang"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    "Marathi" -> "मराठी"
                                    "Hindi" -> "हिंदी"
                                    else -> "English"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Appearance Mode Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = viewModel.translate("appearance"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Light", "Dark", "System").forEach { mode ->
                        val isSel = currentTheme == mode
                        Button(
                            onClick = { viewModel.changeThemeMode(mode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_$mode"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Help & Support Feedback Redirect
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToFeedback() }
                .testTag("feedback_support_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ContactSupport, "help", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(viewModel.translate("help_support"), fontWeight = FontWeight.Bold)
                    Text("FAQ, contact admin support desk.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "open")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Admin Access unlocking area
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            var adminPassword by remember { mutableStateOf("") }
            var isPassError by remember { mutableStateOf(false) }

            Column(modifier = Modifier.padding(12.dp)) {
                Text("Lock/Unlock Admin Dash Controls", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isUserAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(viewModel.translate("is_admin"), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Button(onClick = { viewModel.setAdminMode(false) }) {
                            Text("Disable Admin")
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            placeholder = { Text("Code: admin123") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_password_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (adminPassword == "admin123") {
                                    viewModel.setAdminMode(true)
                                    isPassError = false
                                } else {
                                    isPassError = true
                                }
                            },
                            modifier = Modifier.testTag("admin_auth_confirm_btn")
                        ) {
                            Text("Unlock")
                        }
                    }
                    if (isPassError) {
                        Text("Incorrect authorization password.", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_btn"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = viewModel.translate("logout"), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}


// ==========================================
// 3. ADMIN CONTROL CENTER SCREEN
// ==========================================

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    // Media Upload Fields
    var matBranch by remember { mutableStateOf("Computer Engineering") }
    var matCategory by remember { mutableStateOf("Notes") }
    var matTitle by remember { mutableStateOf("") }
    var matSubject by remember { mutableStateOf("") }
    var matContentText by remember { mutableStateOf("") }
    var matUrl by remember { mutableStateOf("https://science.pdf") }

    // Circular Notice Fields
    var circCat by remember { mutableStateOf("MSBTE Circular") }
    var circTitle by remember { mutableStateOf("") }
    var circDesc by remember { mutableStateOf("") }
    var circEligible by remember { mutableStateOf("") }
    var circLastDate by remember { mutableStateOf("") }
    var circLink by remember { mutableStateOf("") }
    var circBranchTarget by remember { mutableStateOf("All") }
    var circIsPinned by remember { mutableStateOf(false) }

    var isUploadResultAlert by remember { mutableStateOf("") }

    val dbBranches = listOf("Computer Engineering", "Mechanical Engineering", "Civil Engineering")
    val dbCategories = listOf("Notes", "Previous Year Papers", "Lab Manuals", "Assignments")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text("Admin Control Console", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("📤 UPLOAD ACADEMIC MATERIAL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                // Fields
                OutlinedTextField(value = matTitle, onValueChange = { matTitle = it }, label = { Text("Material Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = matSubject, onValueChange = { matSubject = it }, label = { Text("Subject (eg. MAD, TOM)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = matContentText, onValueChange = { matContentText = it }, label = { Text("PDF Content Text / Body Page") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                
                // Branch chip selector
                Text("Select Branch Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dbBranches.forEach { br ->
                        FilterChip(
                            selected = matBranch == br,
                            onClick = { matBranch = br },
                            label = { Text(br, fontSize = 11.sp) }
                        )
                    }
                }

                // Category chip selector
                Text("Select Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dbCategories.forEach { cat ->
                        FilterChip(
                            selected = matCategory == cat,
                            onClick = { matCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (matTitle.isNotBlank() && matSubject.isNotBlank()) {
                            viewModel.uploadMaterialsByAdmin(matBranch, matCategory, matTitle, matSubject, matContentText, matUrl)
                            isUploadResultAlert = "Material item posted successfully."
                            matTitle = ""
                            matSubject = ""
                            matContentText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_upload_submit"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Publish to Student Library")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Circular and notices uploads
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("📢 PUBLISH GENERAL NOTICE & SCHOLARSHIP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                OutlinedTextField(value = circTitle, onValueChange = { circTitle = it }, label = { Text("Notice Header Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = circDesc, onValueChange = { circDesc = it }, label = { Text("Circular Body Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                OutlinedTextField(value = circEligible, onValueChange = { circEligible = it }, label = { Text("Eligibility criteria (for Scholarships)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = circLastDate, onValueChange = { circLastDate = it }, label = { Text("Target Due Date (eg. 15th Aug)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = circLink, onValueChange = { circLink = it }, label = { Text("External Action URL portal") }, modifier = Modifier.fillMaxWidth())

                // Type select
                Text("Notice Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MSBTE Circular", "Notice", "Scholarship", "Announcement", "Result").forEach { itemType ->
                        FilterChip(
                            selected = circCat == itemType,
                            onClick = { circCat = itemType },
                            label = { Text(itemType, fontSize = 11.sp) }
                        )
                    }
                }

                // Is pinned check
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = circIsPinned, onCheckedChange = { circIsPinned = it })
                    Text("Pin this announcement to Dashboard Carousel", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (circTitle.isNotBlank() && circDesc.isNotBlank()) {
                            viewModel.createNoticeByAdmin(
                                circCat, circTitle, circDesc, circEligible, circLastDate, circLink, circBranchTarget, circIsPinned
                            )
                            isUploadResultAlert = "Circular Notice generated successfully."
                            circTitle = ""
                            circDesc = ""
                            circEligible = ""
                            circLastDate = ""
                            circLink = ""
                            circIsPinned = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_notice_submit"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Broadcast Alert Now")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isUploadResultAlert.isNotEmpty()) {
            Text(
                text = isUploadResultAlert,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ==========================================
// 4. EMBEDDED PDF VIEWER SCREEN
// ==========================================

@Composable
fun EmbedPdfViewerScreen(
    viewModel: MainViewModel,
    contentId: Int,
    onBack: () -> Unit
) {
    val allContents by viewModel.allBranchContents.collectAsState()
    val resolvedContent = allContents.find { it.id == contentId }

    var isDownloadedState by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0.0f) }
    var isDownloading by remember { mutableStateOf(false) }

    val coroutine = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App top header
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = resolvedContent?.title ?: "Document Viewer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resolvedContent?.subject ?: "Notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("pdf_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
                }
            },
            actions = {
                if (resolvedContent != null) {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(resolvedContent.id, !resolvedContent.isBookmarked) },
                        modifier = Modifier.testTag("pdf_bookmark_toggle")
                    ) {
                        Icon(
                            imageVector = if (resolvedContent.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "bookmark",
                            tint = if (resolvedContent.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                IconButton(onClick = { /* Simulated Android Share Intent */ }, modifier = Modifier.testTag("pdf_share_btn")) {
                    Icon(Icons.Default.Share, "Share notes")
                }
            }
        )

        if (resolvedContent == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: Note item could not be retrieved locally.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Inline downlaod trigger bar helper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDownloadedState) "✓ Saved for offline use" else "Available to bookmark and save offline",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDownloadedState) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isDownloading) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (!isDownloadedState) {
                        Button(
                            onClick = {
                                isDownloading = true
                                coroutine.launch {
                                    // Simulated high speed download loops
                                    for (i in 1..10) {
                                        kotlinx.coroutines.delay(100)
                                        downloadProgress = i / 10f
                                    }
                                    isDownloading = false
                                    isDownloadedState = true
                                    viewModel.simulateDownloadFile(resolvedContent.id)
                                }
                            },
                            modifier = Modifier.testTag("pdf_download_init_btn"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text viewer simulator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "--- PREVIEW PAGE 1 ---",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = resolvedContent.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "MSBTE Diploma Syllabus Companion - Branch: ${resolvedContent.branch}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = resolvedContent.contentText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "--- END OF DOCUMENT PREVIEW ---",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // PDF Comments module integration
            Box(
                modifier = Modifier
                    .fillToHeightFraction(0.35f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                var localCommentInput by remember { mutableStateOf("") }
                val commentsList by viewModel.getCommentsForPost("content", resolvedContent.id).collectAsState(initial = emptyList())

                Column(modifier = Modifier.padding(12.dp)) {
                    Text("File Comments & Discussions (${commentsList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = localCommentInput,
                            onValueChange = { localCommentInput = it },
                            placeholder = { Text("Type query about this note...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pdf_comment_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (localCommentInput.isNotBlank()) {
                                    viewModel.addComment("content", resolvedContent.id, localCommentInput)
                                    localCommentInput = ""
                                }
                            },
                            modifier = Modifier.testTag("pdf_comment_submit_btn")
                        ) {
                            Icon(Icons.Default.Send, "Send comments", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (commentsList.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No doubts posted on this note. Ask a question!", fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(commentsList) { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.5f))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = "comment bubble",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(comment.content, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// Fraction height helper
@Composable
fun Modifier.fillToHeightFraction(fraction: Float): Modifier {
    return this.fillMaxHeight(fraction)
}


// ==========================================
// 5. HELP & FAQ FEEDBACK FORM SUPPORT SCREEN
// ==========================================

@Composable
fun HelpSupportScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var feedbackType by remember { mutableStateOf("Feedback") }
    var userMessageText by remember { mutableStateOf("") }
    var isSubmittedAlert by remember { mutableStateOf(false) }

    val faqs = listOf(
        Pair("How score is calculated?", "The MSBTE grade points are calculated based on theory marks (70 marks board paper) plus internal practical assessment scores."),
        Pair("How to read notes offline?", "Simply tap the Bookmark icon on any PDF document. It will be saved securely to your local library for immediate offline access."),
        Pair("Are Tata scholarships active?", "Yes, Tata trusts and MAHDBT portals are open for this academic term. Check details in our board section.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("help_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text("Help & Academic Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FAQ section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))
                faqs.forEach { (q, a) ->
                    Text("• $q", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(a, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback ticket submit form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Contact Council Admin support", fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Feedback", "Report Issue", "Syllabus request").forEach { type ->
                        val isSelected = feedbackType == type
                        Button(
                            onClick = { feedbackType = type },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("Write your message to college administrators...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("support_input"),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (userMessageText.isNotBlank()) {
                            isSubmittedAlert = true
                            userMessageText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_submit_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Query Ticket")
                }

                if (isSubmittedAlert) {
                    Text(
                        text = "Ticket submitted successfully! Admin panel will notify you shortly.",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
