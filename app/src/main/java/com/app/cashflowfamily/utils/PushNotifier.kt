// app/src/main/java/com/app/cashflowfamily/utils/PushNotifier.kt

package com.app.cashflowfamily.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Pengganti Cloud Functions untuk mengirim FCM push, karena project
 * belum di Blaze plan. Memanggil server Node kecil (lihat push-server/)
 * yang sudah punya service account & logic pengiriman FCM.
 *
 * GANTI ENDPOINT dan API_SECRET ini setelah server-nya di-deploy.
 * API_SECRET harus SAMA PERSIS dengan env var PUSH_API_SECRET di server.
 */
object PushNotifier {

    private const val TAG = "PushNotifier"

    // TODO: ganti dengan URL server kamu setelah deploy, contoh:
    // "https://cashflow-push-server.vercel.app//send-push"
    private const val ENDPOINT = "https://cashflow-push-server.vercel.app/api/send-push"

    // TODO: ganti dengan secret yang sama seperti di env var server
    private const val API_SECRET = "Cf9x2LqW8mVzR4tPnK6jH1sYuA3bD7oE"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Kirim permintaan push ke server. Fire-and-forget (async), gagal
     * kirim push TIDAK BOLEH menggagalkan alur utama app (menyimpan
     * transaksi/notifikasi in-app harus tetap sukses walau push gagal).
     */
    fun notify(
        recipientUserIds: List<String>,
        actorUserId: String,
        type: String,
        title: String,
        message: String,
        notificationId: String
    ) {
        if (recipientUserIds.isEmpty()) return

        val body = JSONObject().apply {
            put("recipientUserIds", JSONArray(recipientUserIds))
            put("actorUserId", actorUserId)
            put("type", type)
            put("title", title)
            put("message", message)
            put("notificationId", notificationId)
        }.toString()

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $API_SECRET")
            .post(body.toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Gagal panggil push server", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "Push server error: ${it.code} ${it.body?.string()}")
                    } else {
                        Log.d(TAG, "Push server OK: ${it.body?.string()}")
                    }
                }
            }
        })
    }
}
