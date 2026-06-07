package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SphereXViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SphereXDatabase.getDatabase(application)
    val repository = SphereXRepository(db.dao)

    // Current navigation state: "auth", "feed", "discover", "reels", "chats", "communities", "monetization", "admin", "profile", "chat_room", "security", "edit_profile"
    private val _currentScreen = MutableStateFlow("auth")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Navigation sub-states
    private val _activeChatUser = MutableStateFlow<String?>("elena_vision")
    val activeChatUser: StateFlow<String?> = _activeChatUser.asStateFlow()

    private val _activePostCommentsId = MutableStateFlow<Int?>(1)
    val activePostCommentsId: StateFlow<Int?> = _activePostCommentsId.asStateFlow()

    private val _viewingUser = MutableStateFlow<String?>(null) // null means ourselves
    val viewingUser: StateFlow<String?> = _viewingUser.asStateFlow()

    // Auth screen states
    val loginUsername = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val sessionRegister = MutableStateFlow(false)
    val pin2FA = MutableStateFlow("")
    val is2FARequired = MutableStateFlow(false)
    val isAuthenticated = MutableStateFlow(false)
    val authError = MutableStateFlow("")

    // Profile Fields (for editing)
    val editDisplayName = MutableStateFlow("")
    val editBio = MutableStateFlow("")
    val editLocation = MutableStateFlow("")
    val editWebsite = MutableStateFlow("")

    // AI states
    val aiGenerating = MutableStateFlow(false)
    val aiResult = MutableStateFlow("")
    val aiPromptInput = MutableStateFlow("")

    // Filtering
    val selectedFeedFilter = MutableStateFlow("personalized") // personalized, trending, following, friends
    val searchQuery = MutableStateFlow("")

    // Interactive state helpers
    val pollVotes = MutableStateFlow<Map<Int, Int>>(emptyMap()) // map of post id to selected option index
    val joinedCommunities = MutableStateFlow<Set<Int>>(setOf(1)) // IDs of groups joined
    val blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val mutedUsers = MutableStateFlow<Set<String>>(emptySet())
    val followedUsers = MutableStateFlow<Set<String>>(setOf("elena_vision", "leo_beats"))
    val activeCallRoom = MutableStateFlow<String?>(null) // recipient username if call is active

    init {
        viewModelScope.launch {
            repository.populateMockDataIfNeeded()
            val p = repository.getUserProfileOneShot()
            editDisplayName.value = p.displayName
            editBio.value = p.bio
            editLocation.value = p.location
            editWebsite.value = p.website
        }
    }

    // Reactive database streams
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val rawPosts: StateFlow<List<PostEntity>> = repository.allPosts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered posts based on active search parameters and user feed settings
    val filteredPosts: StateFlow<List<PostEntity>> = combine(
        rawPosts,
        selectedFeedFilter,
        searchQuery,
        blockedUsers
    ) { posts, filter, query, blocked ->
        posts.filter { post ->
            if (blocked.contains(post.username)) return@filter false
            
            // Search query filter
            val matchesQuery = query.isEmpty() || 
                    post.content.contains(query, ignoreCase = true) || 
                    post.userDisplayName.contains(query, ignoreCase = true) ||
                    post.locationTag.contains(query, ignoreCase = true)

            if (!matchesQuery) return@filter false

            // Feed filter tab
            when (filter) {
                "trending" -> post.likesCount > 200
                "following" -> followedUsers.value.contains(post.username) || post.username == "alex_sphere"
                "friends" -> post.username == "elena_vision" || post.username == "leo_beats" || post.username == "alex_sphere"
                else -> true // personalized shows all
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReels: StateFlow<List<ReelEntity>> = repository.allReels.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allCommunities: StateFlow<List<CommunityEntity>> = repository.allCommunities.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Comments for active post
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeComments: StateFlow<List<CommentEntity>> = _activePostCommentsId.flatMapLatest { postId ->
        if (postId == null) flowOf(emptyList())
        else repository.getCommentsForPost(postId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat messages for active user
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<MessageEntity>> = _activeChatUser.flatMapLatest { recipient ->
        if (recipient == null) flowOf(emptyList())
        else repository.getMessagesBetweenUsers("alex_sphere", recipient)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun navigateToProfile(username: String?) {
        _viewingUser.value = username
        _currentScreen.value = "profile"
    }

    fun openChatWith(username: String) {
        _activeChatUser.value = username
        _currentScreen.value = "chat_room"
    }

    fun setCommentPost(postId: Int) {
        _activePostCommentsId.value = postId
    }

    // Authentication flow
    fun handleAuthSubmit() {
        val user = loginUsername.value.trim()
        val pass = loginPassword.value.trim()

        if (user.isEmpty() || pass.isEmpty()) {
            authError.value = "Username and password cannot be empty"
            return
        }

        if (sessionRegister.value) {
            // Register flow
            viewModelScope.launch {
                val newProfile = UserProfile(
                    username = user,
                    displayName = user.replaceFirstChar { it.uppercase() } + " Sphere",
                    bio = "Newly joined SphereX explorer! 🚀",
                    coinBalance = 1000,
                    levelValue = 1,
                    xpPoints = 100
                )
                repository.saveProfile(newProfile)
                isAuthenticated.value = true
                _currentScreen.value = "feed"
            }
        } else {
            // Login Verification flow
            if (user.lowercase() == "alex" || user.lowercase() == "alex_sphere") {
                viewModelScope.launch {
                    val p = repository.getUserProfileOneShot()
                    if (p.twoFactorEnabled) {
                        is2FARequired.value = true
                    } else {
                        isAuthenticated.value = true
                        _currentScreen.value = "feed"
                    }
                }
            } else {
                // Mock user auto registration/login
                viewModelScope.launch {
                    val newProfile = UserProfile(
                        username = user,
                        displayName = user.replaceFirstChar { it.uppercase() },
                        bio = "SphereX explorer. ✨",
                        coinBalance = 500,
                        levelValue = 1,
                        xpPoints = 50
                    )
                    repository.saveProfile(newProfile)
                    isAuthenticated.value = true
                    _currentScreen.value = "feed"
                }
            }
        }
    }

    fun handleTwoFactorVerify() {
        if (pin2FA.value == "1234") {
            isAuthenticated.value = true
            is2FARequired.value = false
            _currentScreen.value = "feed"
        } else {
            authError.value = "Invalid 2FA code. Use default '1234' to authenticate."
        }
    }

    fun triggerLogout() {
        isAuthenticated.value = false
        loginUsername.value = ""
        loginPassword.value = ""
        authError.value = ""
        is2FARequired.value = false
        _currentScreen.value = "auth"
    }

    // Profile Settings updates
    fun saveUserProfileChanges() {
        viewModelScope.launch {
            val original = repository.getUserProfileOneShot()
            val updated = original.copy(
                displayName = editDisplayName.value,
                bio = editBio.value,
                location = editLocation.value,
                website = editWebsite.value
            )
            repository.saveProfile(updated)
            addNotification("BADGE", "Profile Updated", "Your SphereX digital identity has been saved successfully.")
            _currentScreen.value = "profile"
        }
    }

    fun toggleTwoFactor(enabled: Boolean) {
        viewModelScope.launch {
            val original = repository.getUserProfileOneShot()
            repository.saveProfile(original.copy(twoFactorEnabled = enabled))
            val action = if (enabled) "enabled" else "disabled"
            addNotification("BADGE", "Security Settings Change", "Two-Factor Authentication (2FA) is now $action.")
        }
    }

    // Feed Interactions
    fun reactToPost(postId: Int, reactionType: String) {
        viewModelScope.launch {
            val post = repository.getPostById(postId) ?: return@launch
            var likedState = post.hasLiked
            var currentLikes = post.likesCount
            var love = post.loveCount
            var haha = post.hahaCount
            var sad = post.sadCount
            var angry = post.angryCount

            when (reactionType) {
                "LIKE" -> {
                    if (likedState) {
                        currentLikes = (currentLikes - 1).coerceAtLeast(0)
                        likedState = false
                    } else {
                        currentLikes += 1
                        likedState = true
                    }
                }
                "LOVE" -> love += 1
                "HAHA" -> haha += 1
                "SAD" -> sad += 1
                "ANGRY" -> angry += 1
            }

            val updatedPost = post.copy(
                likesCount = currentLikes,
                loveCount = love,
                hahaCount = haha,
                sadCount = sad,
                angryCount = angry,
                hasLiked = likedState
            )
            repository.updatePost(updatedPost)
            
            // XP check
            addXp(15)
        }
    }

    fun submitPost(content: String, postType: String, postMedia: String = "", location: String = "") {
        viewModelScope.launch {
            val currentProfile = repository.getUserProfileOneShot()
            val newPost = PostEntity(
                username = currentProfile.username,
                userDisplayName = currentProfile.displayName,
                userAvatarUrl = currentProfile.avatarUrl,
                content = content,
                type = postType,
                mediaUrl = postMedia,
                locationTag = location
            )
            repository.insertPost(newPost)
            addXp(50)
            addNotification("COMMUNITY", "Post Created", "Your new ${postType.lowercase()} is active.")
        }
    }

    fun submitPollPost(content: String, pollOptionsCombined: String) {
        viewModelScope.launch {
            val currentProfile = repository.getUserProfileOneShot()
            val newPost = PostEntity(
                username = currentProfile.username,
                userDisplayName = currentProfile.displayName,
                userAvatarUrl = currentProfile.avatarUrl,
                content = content,
                type = "POLL",
                pollOptions = pollOptionsCombined
            )
            repository.insertPost(newPost)
            addXp(55)
        }
    }

    fun castVote(postId: Int, optionIndex: Int) {
        val currentVotes = pollVotes.value.toMutableMap()
        currentVotes[postId] = optionIndex
        pollVotes.value = currentVotes
        
        viewModelScope.launch {
            val post = repository.getPostById(postId) ?: return@launch
            repository.updatePost(post.copy(likesCount = post.likesCount + 1))
            addXp(10)
        }
    }

    // Communities Interactions
    fun toggleJoinCommunity(id: Int) {
        val currentSet = joinedCommunities.value.toMutableSet()
        if (currentSet.contains(id)) {
            currentSet.remove(id)
        } else {
            currentSet.add(id)
        }
        joinedCommunities.value = currentSet
        viewModelScope.launch {
            addXp(20)
        }
    }

    suspend fun createCommunity(name: String, description: String, banner: String) {
        val group = CommunityEntity(
            groupName = name,
            description = description,
            bannerUrl = banner
        )
        repository.insertCommunity(group)
        addXp(100)
    }

    // Reels Interactivity
    fun likeReel(reelId: Int) {
        viewModelScope.launch {
            // Locate local reels
            // For mock purposes, just bump the list
            val current = allReels.value
            val target = current.find { it.id == reelId } ?: return@launch
            val updated = target.copy(
                likesCount = if (target.hasLiked) target.likesCount - 1 else target.likesCount + 1,
                hasLiked = !target.hasLiked
            )
            repository.updateReel(updated)
            addXp(15)
        }
    }

    fun tipCreatorCoins(creatorUsername: String, coinsAmount: Int) {
        viewModelScope.launch {
            val currentProfile = repository.getUserProfileOneShot()
            if (currentProfile.coinBalance < coinsAmount) {
                // Not enough coins context error
                addNotification("VIRTUAL_GIFT", "Coin OutOfBounds error", "Not enough coins. Tip simulated but profile balance unchanged.")
                return@launch
            }
            // Update profile
            val updatedProfile = currentProfile.copy(coinBalance = currentProfile.coinBalance - coinsAmount)
            repository.saveProfile(updatedProfile)

            // Dynamic tipping notification
            addNotification("VIRTUAL_GIFT", "Tipped Creator", "You tipped $coinsAmount coins to @$creatorUsername!")
            addXp(coinsAmount * 2)

            // Save tipped message to creator chat logs
            val messageText = "🎁 Tipped Creator: $coinsAmount coins! Thanks for your incredible content! ✨"
            repository.insertMessage(MessageEntity(
                senderUsername = "alex_sphere",
                recipientUsername = creatorUsername,
                content = messageText,
                type = "GIFT"
            ))
            
            // Auto response simulation
            triggerAutoResponse(creatorUsername, coinsAmount)
        }
    }

    // Comments actions
    fun addComment(content: String) {
        val postId = _activePostCommentsId.value ?: return
        viewModelScope.launch {
            val profile = repository.getUserProfileOneShot()
            val comment = CommentEntity(
                postId = postId,
                username = profile.username,
                displayName = profile.displayName,
                avatarUrl = profile.avatarUrl,
                content = content
            )
            repository.insertComment(comment)

            val post = repository.getPostById(postId)
            if (post != null) {
                repository.updatePost(post.copy(commentCount = post.commentCount + 1))
            }
            addXp(15)
        }
    }

    fun removeComment(commentId: Int) {
        val postId = _activePostCommentsId.value ?: return
        viewModelScope.launch {
            repository.deleteComment(commentId)
            val post = repository.getPostById(postId)
            if (post != null) {
                repository.updatePost(post.copy(commentCount = (post.commentCount - 1).coerceAtLeast(0)))
            }
        }
    }

    // Direct Messages chat actions
    fun sendChatMessage(content: String, attachmentType: String = "TEXT", attachmentUrl: String = "") {
        val recipient = _activeChatUser.value ?: return
        viewModelScope.launch {
            val message = MessageEntity(
                senderUsername = "alex_sphere",
                recipientUsername = recipient,
                content = content,
                type = attachmentType,
                mediaUrl = attachmentUrl
            )
            repository.insertMessage(message)
            addXp(5)

            // Smart Auto response trigger
            val triggerText = content.lowercase()
            delay(1500)
            val replyText = if (triggerText.contains("stream") || triggerText.contains("live")) {
                "Awesome! Connected the host key. I'll invite you to the dual feed as soon as the virtual canvas is live."
            } else if (triggerText.contains("synth") || triggerText.contains("beats") || triggerText.contains("music")) {
                "Awesome loop, Alex! Added some stereo spatial widening to the synthesizers. Let me know if that hits nicely!"
            } else if (triggerText.contains("hello") || triggerText.contains("hi") || triggerText.contains("hey")) {
                "Hey! Welcome back Alex. Checked out your recent user profile update, looks super clean!"
            } else {
                "Spherical network models are incredible. Thanks for checking in. Chat with you super soon!"
            }

            repository.insertMessage(MessageEntity(
                senderUsername = recipient,
                recipientUsername = "alex_sphere",
                content = replyText
            ))
            
            // Add notification
            addNotification("LIKE" /* DM style badge */, "Message Received", "@$recipient replied to you in DMs.")
        }
    }

    private fun triggerAutoResponse(creator: String, tippedCoins: Int) {
        viewModelScope.launch {
            delay(2000)
            val reply = "Wow!! Oh my god, thank you SO much for the $tippedCoins golden coins, Alex! 💖 This really fuels my daily generative designs and beats."
            repository.insertMessage(MessageEntity(
                senderUsername = creator,
                recipientUsername = "alex_sphere",
                content = reply
            ))
        }
    }

    // Voice & Video Call actions
    fun startVoiceCall(username: String) {
        activeCallRoom.value = username
        addNotification("COMMUNITY", "Call Initiated", "Calling $username...")
    }

    fun endActiveCall() {
        activeCallRoom.value = null
    }

    // Gamification Gamestate
    private suspend fun addXp(amount: Int) {
        val original = repository.getUserProfileOneShot()
        var newXp = original.xpPoints + amount
        var currLevel = original.levelValue
        val neededXp = currLevel * 300

        if (newXp >= neededXp) {
            newXp -= neededXp
            currLevel += 1
            addNotification(
                "BADGE", 
                "Level Achieved!", 
                "You reached Level $currLevel! New exclusive SphereX themes unlocked."
            )
        }

        repository.saveProfile(original.copy(
            xpPoints = newXp,
            levelValue = currLevel
        ))
    }

    fun claimDailyCheckInReward() {
        viewModelScope.launch {
            val original = repository.getUserProfileOneShot()
            val rewardCoins = 150
            repository.saveProfile(original.copy(
                coinBalance = original.coinBalance + rewardCoins
            ))
            addXp(100)
            addNotification("VIRTUAL_GIFT", "Daily Chest Unlocked", "Claimed 150 SphereX Coins daily login gift! 🎁")
        }
    }

    fun testPurchaseCoins(amount: Int, priceText: String) {
        viewModelScope.launch {
            val p = repository.getUserProfileOneShot()
            repository.saveProfile(p.copy(coinBalance = p.coinBalance + amount))
            addNotification("VIRTUAL_GIFT", "Purchase Complete", "Simulated billing profile $priceText succeeded. Added $amount coins!")
            addXp(amount / 2)
        }
    }

    // Administrative Moderation Controls & verification approvals
    fun adminDeletePost(post: PostEntity) {
        viewModelScope.launch {
            repository.deletePost(post)
            addNotification("COMMUNITY", "System Cleanup", "Admin deleted flagged post # ${post.id}.")
        }
    }

    fun adminApproveVerification(username: String) {
        viewModelScope.launch {
            // Check if current user
            if (username == "alex_sphere") {
                val origin = repository.getUserProfileOneShot()
                repository.saveProfile(origin.copy(isVerified = true))
                addNotification("BADGE", "Verified Creator Badge", "Congratulations! Your SphereX Verification is approved!")
            } else {
                // Preloaded user, trigger notification
                addNotification("BADGE", "Global Action", "Verification requested index Approved.")
            }
        }
    }

    // Following blocking muting list managers
    fun toggleFollowUser(username: String) {
        val currentSet = followedUsers.value.toMutableSet()
        if (currentSet.contains(username)) {
            currentSet.remove(username)
        } else {
            currentSet.add(username)
        }
        followedUsers.value = currentSet
        viewModelScope.launch {
            addXp(25)
        }
    }

    fun toggleBlockUser(username: String) {
        val currentSet = blockedUsers.value.toMutableSet()
        if (currentSet.contains(username)) {
            currentSet.remove(username)
        } else {
            currentSet.add(username)
        }
        blockedUsers.value = currentSet
    }

    fun toggleMuteUser(username: String) {
        val currentSet = mutedUsers.value.toMutableSet()
        if (currentSet.contains(username)) {
            currentSet.remove(username)
        } else {
            currentSet.add(username)
        }
        mutedUsers.value = currentSet
    }

    // System Notifications triggers
    fun addNotification(type: String, title: String, description: String) {
        viewModelScope.launch {
            repository.insertNotification(NotificationEntity(
                type = type,
                title = title,
                description = description
            ))
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // --- Gemini AI Assistant Sidebar Operations ---

    fun generateAiCaption(postContent: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            aiResult.value = ""
            val prompt = "Based on this text concept: \"$postContent\", generate 3 highly expressive and engaging alternative captions for a social media platform called SphereX. Include clever hashtags, spaced emojis, and viral appeal. Keep the response neat."
            val system = "You are a professional social media manager and growth strategist for SphereX - a high-tech creator network. Respond with the generated captions formatted cleanly as bullet points."
            val res = GeminiApiHelper.generateContent(prompt, system)
            aiResult.value = res
            aiGenerating.value = false
        }
    }

    fun generateAiHashtags(postContent: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            aiResult.value = ""
            val prompt = "Generate a block of 10 relevant trending and viral hashtags for this post: \"$postContent\"."
            val system = "Provide only the hashtags, spaced neatly by spaces. Do not write introductory words or conversational sentences."
            val res = GeminiApiHelper.generateContent(prompt, system)
            aiResult.value = res
            aiGenerating.value = false
        }
    }

    fun aiAnalyzeCommentToxicity(commentText: String) {
        viewModelScope.launch {
            aiGenerating.value = true
            aiResult.value = ""
            val prompt = "Analyze the toxicity levels of this social comment: \"$commentText\". Rate it from 0 to 100% on potential spam, hate speech, or toxic metrics. Provide a brief verdict of whether this should be flagged or approved."
            val system = "You are an automated SphereX AI Content Moderator. Respond with JSON-style format or crisp analytical bullet points indicating Toxicity level and Verdict."
            val res = GeminiApiHelper.generateContent(prompt, system)
            aiResult.value = res
            aiGenerating.value = false
        }
    }

    fun queryGeneralAiAssistant(userPrompt: String) {
        if (userPrompt.trim().isEmpty()) return
        viewModelScope.launch {
            aiGenerating.value = true
            aiResult.value = ""
            aiPromptInput.value = ""
            val res = GeminiApiHelper.generateContent(userPrompt, "You are SphereX's built-in intelligent companion helper. Guide the developer with modern advice.")
            aiResult.value = res
            aiGenerating.value = false
        }
    }
}
