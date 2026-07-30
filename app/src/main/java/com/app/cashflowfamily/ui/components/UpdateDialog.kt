package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.app.cashflowfamily.utils.UpdateInfo
import com.app.cashflowfamily.utils.UpdateManager

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onDownloadStart: () -> Unit
) {
    val context = LocalContext.current
    val updateManager = remember { UpdateManager(context) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var downloadComplete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading,
            dismissOnClickOutside = !isDownloading
        ),
        title = {
            Text(
                text = when {
                    downloadComplete -> "Membuka Installer..."
                    isDownloading -> "Mengunduh Update"
                    else -> "Update Tersedia!"
                },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isDownloading) {
                    Text(
                        text = "Versi terbaru: ${updateInfo.latestVersion}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (updateInfo.apkSize > 0) {
                        Text(
                            text = "Ukuran: ${formatFileSize(updateInfo.apkSize)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = "Catatan rilis:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = updateInfo.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { downloadProgress.toFloat() / 100 },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (downloadComplete) {
                            "Download selesai! Installer akan terbium..."
                        } else {
                            "$downloadProgress% - Mohon tunggu..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(
                    onClick = {
                        isDownloading = true
                        downloadProgress = 0
                        errorMessage = null
                        onDownloadStart()

                        updateManager.downloadAndInstall(
                            downloadUrl = updateInfo.downloadUrl,
                            onProgress = { progress ->
                                downloadProgress = progress
                            },
                            onComplete = {
                                downloadComplete = true
                                // Dialog akan tetap terbuka sebentar sampai installer muncul
                            },
                            onError = { error ->
                                isDownloading = false
                                errorMessage = error
                            }
                        )
                    }
                ) {
                    Text("Update Sekarang")
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Nanti")
                }
            }
        }
    )
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}