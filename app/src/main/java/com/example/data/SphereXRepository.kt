package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SphereXRepository(private val dao: SphereXDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allPosts: Flow<List<PostEntity>> = dao.getAllPosts()
    val allReels: Flow<List<ReelEntity>> = dao.getAllReels()
    val allCommunities: Flow<List<CommunityEntity>> = dao.getAllCommunities()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getNotifications()

    suspend fun getUserProfileOneShot(): UserProfile {
        return dao.getUserProfileOneShot() ?: run {
            val newUser = UserProfile()
            dao.insertUserProfile(newUser)
            newUser
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun insertPost(post: PostEntity) {
        dao.insertPost(post)
    }

    suspend fun getPostById(postId: Int): PostEntity? {
        return dao.getPostById(postId)
    }

    suspend fun updatePost(post: PostEntity) {
        dao.updatePost(post)
    }

    suspend fun deletePost(post: PostEntity) {
        dao.deletePost(post)
    }

    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>> = dao.getCommentsForPost(postId)

    suspend fun insertComment(comment: CommentEntity) {
        dao.insertComment(comment)
    }

    suspend fun deleteComment(commentId: Int) {
        dao.deleteComment(commentId)
    }

    suspend fun insertReel(reel: ReelEntity) {
        dao.insertReel(reel)
    }

    suspend fun updateReel(reel: ReelEntity) {
        dao.updateReel(reel)
    }

    fun getMessagesBetweenUsers(userA: String, userB: String): Flow<List<MessageEntity>> =
        dao.getMessagesBetweenUsers(userA, userB)

    suspend fun insertMessage(message: MessageEntity) {
        dao.insertMessage(message)
    }

    suspend fun insertCommunity(community: CommunityEntity) {
        dao.insertCommunity(community)
    }

    suspend fun insertNotification(notification: NotificationEntity) {
        dao.insertNotification(notification)
    }

    suspend fun clearNotifications() {
        dao.clearAllNotifications()
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun populateMockDataIfNeeded() {
        val existingProfile = dao.getUserProfileOneShot()
        if (existingProfile == null) {
            // 1. User
            dao.insertUserProfile(UserProfile())

            // 2. Initial Posts
            dao.insertPost(PostEntity(
                username = "elena_vision",
                userDisplayName = "Elena Vision",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                content = "Just designed a new neural augmented reality workspace concept in WebGL! What do you hackers think about shifting from flat phone layouts to immersive 3D spherical interfaces? #ar #future #metaverse",
                type = "IMAGE",
                mediaUrl = "https://images.unsplash.com/photo-1558655146-d09347e92766",
                likesCount = 842,
                loveCount = 203,
                hahaCount = 2,
                commentCount = 14,
                locationTag = "MetaLabs VR, San Francisco",
                isPinned = true
            ))

            dao.insertPost(PostEntity(
                username = "leo_beats",
                userDisplayName = "Leo Beats",
                userAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                content = "Just compiled my new Cyberpunk Chillhop EP tracks. Which art direction fits the vibe better? Option A: Neon Glitch, Option B: Retro Synthwave Sunset.",
                type = "POLL",
                pollOptions = "Option A (Neon Glitch)|Option B (Retro Sunset)",
                likesCount = 192,
                loveCount = 45,
                commentCount = 9,
                locationTag = "Synth City Studio"
            ))

            dao.insertPost(PostEntity(
                username = "zara_rebel",
                userDisplayName = "Zara Rebel",
                userAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
                content = "Riding past the futuristic neon signs of Tokyo tonight. The city really comes alive under the virtual raindrops. ✨🚕 #cyberpunk #wanderlust",
                type = "IMAGE",
                mediaUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26",
                likesCount = 712,
                hahaCount = 14,
                commentCount = 28,
                locationTag = "Shibuya, Tokyo"
            ))

            dao.insertPost(PostEntity(
                username = "spherex_hq",
                userDisplayName = "SphereX HQ",
                userAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe",
                content = "Welcome to SphereX beta! 🎉 Connect with creators globally, watch full-screen reels, send virtual tip coins, and test our built-in Gemini Assistant in the sidebar chat drawer! Let us know how the flow feels.",
                type = "TEXT",
                likesCount = 12500,
                commentCount = 1530
            ))

            // 3. Comments for Post 1 (Elena's post)
            dao.insertComment(CommentEntity(
                postId = 1,
                username = "leo_beats",
                displayName = "Leo Beats",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                content = "This looks crazy polished Elena! The neon glowing dials are gorgeous. Need some custom lofi futuristic synth tracks for the background?",
                likesCount = 45
            ))
            dao.insertComment(CommentEntity(
                postId = 1,
                username = "alex_sphere",
                displayName = "Alex SphereX",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                content = "I absolutely love this! The spatial user density is perfectly laid out. Can we try this out on a custom device?",
                likesCount = 12,
                isPinned = true
            ))

            // 4. Reels
            dao.insertReel(ReelEntity(
                username = "elena_vision",
                displayName = "Elena Vision",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                videoUrl = "https://images.unsplash.com/photo-1549490349-8643362247b5",
                caption = "Creating neon light effects utilizing custom shader properties. The visual density looks incredible! #reels #graphics #gamedev",
                musicTrack = "Digital Dreaming - Elena V",
                likesCount = 5904,
                commentCount = 120,
                playCount = 145000
            ))

            dao.insertReel(ReelEntity(
                username = "zara_rebel",
                displayName = "Zara Rebel",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9",
                videoUrl = "https://images.unsplash.com/photo-1504608524841-42fe6f032b4b",
                caption = "Chasing virtual views. Cyber Tokyo loop is fully online! ⚡🏎️ #reels #motorsport #neotokyo",
                musicTrack = "Phonk Drift Overdrive - Synth Syndicate",
                likesCount = 12435,
                commentCount = 455,
                playCount = 384000
            ))

            dao.insertReel(ReelEntity(
                username = "leo_beats",
                displayName = "Leo Beats",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                videoUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
                caption = "Live modular synth performance for my upcoming EP. Tweaking the resonance to create optimal brainwave synchronization! 🧠🎛️ #synth #modular #modularpatch",
                musicTrack = "Infinite Resonance - Leo Beats",
                likesCount = 4590,
                commentCount = 98,
                playCount = 82000
            ))

            // 5. Communities
            dao.insertCommunity(CommunityEntity(
                groupName = "Neon Design Guild",
                description = "Discussions and showcases of highly polished cyberpunk, brutalist and high-tech user interface designs.",
                bannerUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f",
                memberCount = 1420
            ))
            dao.insertCommunity(CommunityEntity(
                groupName = "SphereX Indie Hackers",
                description = "For creators monetizing digital feeds, custom widgets, sub-groups and NFT badges on SphereX.",
                bannerUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c",
                memberCount = 890
            ))
            dao.insertCommunity(CommunityEntity(
                groupName = "Gemini Alchemy Labs",
                description = "Connecting automated prompts, automated code flows, and AI agents directly with local data stores.",
                bannerUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe",
                memberCount = 412,
                isPrivate = true
            ))

            // 6. Notifications
            dao.insertNotification(NotificationEntity(
                type = "LIKE",
                title = "Like Alert",
                description = "elena_vision and 14 others liked your comment in Neon Design Guild."
            ))
            dao.insertNotification(NotificationEntity(
                type = "VIRTUAL_GIFT",
                title = "Coins Tip Received!",
                description = "leo_beats tipped you 200 Creator Coins for your post guidance."
            ))
            dao.insertNotification(NotificationEntity(
                type = "BADGE",
                title = "Level Up!",
                description = "Congratulations Alex! You reached Creator Level 5: 'Trendsetter'. +10% content boosting!"
            ))

            // 7. Conversational Messages
            dao.insertMessage(MessageEntity(
                senderUsername = "elena_vision",
                recipientUsername = "alex_sphere",
                content = "Hey Alex! Loved your input on the generative UI post. I'm going live in 10 minutes to code up the shader simulation. Are you free to hop on as a multi-guest stream guest?"
            ))
            dao.insertMessage(MessageEntity(
                senderUsername = "alex_sphere",
                recipientUsername = "elena_vision",
                content = "Wow Elena! That sounds incredibly fun. Let me prep my camera setup and I will hop on right away. ⚡"
            ))
            dao.insertMessage(MessageEntity(
                senderUsername = "elena_vision",
                recipientUsername = "alex_sphere",
                content = "Amazing! I will send the guest invite link as soon as I connect the stream."
            ))

            dao.insertMessage(MessageEntity(
                senderUsername = "leo_beats",
                recipientUsername = "alex_sphere",
                content = "Yo Alex! Just sent you the new synth stem loop assets in your mailbox. Let me know if the low-mid frequencies sound good on your headphones."
            ))
        }
    }
}
