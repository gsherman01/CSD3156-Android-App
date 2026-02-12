package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.entity.NotificationEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreNotification"

/**
 * Firestore implementation of RemoteNotificationRepository.
 */
class FirestoreNotificationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteNotificationRepository {

    private val notificationsCollection = firestore.collection("notifications")

    override suspend fun sendNotification(notification: NotificationEntity): Result<Unit> {
        return try {
            notificationsCollection.document(notification.id).set(notification).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationEntityDto::class.java)?.toEntity(doc.id)
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
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val referenceId: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
) {
    fun toEntity(docId: String) = NotificationEntity(
        id = docId, userId = userId, title = title, message = message,
        type = type, referenceId = referenceId, timestamp = timestamp, isRead = read
    )
}
