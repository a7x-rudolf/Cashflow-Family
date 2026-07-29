// app/src/main/java/com/app/cashflowfamily/utils/PushNotifier.kt

package com.app.cashflowfamily.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
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
 * Otorisasi pakai Firebase ID token milik user yang sedang login (BUKAN
 * secret statis yang di-hardcode di client). Server memverifikasi token
 * ini lewat firebase-admin, lalu mengecek sendiri apakah recipients
 * benar-benar satu keluarga dengan pengirim -- jadi tidak ada lagi
 * secret yang bisa diekstrak dari APK untuk memanggil endpoint ini
 * atas nama sembarang user.
 */
object PushNotifier {

    private const val TAG = "PushNotifier"

    // TODO: ganti dengan URL server kamu setelah deploy, contoh:
    // "https://cashflow-push-server.vercel.app//send-push"
    private const val ENDPOINT = "https://cashflow-push-server.vercel.app/api/send-push"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Kirim permintaan push ke server. Fire-and-forget dari sudut pandang
     * pemanggil (suspend, tapi kegagalannya cuma di-log -- TIDAK BOLEH
     * menggagalkan alur utama app; menyimpan transaksi/notifikasi in-app
     * harus tetap sukses walau push gagal).
     *
     * @param recipients map userId -> notificationId milik in-app notification
     *   DOKUMEN USER ITU SENDIRI (bukan notificationId milik user lain!).
     *   Setiap user di Firestore punya dokumen notifikasi terpisah (lihat
     *   NotificationRepository.addNotifications -> batch.set per user),
     *   jadi notificationId yang dikirim ke tiap device HARUS punya milik
     *   dia sendiri, supaya tap notifikasi push membuka/mark-as-read
     *   dokumen yang benar.
     */
    suspend fun notify(
        recipients: Map<String, String>,
        actorUserId: String,
        type: String,
        title: String,
        message: String
    ) {
        if (recipients.isEmpty()) return

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "Tidak ada user login, batal kirim push")
            return
        }

        val idToken = try {
            currentUser.getIdToken(false).await().token
        } catch (e: Exception) {
            Log.e(TAG, "Gagal ambil ID token, batal kirim push", e)
            null
        } ?: return

        val payload = JSONArray(
            recipients.map { (userId, notificationId) ->
                JSONObject().apply {
                    put("userId", userId)
                    put("notificationId", notificationId)
                }
            }
        )

        val body = JSONObject().apply {
            put("recipients", payload)
            put("actorUserId", actorUserId)
            put("type", type)
            put("title", title)
            put("message", message)
        }.toString()

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $idToken")
            .post(body.toRequestBody(JSON))
            .build()

        // enqueue() (async, non-blocking) tetap dipakai untuk HTTP call-nya --
        // cuma pengambilan ID token di atas yang perlu di-await, supaya fungsi
        // suspend ini tidak memblokir thread pemanggil (viewModelScope default
        // jalan di Main dispatcher).
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
