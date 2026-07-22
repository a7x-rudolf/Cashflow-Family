package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "FeedbackRepo"
        private const val COLLECTION = "feedbacks"
    }

    suspend fun sendFeedback(feedback: Feedback): Result<Feedback> {
        return try {
            Log.d(TAG, "Sending feedback from user: ${feedback.userName}")

            val ref = firestore.collection(COLLECTION).document()
            val newFeedback = feedback.copy(feedbackId = ref.id)
            ref.set(newFeedback).await()

            Log.d(TAG, "Feedback sent with ID: ${ref.id}")
            Result.success(newFeedback)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending feedback", e)
            Result.failure(e)
        }
    }

    suspend fun getFeedbacks(userId: String): Result<List<Feedback>> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val feedbacks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Feedback::class.java)?.copy(feedbackId = doc.id)
            }

            Result.success(feedbacks)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching feedbacks", e)
            Result.failure(e)
        }
    }
}