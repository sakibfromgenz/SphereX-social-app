package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.*
import coil.compose.AsyncImage
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SphereXApp(viewModel: SphereXViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()
    val reels by viewModel.allReels.collectAsState()
    val communities by viewModel.allCommunities.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val comments by viewModel.activeComments.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val activeCallRoom by viewModel.activeCallRoom.collectAsState()

    // UI Drawer state for Gemini AI
    var showAiAssistant by remember { mutableStateOf(false) }
    var activeStoryUser by remember { mutableStateOf<String?>(null) } // active story playback overlay
    var showNotificationSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            if (currentScreen != "auth") {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cloud,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "SphereX",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.testTag("app_title")
                            )
                        }
                    },
                    actions = {
                        // Admin Panel entry
                        IconButton(
                            onClick = { viewModel.navigateTo("admin") },
                            modifier = Modifier.testTag("nav_admin_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "Admin Metrics",
                                tint = if (currentScreen == "admin") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        // Notifications badge trigger
                        IconButton(
                            onClick = { showNotificationSheet = true },
                            modifier = Modifier.testTag("notifications_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    val unreadCount = notifications.size
                                    if (unreadCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }

                        // Gemini Assistant floating helper trigger
                        IconButton(
                            onClick = { showAiAssistant = !showAiAssistant },
                            modifier = Modifier.testTag("ai_helper_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Gemini Assistant",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentScreen != "auth" && activeStoryUser == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "feed",
                        onClick = { viewModel.navigateTo("feed") },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Feed", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_feed")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "discover",
                        onClick = { viewModel.navigateTo("discover") },
                        icon = { Icon(Icons.Filled.Explore, contentDescription = "Discover") },
                        label = { Text("Explore", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_discover")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "reels",
                        onClick = { viewModel.navigateTo("reels") },
                        icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Reels") },
                        label = { Text("Reels", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_reels")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "communities",
                        onClick = { viewModel.navigateTo("communities") },
                        icon = { Icon(Icons.Filled.Groups, contentDescription = "Groups") },
                        label = { Text("Groups", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_communities")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "monetization",
                        onClick = { viewModel.navigateTo("monetization") },
                        icon = { Icon(Icons.Filled.MonetizationOn, contentDescription = "Monetize") },
                        label = { Text("Creator", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_monetize")
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
            // Screen router
            when (currentScreen) {
                "auth" -> AuthScreen(viewModel)
                "feed" -> FeedScreen(
                    viewModel = viewModel,
                    posts = posts,
                    profile = profile ?: UserProfile(),
                    onSelectStory = { activeStoryUser = it }
                )
                "discover" -> DiscoverScreen(viewModel)
                "reels" -> ReelsScreen(viewModel = viewModel, reels = reels, profile = profile ?: UserProfile())
                "communities" -> CommunitiesScreen(viewModel = viewModel, communities = communities)
                "monetization" -> CreatorStudioScreen(viewModel = viewModel, profile = profile ?: UserProfile())
                "admin" -> AdminPanelScreen(viewModel = viewModel, posts = posts)
                "profile" -> ProfileScreen(viewModel = viewModel, profile = profile ?: UserProfile())
                "edit_profile" -> EditProfileScreen(viewModel = viewModel, profile = profile ?: UserProfile())
                "chat_room" -> ChatRoomScreen(viewModel = viewModel, recipient = activeChatUser ?: "elena_vision", messages = messages)
                "chats" -> ChatListScreen(viewModel = viewModel)
                "security" -> SecurityDashboardScreen(viewModel = viewModel, profile = profile ?: UserProfile())
                else -> FeedScreen(
                    viewModel = viewModel,
                    posts = posts,
                    profile = profile ?: UserProfile(),
                    onSelectStory = { activeStoryUser = it }
                )
            }

            // Quick Floating Button to view Chat Threads list (only if in main feeds)
            if (currentScreen in listOf("feed", "discover", "communities", "reels", "monetization", "profile") && activeStoryUser == null) {
                FloatingActionButton(
                    onClick = { viewModel.navigateTo("chats") },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .testTag("floating_dm_btn")
                ) {
                    Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = "DMs")
                }
            }

            // Active Story Full Playback Screen Overlay
            activeStoryUser?.let { username ->
                StoryPlaybackOverlay(
                    username = username,
                    onDismiss = { activeStoryUser = null },
                    viewModel = viewModel
                )
            }

            // Real-Time Notification Bottom Sheet Overlay
            if (showNotificationSheet) {
                NotificationOverlay(
                    notifications = notifications,
                    onDismiss = { showNotificationSheet = false },
                    viewModel = viewModel
                )
            }

            // Gemini Interactive AI Sidebar/Drawer Overlay
            if (showAiAssistant) {
                AiAssistantOverlay(
                    viewModel = viewModel,
                    onDismiss = { showAiAssistant = false }
                )
            }

            // Voice & Video Call Active Call Screen Overlay
            activeCallRoom?.let { participant ->
                CallScreenOverlay(
                    participantName = participant,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ========================
// AUTHENTICATION SYSTEM
// ========================
@Composable
fun AuthScreen(viewModel: SphereXViewModel) {
    val username by viewModel.loginUsername.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val registerMode by viewModel.sessionRegister.collectAsState()
    val is2FA by viewModel.is2FARequired.collectAsState()
    val pinCode by viewModel.pin2FA.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("auth_card")
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = "SphereX Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = if (is2FA) "Two-Factor Verification" else if (registerMode) "Create SphereX ID" else "SphereX Portal Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (is2FA) "Security code required is sent to phone" else "Interact dynamically with creators globally",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (authError.isNotEmpty()) {
                    Text(
                        text = authError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (!is2FA) {
                    TextField(
                        value = username,
                        onValueChange = { viewModel.loginUsername.value = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    TextField(
                        value = password,
                        onValueChange = { viewModel.loginPassword.value = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Button(
                        onClick = { viewModel.handleAuthSubmit() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_auth_btn")
                    ) {
                        Text(
                            text = if (registerMode) "Sign Up" else "Login securely",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (registerMode) "Already have an account? " else "Or craft a trial profile? ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (registerMode) "Log In" else "Sign Up Instantly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.sessionRegister.value = !registerMode }
                                .testTag("toggle_register_btn")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Demo Credentials: username 'alex', password is any key (toggles 2FA simulation)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                } else {
                    // Two Factor Authentication Code
                    Text(
                        text = "Enter default passcode '1234' to verify",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    TextField(
                        value = pinCode,
                        onValueChange = { viewModel.pin2FA.value = it },
                        label = { Text("Verification Pin") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_2fa_input")
                    )

                    Button(
                        onClick = { viewModel.handleTwoFactorVerify() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_2fa_btn")
                    ) {
                        Text("Verify and Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ========================
// HOME FEED + STORIES
// ========================
@Composable
fun FeedScreen(
    viewModel: SphereXViewModel,
    posts: List<PostEntity>,
    profile: UserProfile,
    onSelectStory: (String) -> Unit
) {
    var textPostContent by remember { mutableStateOf("") }
    var imagePostUrl by remember { mutableStateOf("") }
    var locationTagInput by remember { mutableStateOf("") }
    var activePollOptionA by remember { mutableStateOf("") }
    var activePollOptionB by remember { mutableStateOf("") }
    var showExtendedComposer by remember { mutableStateOf(false) }
    var currentPostType by remember { mutableStateOf("TEXT") } // TEXT, IMAGE, POLL

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_lazy_column"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Horizontal Stories Line at top
        item {
            StoryStrip(onSelectStory = onSelectStory, profile = profile)
        }

        // Expanded Rich Composer Layer
        item {
            ComposerCard(
                username = profile.username,
                avatarUrl = profile.avatarUrl,
                postText = textPostContent,
                onPostTextChange = { textPostContent = it },
                imageText = imagePostUrl,
                onImageTextChange = { imagePostUrl = it },
                pollAText = activePollOptionA,
                onPollAChange = { activePollOptionA = it },
                pollBText = activePollOptionB,
                onPollBChange = { activePollOptionB = it },
                locationText = locationTagInput,
                onLocationChange = { locationTagInput = it },
                showDetails = showExtendedComposer,
                onToggleDetails = { showExtendedComposer = !showExtendedComposer },
                currentType = currentPostType,
                onTypeSelect = { currentPostType = it },
                onSubmit = {
                    if (textPostContent.trim().isNotEmpty()) {
                        if (currentPostType == "POLL") {
                            viewModel.submitPollPost(
                                textPostContent,
                                "${activePollOptionA.ifEmpty { "A" }}|${activePollOptionB.ifEmpty { "B" }}"
                            )
                        } else {
                            viewModel.submitPost(textPostContent, currentPostType, imagePostUrl, locationTagInput)
                        }
                        textPostContent = ""
                        imagePostUrl = ""
                        activePollOptionA = ""
                        activePollOptionB = ""
                        locationTagInput = ""
                        showExtendedComposer = false
                    }
                },
                viewModel = viewModel
            )
        }

        // Filter Header Bar (Following, Friends, Trending, Personalized)
        item {
            val activeFilter by viewModel.selectedFeedFilter.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "personalized" to "Personalized 🌟",
                    "trending" to "Trending 🔥",
                    "following" to "Following 👥",
                    "friends" to "Friends Only"
                ).forEach { (key, display) ->
                    FilterChip(
                        selected = activeFilter == key,
                        onClick = { viewModel.selectedFeedFilter.value = key },
                        label = { Text(display) }
                    )
                }
            }
        }

        // Active Posts List
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "empty",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No active feed reports. Modify filters above!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                PostItemCard(
                    post = post,
                    viewModel = viewModel,
                    activeProfileUsername = profile.username
                )
            }
        }
    }
}

@Composable
fun StoryStrip(onSelectStory: (String) -> Unit, profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Active Stories (24h)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Self add story icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelectStory(profile.username) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Story", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // Quick Creators Stories list
            listOf(
                Triple("elena_vision", "Elena V", "https://images.unsplash.com/photo-1494790108377-be9c29b29330"),
                Triple("leo_beats", "Leo Synth", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"),
                Triple("zara_rebel", "Zara Rebel", "https://images.unsplash.com/photo-1517841905240-472988babdf9")
            ).forEach { (username, display, avatar) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelectStory(username) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.primary
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(display, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ComposerCard(
    username: String,
    avatarUrl: String,
    postText: String,
    onPostTextChange: (String) -> Unit,
    imageText: String,
    onImageTextChange: (String) -> Unit,
    pollAText: String,
    onPollAChange: (String) -> Unit,
    pollBText: String,
    onPollBChange: (String) -> Unit,
    locationText: String,
    onLocationChange: (String) -> Unit,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    currentType: String,
    onTypeSelect: (String) -> Unit,
    onSubmit: () -> Unit,
    viewModel: SphereXViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "My avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                OutlinedTextField(
                    value = postText,
                    onValueChange = onPostTextChange,
                    placeholder = { Text("What's shifting in the matrix today?", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 120.dp)
                        .testTag("composer_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                // AI smart autofill assistant panel button
                IconButton(
                    onClick = { viewModel.generateAiCaption(postText) },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, "AI helper suggestions", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            AnimatedVisibility(visible = showDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Selector buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TEXT", "IMAGE", "POLL").forEach { type ->
                            ElevatedFilterChip(
                                selected = currentType == type,
                                onClick = { onTypeSelect(type) },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }

                    when (currentType) {
                        "IMAGE" -> {
                            TextField(
                                value = imageText,
                                onValueChange = onImageTextChange,
                                label = { Text("Insert Image URL (e.g., Unsplash/Web link)") },
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("composer_image_url")
                            )
                            TextField(
                                value = locationText,
                                onValueChange = onLocationChange,
                                label = { Text("Add Location Tag (Optional)") },
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "POLL" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = pollAText,
                                    onValueChange = onPollAChange,
                                    label = { Text("Option A") },
                                    modifier = Modifier.weight(1f)
                                )
                                TextField(
                                    value = pollBText,
                                    onValueChange = onPollBChange,
                                    label = { Text("Option B") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleDetails) {
                    Icon(
                        imageVector = if (showDetails) Icons.Filled.Close else Icons.Filled.AttachFile,
                        contentDescription = "Expand attachment",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("submit_post_btn")
                ) {
                    Text("Broadcast", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun PostItemCard(
    post: PostEntity,
    viewModel: SphereXViewModel,
    activeProfileUsername: String
) {
    var showAltComments by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    val commentsList by viewModel.activeComments.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = post.userAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.navigateToProfile(post.username) },
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = post.userDisplayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { viewModel.navigateToProfile(post.username) }
                        )
                        if (post.username == "spherex_hq" || post.username == "elena_vision" || post.username == "zara_rebel") {
                            Icon(Icons.Filled.Verified, "Verified Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (post.locationTag.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Place, "Location", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                            Text(post.locationTag, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Delete node button (if creator is current user OR admin entry)
                if (post.username == activeProfileUsername) {
                    IconButton(onClick = { viewModel.adminDeletePost(post) }) {
                        Icon(Icons.Filled.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Post content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Image Card
            if (post.type == "IMAGE" && post.mediaUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Post Artwork",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Poll Post Options
            if (post.type == "POLL" && post.pollOptions.isNotEmpty()) {
                val options = post.pollOptions.split("|")
                val activeVotes by viewModel.pollVotes.collectAsState()
                val votedOption = activeVotes[post.id]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEachIndexed { idx, option ->
                        val isSelected = votedOption == idx
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.castVote(post.id, idx) }
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (isSelected) "$option (Voted! ✓)" else option,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Interactive Reactions bar (Facebook + Instagram Mix)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Love wow support
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = { viewModel.reactToPost(post.id, "LIKE") },
                        modifier = Modifier.testTag("reaction_like_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.hasLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "reaction Love",
                            tint = if (post.hasLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Haha reaction
                    IconButton(onClick = { viewModel.reactToPost(post.id, "HAHA") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("😂", fontSize = 16.sp)
                            if (post.hahaCount > 0) {
                                Text(
                                    post.hahaCount.toString(),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    // Quick comment trigger
                    IconButton(
                        onClick = {
                            viewModel.setCommentPost(post.id)
                            showAltComments = !showAltComments
                        },
                        modifier = Modifier.testTag("comments_trigger_${post.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ChatBubbleOutline, "Comment")
                            if (post.commentCount > 0) {
                                Text(
                                    post.commentCount.toString(),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${post.likesCount} connections",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Embedded Comments Section
            AnimatedVisibility(visible = showAltComments) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                    
                    // Comments list scroll
                    commentsList.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = comment.avatarUrl,
                                contentDescription = "commenter",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("@${comment.username}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(comment.content, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }

                    // Create comment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Write dynamic reply...", fontSize = 12.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.weight(1f).testTag("comment_input_post_${post.id}"),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.aiAnalyzeCommentToxicity(commentInput) }) {
                                    Icon(Icons.Filled.Security, "Check toxic status", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        )

                        IconButton(
                            onClick = {
                                if (commentInput.trim().isNotEmpty()) {
                                    viewModel.addComment(commentInput)
                                    commentInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}


// ========================
// DISCOVER / SEARCH SCREEN
// ========================
@Composable
fun DiscoverScreen(viewModel: SphereXViewModel) {
    var searchInput by remember { mutableStateOf("") }
    val followedUsers by viewModel.followedUsers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Advanced Custom Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { 
                    searchInput = it
                    viewModel.searchQuery.value = it
                },
                placeholder = { Text("Search Posts, communities, hashtags...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("global_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trending Hashtags
        Text("Trending Hashtags #", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("#cyberpunk", "#ar", "#synth", "#modular", "#future", "#gamedev").forEach { tag ->
                AssistChip(
                    onClick = {
                        searchInput = tag
                        viewModel.searchQuery.value = tag
                    },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggested Creators list (Instagram style)
        Text("Suggested Creator Nodes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        listOf(
            Triple("elena_vision", "Elena Vision", "https://images.unsplash.com/photo-1494790108377-be9c29b29330"),
            Triple("leo_beats", "Leo Beats", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"),
            Triple("zara_rebel", "Zara Rebel", "https://images.unsplash.com/photo-1517841905240-472988babdf9")
        ).forEach { (username, display, avatar) ->
            val isFollowed = followedUsers.contains(username)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = "Creator",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(display, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, "badge", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                            Text("@$username", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { viewModel.openChatWith(username) }) {
                            Icon(Icons.Filled.Chat, "chat to creator", tint = MaterialTheme.colorScheme.secondary)
                        }
                        Button(
                            onClick = { viewModel.toggleFollowUser(username) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isFollowed) "Following" else "Follow",
                                fontSize = 11.sp,
                                color = if (isFollowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}


// ========================
// TIKTOK REELS SECTION
// ========================
@Composable
fun ReelsScreen(viewModel: SphereXViewModel, reels: List<ReelEntity>, profile: UserProfile) {
    if (reels.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Gathering interactive Reels loops...", color = Color.White)
        }
        return
    }

    var selectedReelIndex by remember { mutableStateOf(0) }
    val reel = reels[selectedReelIndex % reels.size]

    // Continuous simulated vertical swiper container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        // Media representation mockup - Using Unsplash pictures stylized as blurred frames
        AsyncImage(
            model = reel.videoUrl,
            contentDescription = "Autoplay Reel Loop",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Custom Radial Overlay to look like a cinematic camera loop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 400f
                    )
                )
        )

        // Swiper buttons (Simulated gesture up and down)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { selectedReelIndex = (selectedReelIndex + reels.size - 1) % reels.size },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, "back", tint = Color.White)
            }
            IconButton(
                onClick = { selectedReelIndex = (selectedReelIndex + 1) % reels.size },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("next_reel_btn")
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, "next", tint = Color.White)
            }
        }

        // Creator Profile Detail Info + Music Track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = reel.avatarUrl,
                    contentDescription = "Creator",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    reel.displayName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Icon(Icons.Filled.Verified, "Badge", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }

            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.MusicNote, "music", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Text(
                    text = reel.musicTrack,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 10.sp
                )
            }
        }

        // Tipping Coins Bar directly in overlay! (Creator monetization action required)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.72f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.MonetizationOn, "Coins Balance", tint = AccentOrange, modifier = Modifier.size(16.dp))
                    Text("Your Coins: ${profile.coinBalance}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        10 to "10",
                        50 to "50",
                        100 to "100"
                    ).forEach { (amount, label) ->
                        Button(
                            onClick = { viewModel.tipCreatorCoins(reel.username, amount) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("tip_btn_${amount}")
                        ) {
                            Text("Tip $label 🪙", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Vertical action icons: Double Tap to Like, comment Count, share overlay
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { viewModel.likeReel(reel.id) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = if (reel.hasLiked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Like",
                    tint = if (reel.hasLiked) MaterialTheme.colorScheme.primary else Color.White
                )
            }
            Text("${reel.likesCount}", color = Color.White, fontSize = 11.sp)

            IconButton(
                onClick = { /* simulated sharing trigger alert */
                    viewModel.addNotification("COMMUNITY", "Link Shared", "Simulated system share profile triggered.")
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.Filled.Share, "Share", tint = Color.White)
            }
            Text("Share", color = Color.White, fontSize = 11.sp)

            // Auto-view loop incremental play simulated statistic count
            Icon(Icons.Filled.Visibility, "views", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            Text("${reel.playCount}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}


// ========================
// COMMUNITIES (GROUPS)
// ========================
@Composable
fun CommunitiesScreen(viewModel: SphereXViewModel, communities: List<CommunityEntity>) {
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }
    var showCreatorForm by remember { mutableStateOf(false) }

    val joinedGroups by viewModel.joinedCommunities.collectAsState()
    val coroutine = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explore Communities (Groups)", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Button(
                onClick = { showCreatorForm = !showCreatorForm },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (showCreatorForm) "Close Form" else "Create Group", fontSize = 11.sp)
            }
        }

        AnimatedVisibility(visible = showCreatorForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(value = newGroupName, onValueChange = { newGroupName = it }, label = { Text("Group Name") })
                    TextField(value = newGroupDesc, onValueChange = { newGroupDesc = it }, label = { Text("Short description") })
                    Button(
                        onClick = {
                            if (newGroupName.isNotEmpty()) {
                                coroutine.launch {
                                    viewModel.createCommunity(newGroupName, newGroupDesc, "https://images.unsplash.com/photo-1550745165-9bc0b252726f")
                                    newGroupName = ""
                                    newGroupDesc = ""
                                    showCreatorForm = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Construct Community Group")
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(communities) { community ->
                val hasJoined = joinedGroups.contains(community.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AsyncImage(
                            model = community.bannerUrl,
                            contentDescription = "group banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(community.groupName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Button(
                                    onClick = { viewModel.toggleJoinCommunity(community.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasJoined) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(if (hasJoined) "Joined ✓" else "Join Group", fontSize = 10.sp)
                                }
                            }

                            Text(community.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${community.memberCount + if (hasJoined) 1 else 0} active digital nodes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (community.isPrivate) "Private Forum" else "Public Space",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ========================
// MONETIZATION HUB
// ========================
@Composable
fun CreatorStudioScreen(viewModel: SphereXViewModel, profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Core coins status card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AccentOrange.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MonetizationOn, "Gold Coins", tint = AccentOrange, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("Active SphereX Coin Wallet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.coinBalance} Gold Coins", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AccentOrange)
                    }
                }

                // XP Progress Engine
                val currentLevel = profile.levelValue
                val currentXp = profile.xpPoints
                val neededXp = currentLevel * 300
                val progress = (currentXp.toFloat() / neededXp).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("XP Level $currentLevel Indicator", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("$currentXp / $neededXp XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Daily Check in bonus action
                Button(
                    onClick = { viewModel.claimDailyCheckInReward() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Decompress Daily Check-In Gift box (+150 Coins!)", fontSize = 11.sp)
                }
            }
        }

        // Custom Paint Canvas: Creator Earnings over time chart!
        Text("Earnings Analytics Track (7 Days)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Draw grid layout
                    val width = size.width
                    val height = size.height
                    val points = listOf(
                        Offset(0f, height * 0.8f),
                        Offset(width * 0.15f, height * 0.7f),
                        Offset(width * 0.33f, height * 0.4f),
                        Offset(width * 0.5f, height * 0.55f),
                        Offset(width * 0.66f, height * 0.3f),
                        Offset(width * 0.82f, height * 0.15f),
                        Offset(width, height * 0.2f)
                    )

                    // Outline grid helper steps
                    drawLine(Color.Gray.copy(alpha = 0.2f), Offset(0f, height * 0.5f), Offset(width, height * 0.5f))

                    val linePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = CyberCyan,
                        style = Stroke(width = 6f)
                    )

                    // Draw neon glowing indicator circles
                    points.forEach { pt ->
                        drawCircle(SparkPink, radius = 8f, center = pt)
                    }
                }
                Text("Monetization Growth Trend indicator", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd).padding(10.dp))
            }
        }

        // Coins Shop listing: Test checkout flows instantly
        Text("Purchase Coins (Simulated Test SandBox)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple(500, "Regular Bundle", "USD $4.99"),
                Triple(2500, "Super Value Chest", "USD $19.99")
            ).forEach { (amount, tag, price) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.testPurchaseCoins(amount, price) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tag, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("+$amount 🪙", fontWeight = FontWeight.Black, fontSize = 16.sp, color = AccentOrange)
                        Text(price, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ========================
// SECURITY DASHBOARD
// ========================
@Composable
fun SecurityDashboardScreen(viewModel: SphereXViewModel, profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Account Security Dashboard", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Two-Factor Authentication", fontWeight = FontWeight.Bold)
                        Text("Requires safe code '1234' on unrecognized portals", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = profile.twoFactorEnabled,
                        onCheckedChange = { viewModel.toggleTwoFactor(it) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ad Revenue Sharing Plan", fontWeight = FontWeight.Bold)
                        Text("Auto credit matching banner traffic into your purse", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = profile.adShareEnabled,
                        onCheckedChange = { /* enabled */ }
                    )
                }
            }
        }

        Text("Active Devices & Session states", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("Android emulator (Streaming Device) - Current", "Succeeded 2 minutes ago (Location: Seattle, WA)"),
                    Pair("Apple iPhone 14 Pro Max", "Logged out 2 days ago (Location: San Francisco, CA)"),
                    Pair("Google Chrome Node Desktop v120", "Active 10 hours ago (Location: Seattle, CA)")
                ).forEach { (dev, meta) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dev, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(meta, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Filled.Lock, "Lock Icon", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}


// ========================
// DIRECT CHATS (DMs)
// ========================
@Composable
fun ChatListScreen(viewModel: SphereXViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("DMs Conversations list", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        listOf(
            Triple("elena_vision", "Elena Vision", "Hey Alex! Loved your input on generative UI..."),
            Triple("leo_beats", "Leo Beats", "Yo Alex! Just sent you the new synth stems..."),
            Triple("zara_rebel", "Zara Rebel", "Spherical networks are incredible! Chat with you soon...")
        ).forEach { (uname, dname, msg) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openChatWith(uname) }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Filled.Person, "pic", modifier = Modifier.align(Alignment.Center))
                        }
                        Column {
                            Text(dname, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(msg, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Icon(Icons.Filled.KeyboardArrowRight, "open")
                }
            }
        }
    }
}

@Composable
fun ChatRoomScreen(viewModel: SphereXViewModel, recipient: String, messages: List<MessageEntity>) {
    var txtInput by remember { mutableStateOf("") }
    val coroutine = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header with Calling Actions!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = { viewModel.navigateTo("chats") }) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }
                Column {
                    Text(recipient.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("Secure Chat Node", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row {
                IconButton(onClick = { viewModel.startVoiceCall(recipient) }) {
                    Icon(Icons.Filled.Call, "Voice audio call", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.startVoiceCall(recipient) }) {
                    Icon(Icons.Filled.Videocam, "Video call", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Messages area scrollable
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMe = msg.senderUsername == "alex_sphere"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                )
                            )
                            .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.content,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            if (msg.type == "GIFT") {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .background(AccentOrange, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Creator Gift Verified 🪙", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create DM message text bar with smart quick action responses!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = txtInput,
                onValueChange = { txtInput = it },
                placeholder = { Text("Enter message safely...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("dm_composer_field")
            )

            IconButton(
                onClick = {
                    if (txtInput.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(txtInput)
                        txtInput = ""
                    }
                },
                modifier = Modifier.testTag("dm_send_btn")
            ) {
                Icon(Icons.Filled.Send, "Send message", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


// ========================
// ADMIN CONSOLE PANEL
// ========================
@Composable
fun AdminPanelScreen(viewModel: SphereXViewModel, posts: List<PostEntity>) {
    var adminActiveKeyUsersMap by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Admin Performance Metrics Index", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        // Graph with DAU Canvas painted diagram
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(0f, h * 0.9f)
                        quadraticTo(w * 0.4f, h * 0.8f, w * 0.62f, h * 0.35f)
                        lineTo(w, h * 0.1f)
                    }
                    drawPath(path, SparkPink, style = Stroke(width = 8f))
                }
                Text("Daily Active Nodes: 1.4M (Trend Up)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(10.dp))
            }
        }

        // Action triggers: approve verified profiles
        Text("Manage Creator verification applications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        listOf(
            "alex_sphere" to "Current Alex Profile Hub",
            "elena_vision" to "Elena Augmented assets application",
            "zara_rebel" to "Tokyo Drift video content verified request"
        ).forEach { (uname, desc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(uname, fontWeight = FontWeight.Bold)
                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { viewModel.adminApproveVerification(uname) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Approve badge", fontSize = 10.sp)
                    }
                }
            }
        }

        Text("Toxic comments reported triggers", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        posts.take(2).forEach { post ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("@${post.username} broadcast", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(post.content, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                    }
                    IconButton(onClick = { viewModel.adminDeletePost(post) }) {
                        Icon(Icons.Filled.Delete, "erase flagged content", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}


// ========================
// USER PROFILE SCREEN
// ========================
@Composable
fun ProfileScreen(viewModel: SphereXViewModel, profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cover Photo Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            AsyncImage(
                model = profile.coverUrl,
                contentDescription = "Cover photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic header config buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("security") },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.Settings, "security setup", tint = Color.White)
                }
                IconButton(
                    onClick = { viewModel.triggerLogout() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Filled.ExitToApp, "Logout", tint = Color.White)
                }
            }
        }

        // Profile details (Avatar and bio info overlaps cover gently)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-30).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "My Portrait",
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(profile.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (profile.isVerified) {
                        Icon(Icons.Filled.Verified, "Badge Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Text("@${profile.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Bio section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(profile.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

            Text("Location Tag: ${profile.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Website: ${profile.website}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { })

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "followers ${profile.followersCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "following ${profile.followingCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.navigateTo("edit_profile") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit SphereX bio & connections")
            }
        }
    }
}

@Composable
fun EditProfileScreen(viewModel: SphereXViewModel, profile: UserProfile) {
    var dName by remember { mutableStateOf(profile.displayName) }
    var bioStr by remember { mutableStateOf(profile.bio) }
    var loc by remember { mutableStateOf(profile.location) }
    var web by remember { mutableStateOf(profile.website) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Customize Public digital space", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        TextField(value = dName, onValueChange = { dName = it }, label = { Text("Display Name") })
        TextField(value = bioStr, onValueChange = { bioStr = it }, label = { Text("Bio description") })
        TextField(value = loc, onValueChange = { loc = it }, label = { Text("Physical Location tag") })
        TextField(value = web, onValueChange = { web = it }, label = { Text("Website linkage link") })

        Button(
            onClick = {
                viewModel.editDisplayName.value = dName
                viewModel.editBio.value = bioStr
                viewModel.editLocation.value = loc
                viewModel.editWebsite.value = web
                viewModel.saveUserProfileChanges()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publish Customizations")
        }
    }
}


// ========================
// STORY PLAYBACK SCREEN
// ========================
@Composable
fun StoryPlaybackOverlay(username: String, onDismiss: () -> Unit, viewModel: SphereXViewModel) {
    var storyReplyMessage by remember { mutableStateOf("") }
    val systemTime = remember { System.currentTimeMillis() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_playback_overlay")
    ) {
        // Main Story mock content based on selected user
        AsyncImage(
            model = if (username == "elena_vision") "https://images.unsplash.com/photo-1558655146-d09347e92766" else "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
            contentDescription = "Active story view",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Custom story gradient shade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        // Vertical Progress timer bar (simulated)
        var progress by remember { mutableStateOf(0f) }
        LaunchedEffect(key1 = true) {
            while (progress < 1.0f) {
                delay(100)
                progress += 0.02f
            }
            onDismiss()
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    )
                    Text("@$username active 24h Story", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Dismiss", tint = Color.White)
                }
            }
        }

        // Story interactions: Quick reaction buttons + Quick Reply DM input box!
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Preset inline responses reactions 👍 ❤️ 😂
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("👍", "💖", "😂", "😮", "🔥").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.42f), CircleShape)
                            .clickable {
                                viewModel.addNotification("LIKE", "Story interaction", "You reacted $emoji to @$username story!")
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 16.sp)
                    }
                }
            }

            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = storyReplyMessage,
                    onValueChange = { storyReplyMessage = it },
                    placeholder = { Text("Comment or reply privately to DM...", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (storyReplyMessage.isNotEmpty()) {
                            viewModel.sendChatMessage("Replied story: $storyReplyMessage")
                            onDismiss()
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Filled.Send, "Send DM Reply", tint = Color.Black)
                }
            }
        }
    }
}


// ========================
// REAL-TIME NOTIFICATION SHEET
// ========================
@Composable
fun NotificationOverlay(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    viewModel: SphereXViewModel
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .testTag("notification_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SphereX Live Alerts", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Clear", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable { viewModel.clearAllNotifications() }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No live alerts. Engage with creators!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (item.type) {
                                        "LIKE" -> Icons.Filled.Favorite
                                        "VIRTUAL_GIFT" -> Icons.Filled.MonetizationOn
                                        "BADGE" -> Icons.Filled.Verified
                                        else -> Icons.Filled.Public
                                    },
                                    contentDescription = "Alert",
                                    tint = if (item.type == "VIRTUAL_GIFT") AccentOrange else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Decompress Alerts Feed", fontSize = 12.sp)
                }
            }
        }
    }
}


// ========================
// GEMINI INTELLIGENT DRAWER
// ========================
@Composable
fun AiAssistantOverlay(viewModel: SphereXViewModel, onDismiss: () -> Unit) {
    var assistantPromptInput by remember { mutableStateOf("") }
    val aiGenerating by viewModel.aiGenerating.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .testTag("ai_assistant_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.AutoAwesome, "AI", tint = MaterialTheme.colorScheme.primary)
                        Text("Gemini AI Copilot", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "close")
                    }
                }

                Text(
                    text = "Generate viral captions, predicting trends, check toxicity, or request customized prompts instantly.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Render result scroll card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        if (aiGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = aiResult.ifEmpty { "AI Result Output: Set user prompt below or tap AI Suggest on composer text inputs to initiate dynamic parsing!" },
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = assistantPromptInput,
                        onValueChange = { assistantPromptInput = it },
                        placeholder = { Text("Ask Gemini Anything...", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        enabled = !aiGenerating,
                        onClick = {
                            if (assistantPromptInput.trim().isNotEmpty()) {
                                viewModel.queryGeneralAiAssistant(assistantPromptInput)
                                assistantPromptInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


// ========================
// VOICE & VIDEO CALL OVERLAY
// ========================
@Composable
fun CallScreenOverlay(participantName: String, viewModel: SphereXViewModel) {
    var callTimerValue by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            callTimerValue++
        }
    }

    val minutes = callTimerValue / 60
    val seconds = callTimerValue % 60
    val formattedTimer = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) { }
            .testTag("call_screen_overlay")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, "Participant profile picture", tint = Color.White, modifier = Modifier.size(56.dp))
            }

            Text(
                text = "Connected with @$participantName",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Voice + Interactive Stream Active • $formattedTimer",
                fontSize = 12.sp,
                color = AccentGreen,
                fontWeight = FontWeight.Medium
            )
        }

        // Floating Simulated Self Camera box (TikTok/Instagram style)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 120.dp, height = 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Filled.Videocam, "Self stream feedback", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(36.dp).align(Alignment.Center))
            Text("Self Feed", color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp))
        }

        // Call Control Buttons at Bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Mute mic",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.endActiveCall() },
                modifier = Modifier
                    .background(Color.Red, CircleShape)
                    .size(56.dp)
                    .testTag("end_call_btn")
            ) {
                Icon(Icons.Filled.CallEnd, "End active voice/video call session", tint = Color.White)
            }

            IconButton(
                onClick = { /* Simulated switch device alert */ },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.VolumeUp, "speaker", tint = Color.White)
            }
        }
    }
}
