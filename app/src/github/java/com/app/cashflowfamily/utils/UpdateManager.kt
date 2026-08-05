package com.app.cashflowfamily.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.*
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class UpdateManager(private val context: Context) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun downloadAndInstall(
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Hapus APK lama biar tidak menumpuk
        cleanOldApks()

        val fileName = "cashflow_update.apk"
        val destinationFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(downloadUrl.toUri()).apply {
            setDestinationUri(Uri.fromFile(destinationFile))
            setTitle("Cashflow Family Update")
            setDescription("Mengunduh versi terbaru...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setMimeType("application/vnd.android.package-archive")
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
        }

        try {
            downloadId = downloadManager.enqueue(request)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Enqueue failed", e)
            onError("Gagal memulai download: ${e.message}")
            return
        }

        scope.launch {
            var isComplete = false
            var lastProgress = -1

            while (!isComplete) {
                delay(500.milliseconds)

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0 && statusIndex >= 0) {
                        val bytesDownloaded = cursor.getInt(bytesDownloadedIndex)
                        val bytesTotal = cursor.getInt(bytesTotalIndex)

                        if (bytesTotal > 0) {
                            val progress = (bytesDownloaded * 100 / bytesTotal)
                            if (progress != lastProgress) {
                                lastProgress = progress
                                withContext(Dispatchers.Main) {
                                    onProgress(progress)
                                }
                            }
                        }

                        val status = cursor.getInt(statusIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                isComplete = true
                                withContext(Dispatchers.Main) {
                                    onProgress(100)
                                    onComplete()
                                    delay(300.milliseconds)
                                    installApk(destinationFile, onError)
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                isComplete = true
                                withContext(Dispatchers.Main) {
                                    onError("Download gagal! Cek koneksi internet.")
                                }
                            }
                        }
                    }
                } else {
                    isComplete = true
                    withContext(Dispatchers.Main) {
                        onError("Download dibatalkan")
                    }
                }
                cursor.close()
            }
        }
    }

    /**
     * Install APK dengan cek permission "Install Unknown Apps"
     * (minSdk = 26, jadi tidak perlu cek Build.VERSION.SDK_INT)
     */
    private fun installApk(file: File, onError: (String) -> Unit) {
        if (!file.exists()) {
            onError("File APK tidak ditemukan")
            return
        }

        // Cek permission install unknown apps
        if (!context.packageManager.canRequestPackageInstalls()) {
            showInstallPermissionDialog()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Install failed", e)
            onError("Gagal membuka installer: ${e.message}")
        }
    }

    /**
     * Tampilkan dialog minta permission install unknown apps
     */
    private fun showInstallPermissionDialog() {
        Toast.makeText(
            context,
            "Izinkan aplikasi ini untuk menginstall update, lalu klik Update lagi",
            Toast.LENGTH_LONG
        ).show()

        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                "package:${context.packageName}".toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Cannot open settings", e)
            Toast.makeText(
                context,
                "Silakan aktifkan 'Install Unknown Apps' di Settings > Apps > Cashflow Family",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Hapus APK lama dari download sebelumnya
     */
    private fun cleanOldApks() {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            downloadDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("UpdateManager", "Failed to clean old APKs", e)
        }
    }
}