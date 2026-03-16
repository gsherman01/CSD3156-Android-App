package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.entity.NotificationEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreNotification"

/**
 * Firestore implementation of RemoteNotificationRepository.
 * [FIXED]: Synchronized field names and added 'id' field for mapping compatibility.
 */
class FirestoreNotificationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteNotificationRepository {

    private val notificationsCollection = firestore.collection("notifications")

    override suspend fun sendNotification(notification: NotificationEntity): Result<Unit> {
        return try {
            val dto = NotificationEntityDto(
                id = notification.id, // Ensure ID is passed to cloud doc
                userId = notification.userId,
                title = notification.title,
                message = notification.message,
                type = notification.type,
                referenceId = notification.referenceId,
                timestamp = notification.timestamp,
                isRead = notification.isRead
            )
            Log.d(TAG, "OFFER_DEBUG: Pushing notification ${notification.id} to Cloud...")
            notificationsCollection.document(notification.id).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "OFFER_DEBUG: Cloud notification failed: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    // doc.id is used to populate the 'id' field of the DTO
                    doc.toObject(NotificationEntityDto::class.java)?.apply { id = doc.id }?.toEntity()
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }
}

/**
 * Internal DTO for Firestore compatibility.
 */
data class NotificationEntityDto(
    var id: String = "", // [FIXED]: Added id field for CustomClassMapper compatibility
    var userId: String = "",
    var title: String = "",
    var message: String = "",
    var type: String = "",
    var referenceId: String = "",
    var timestamp: Long = 0L,
    
    @get:PropertyName("read") @set:PropertyName("read")
    var isRead: Boolean = false
) {
    fun toEntity() = NotificationEntity(
        id = id, userId = userId, title = title, message = message,
        type = type, referenceId = referenceId, timestamp = timestamp, isRead = isRead
    )
}
