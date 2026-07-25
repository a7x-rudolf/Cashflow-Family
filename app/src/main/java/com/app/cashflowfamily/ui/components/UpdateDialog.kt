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

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Update Tersedia!",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress.toFloat() / 100 },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Mengunduh... $downloadProgress%",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(
                    onClick = {
                        isDownloading = true
                        onDownloadStart()
                        updateManager.downloadAndInstall(
                            downloadUrl = updateInfo.downloadUrl,
                            onProgress = { progress ->
                                downloadProgress = progress
                            },
                            onComplete = {
                                isDownloading = false
                                onDismiss()
                            },
                            onError = { _ ->
                                isDownloading = false
                            }
                        )
                    }
                ) {
                    Text("Update Sekarang")
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text("Mengunduh...")
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