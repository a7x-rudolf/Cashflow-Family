package com.app.cashflowfamily.data.model

import com.google.firebase.firestore.PropertyName

data class Notification(
    @get:PropertyName("notificationId") @set:PropertyName("notificationId")
    var notificationId: String = "",

    @get:PropertyName("familyId") @set:PropertyName("familyId")
    var familyId: String = "",

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "info",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("message") @set:PropertyName("message")
    var message: String = "",

    @get:PropertyName("data") @set:PropertyName("data")
    var data: Map<String, String> = emptyMap(),

    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)