package com.app.cashflowfamily.data.model

import com.google.firebase.firestore.PropertyName

data class Family(
    @get:PropertyName("familyId") @set:PropertyName("familyId")
    var familyId: String = "",

    @get:PropertyName("familyName") @set:PropertyName("familyName")
    var familyName: String = "",

    @get:PropertyName("familyCode") @set:PropertyName("familyCode")
    var familyCode: String = "",

    @get:PropertyName("ownerId") @set:PropertyName("ownerId")
    var ownerId: String = "",

    @get:PropertyName("members") @set:PropertyName("members")
    var members: List<String> = emptyList(),

    @get:PropertyName("currency") @set:PropertyName("currency")
    var currency: String = "IDR",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)