package com.app.cashflowfamily.utils

import android.content.Context
import android.content.pm.PackageManager
import com.app.cashflowfamily.data.model.GitHubRelease
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class UpdateChecker(private val context: Context) {

    companion object {
        // ⚠️ GANTI DENGAN USERNAME DAN REPO GITHUB KAMU!
        private const val GITHUB_USER = "a7x-rudolf"
        private const val GITHUB_REPO = "Cashflow-Family"
        private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_USER/$GITHUB_REPO/releases/latest"

        // Timeout 10 detik
        private const val TIMEOUT_SECONDS = 10L
    }

    // OkHttpClient dengan timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            withTimeout((TIMEOUT_SECONDS * 1000).milliseconds) {
                val currentVersion = getCurrentVersion()
                android.util.Log.d("UpdateChecker", "Current Version: $currentVersion")

                val request = Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    android.util.Log.e("UpdateChecker", "API Error: ${response.code}")
                    return@withTimeout null
                }

                val json = response.body?.string() ?: return@withTimeout null
                android.util.Log.d("UpdateChecker", "GitHub Response: $json")

                val release = gson.fromJson(json, GitHubRelease::class.java)
                val latestVersion = release.tagName.removePrefix("v")
                android.util.Log.d("UpdateChecker", "Latest Version: $latestVersion")

                val isNewer = isNewerVersion(latestVersion, currentVersion)
                android.util.Log.d("UpdateChecker", "Is Newer: $isNewer")

                if (isNewer) {
                    val apkAsset = release.assets.firstOrNull {
                        it.name.endsWith(".apk")
                    } ?: return@withTimeout null

                    return@withTimeout UpdateInfo(
                        latestVersion = latestVersion,
                        downloadUrl = apkAsset.downloadUrl,
                        releaseNotes = release.body ?: "Update tersedia!",
                        apkSize = apkAsset.size
                    )
                } else {
                    android.util.Log.d("UpdateChecker", "No update available")
                    return@withTimeout null
                }
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            android.util.Log.e("UpdateChecker", "Timeout: ${e.message}")
            return@withContext null
        } catch (e: Exception) {
            android.util.Log.e("UpdateChecker", "Error: ${e.message}", e)
            return@withContext null
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(latestParts.size, currentParts.size)
            val normalizedLatest = latestParts + List(maxLength - latestParts.size) { 0 }
            val normalizedCurrent = currentParts + List(maxLength - currentParts.size) { 0 }

            for (i in 0 until maxLength) {
                if (normalizedLatest[i] > normalizedCurrent[i]) return true
                if (normalizedLatest[i] < normalizedCurrent[i]) return false
            }
            return false
        } catch (_: Exception) {
            return latest > current
        }
    }
}