package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "NotificationRepo"
        private const val COLLECTION = "notifications"
    }

    // ===== SUSPEND FUNCTIONS =====

    suspend fun getNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val notifications = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(notificationId = doc.id)
            }

            Result.success(notifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Log.e(TAG, "Error counting unread notifications", e)
            Result.failure(e)
        }
    }

    // Digunakan untuk broadcast ke banyak user sekaligus
    suspend fun addNotifications(notifications: List<Notification>): Result<List<Notification>> {
        return try {
            val batch = firestore.batch()
            val results = mutableListOf<Notification>()

            notifications.forEach { notification ->
                val ref = firestore.collection(COLLECTION).document()
                val newNotification = notification.copy(notificationId = ref.id)
                batch.set(ref, newNotification)
                results.add(newNotification)
            }

            batch.commit().await()
            Log.d(TAG, "Added ${results.size} notifications")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding notifications", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.success(Unit)
            }

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Log.d(TAG, "Marked all notifications as read for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all as read", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION)
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
            Result.failure(e)
        }
    }

    // ===== REAL-TIME LISTENERS =====

    fun observeNotifications(
        userId: String,
        onUpdate: (List<Notification>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        Log.d(TAG, "Starting real-time listener for user: $userId")

        return firestore.collection(COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listener error", error)
                    onError(error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val notifications = it.documents.mapNotNull { doc ->
                        doc.toObject(Notification::class.java)?.copy(notificationId = doc.id)
                    }
                    Log.d(TAG, "Real-time update: ${notifications.size} notifications")
                    onUpdate(notifications)
                }
            }
    }

    fun observeUnreadCount(
        userId: String,
        onUpdate: (Int) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        Log.d(TAG, "Starting unread count listener for user: $userId")

        return firestore.collection(COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Unread count listener error", error)
                    onError(error)
                    return@addSnapshotListener
                }

                val count = snapshot?.size() ?: 0
                Log.d(TAG, "Unread count: $count")
                onUpdate(count)
            }
    }
}