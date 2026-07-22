package com.app.cashflowfamily.utils

import java.util.Calendar

object GreetingHelper {

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    fun getGreetingWithName(name: String): String {
        val firstName = name.split(" ").firstOrNull() ?: name
        return "${getGreeting()}, $firstName"
    }
}