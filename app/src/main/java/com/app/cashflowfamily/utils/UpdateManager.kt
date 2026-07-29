package com.app.cashflowfamily.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.app.cashflowfamily.BuildConfig
import kotlinx.coroutines.*
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri

class UpdateManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var downloadId: Long = -1
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun downloadAndInstall(
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fileName = "update_${System.currentTimeMillis()}.apk"
        val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        val request = DownloadManager.Request(downloadUrl.toUri()).apply {
            setDestinationUri(Uri.fromFile(destinationFile))
            setTitle("Cashflow Family Update")
            setDescription("Mengunduh versi terbaru...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setMimeType("application/vnd.android.package-archive")
        }

        downloadId = downloadManager.enqueue(request)

        scope.launch {
            var isComplete = false
            while (!isComplete) {
                delay(500.milliseconds)
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0 && statusIndex >= 0) {
                        val bytesDownloaded = cursor.getInt(bytesDownloadedIndex)
                        val bytesTotal = cursor.getInt(bytesTotalIndex)

                        if (bytesTotal > 0) {
                            val progress = (bytesDownloaded * 100 / bytesTotal)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }

                        val status = cursor.getInt(statusIndex)
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                isComplete = true
                                withContext(Dispatchers.Main) {
                                    onComplete()
                                    installApk(destinationFile)
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                isComplete = true
                                withContext(Dispatchers.Main) {
                                    onError("Download gagal!")
                                }
                            }
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun installApk(file: File) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        intent.apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}