package com.app.cashflowfamily.data.model

enum class FeedbackType(val label: String) {
    FEEDBACK("Kritik & Saran"),
    BUG("Laporkan Masalah"),
    QUESTION("Pertanyaan"),
    FEATURE("Permintaan Fitur")
}

data class Feedback(
    val feedbackId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val type: String = "feedback", // feedback, bug, question, feature
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val adminReply: String? = null,
    val replyAt: Long? = null
)