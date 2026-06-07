package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SphereXDao {
    // User Profile
    @Query("SELECT * FROM spherex_user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM spherex_user_profile LIMIT 1")
    suspend fun getUserProfileOneShot(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    // Posts Feed
    @Query("SELECT * FROM spherex_posts ORDER BY isPinned DESC, timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM spherex_posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    // Comments
    @Query("SELECT * FROM spherex_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM spherex_comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)

    // Reels
    @Query("SELECT * FROM spherex_reels ORDER BY id DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Update
    suspend fun updateReel(reel: ReelEntity)

    // Chat Messages
    @Query("SELECT * FROM spherex_messages WHERE (senderUsername = :userA AND recipientUsername = :userB) OR (senderUsername = :userB AND recipientUsername = :userA) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userA: String, userB: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Communities
    @Query("SELECT * FROM spherex_communities ORDER BY memberCount DESC")
    fun getAllCommunities(): Flow<List<CommunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: CommunityEntity)

    // Notifications
    @Query("SELECT * FROM spherex_notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE spherex_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM spherex_notifications")
    suspend fun clearAllNotifications()
}
