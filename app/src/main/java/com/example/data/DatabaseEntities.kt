package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spherex_user_profile")
data class UserProfile(
    @PrimaryKey val username: String = "alex_sphere",
    val displayName: String = "Alex SphereX",
    val bio: String = "Creative developer crafting future aesthetics on SphereX. 🌟 @deepmind",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
    val coverUrl: String = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809",
    val website: String = "https://spherex.io/alex",
    val location: String = "San Francisco, CA",
    val interests: String = "Design, AI, Jetpack Compose, LiveStreams",
    val isVerified: Boolean = true,
    val followersCount: Int = 14205,
    val followingCount: Int = 384,
    val xpPoints: Int = 450,
    val levelValue: Int = 5,
    val coinBalance: Int = 2450,
    val adShareEnabled: Boolean = true,
    val twoFactorEnabled: Boolean = false,
    val emailVerified: Boolean = true,
    val phoneVerified: Boolean = true
)

@Entity(tableName = "spherex_posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val content: String,
    val type: String, // TEXT, IMAGE, POLL, EVENT
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val loveCount: Int = 0,
    val hahaCount: Int = 0,
    val sadCount: Int = 0,
    val angryCount: Int = 0,
    val commentCount: Int = 0,
    val hasLiked: Boolean = false,
    val locationTag: String = "",
    val isPinned: Boolean = false,
    val pollOptions: String = "" // OptionA|OptionB|OptionC format
)

@Entity(tableName = "spherex_reels")
data class ReelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val videoUrl: String = "",
    val caption: String,
    val musicTrack: String = "Original Audio - SphereX Beats",
    val likesCount: Int = 1240,
    val commentCount: Int = 84,
    val playCount: Int = 45000,
    val coinCost: Int = 0,
    val hasLiked: Boolean = false
)

@Entity(tableName = "spherex_comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isPinned: Boolean = false,
    val parentCommentId: Int = 0
)

@Entity(tableName = "spherex_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUsername: String,
    val recipientUsername: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "TEXT", // TEXT, IMAGE, AUDIO, GIFT
    val mediaUrl: String = "",
    val isRead: Boolean = false,
    val reactions: String = ""
)

@Entity(tableName = "spherex_communities")
data class CommunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val description: String,
    val bannerUrl: String,
    val memberCount: Int = 12,
    val isPrivate: Boolean = false,
    val rules: String = "1. Be respectful\n2. No spam\n3. Enjoy creative design."
)

@Entity(tableName = "spherex_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // LIKE, COMMENT, COMMUNITY, VIRTUAL_GIFT, BADGE
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
