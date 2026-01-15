package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tinycell.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ChatMessage table.
 *
 * Manages chat messages between users about specific listings.
 * Supports bidirectional conversations and unread message tracking.
 */
@Dao
interface ChatMessageDao {

    /**
     * Insert a new chat message.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    /**
     * Insert multiple messages (for syncing from server).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    /**
     * Get conversation between two users (bidirectional).
     *
     * Returns all messages where either:
     * - user1 sent to user2, OR
     * - user2 sent to user1
     *
     * Ordered chronologically (oldest first) for chat display.
     *
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Flow of messages between the two users
     */
    @Query("""
        SELECT * FROM chat_messages
        WHERE (senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1)
        ORDER BY timestamp ASC
    """)
    fun getConversation(userId1: String, userId2: String): Flow<List<ChatMessageEntity>>

    /**
     * Get conversation about a specific listing between two users.
     *
     * More specific than getConversation - filters by listing too.
     * Useful when users have multiple listings and multiple conversations.
     *
     * @param listingId The listing being discussed
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Flow of messages about this listing between the users
     */
    @Query("""
        SELECT * FROM chat_messages
        WHERE listingId = :listingId
          AND ((senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1))
        ORDER BY timestamp ASC
    """)
    fun getListingConversation(
        listingId: String,
        userId1: String,
        userId2: String
    ): Flow<List<ChatMessageEntity>>

    /**
     * Get all messages for a specific listing (any participants).
     * Useful for listing owner to see all inquiries.
     */
    @Query("SELECT * FROM chat_messages WHERE listingId = :listingId ORDER BY timestamp ASC")
    fun getMessagesForListing(listingId: String): Flow<List<ChatMessageEntity>>

    /**
     * Get all unread messages for a user.
     */
    @Query("SELECT * FROM chat_messages WHERE receiverId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadMessages(userId: String): Flow<List<ChatMessageEntity>>

    /**
     * Get count of unread messages for a user (for notification badge).
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    /**
     * Mark a specific message as read.
     */
    @Query("UPDATE chat_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    /**
     * Mark all messages from a specific sender as read.
     * Useful when opening a conversation.
     */
    @Query("UPDATE chat_messages SET isRead = 1 WHERE senderId = :senderId AND receiverId = :receiverId AND isRead = 0")
    suspend fun markConversationAsRead(senderId: String, receiverId: String)

    /**
     * Mark all unread messages for a user as read.
     */
    @Query("UPDATE chat_messages SET isRead = 1 WHERE receiverId = :userId AND isRead = 0")
    suspend fun markAllAsRead(userId: String)

    /**
     * Delete a specific message.
     */
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    /**
     * Delete all messages in a conversation between two users.
     */
    @Query("""
        DELETE FROM chat_messages
        WHERE (senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1)
    """)
    suspend fun deleteConversation(userId1: String, userId2: String)

    /**
     * Delete all messages (useful for testing/reset).
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
