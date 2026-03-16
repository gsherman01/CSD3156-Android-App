package com.example.tinycell.data.repository

import com.example.tinycell.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Remote Notification operations (Firestore).
 */
interface RemoteNotificationRepository {
    /**
     * Pushes a notification to the cloud for a specific user.
     */
    suspend fun sendNotification(notification: NotificationEntity): Result<Unit>

    /**
     * Listens for real-time notifications for a specific user.
     */
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>>
}
