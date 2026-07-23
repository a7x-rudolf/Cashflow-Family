package com.app.cashflowfamily.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("familyId") @set:PropertyName("familyId")
    var familyId: String = "",

    @get:PropertyName("role") @set:PropertyName("role")
    var role: String = "member",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("fcmToken") @set:PropertyName("fcmToken")
    var fcmToken: String = ""
)