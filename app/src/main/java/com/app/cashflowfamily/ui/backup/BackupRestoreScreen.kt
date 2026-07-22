package com.app.cashflowfamily.ui.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.ui.components.ConfirmationDialog
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.utils.backup.BackupHelper
import com.app.cashflowfamily.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val restoreState by viewModel.restoreState.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showRestoreResultDialog by remember { mutableStateOf(false) }

    // File picker untuk restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it) }
    }

    // Handle backup result
    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is Resource.Success -> {
                val file = state.data

                Toast.makeText(
                    context,
                    "Backup berhasil: ${file.name}",
                    Toast.LENGTH_SHORT
                ).show()

                // Delay agar Toast sempat tampil dulu
                kotlinx.coroutines.delay(500)

                // Share backup
                try {
                    BackupHelper.shareBackup(context, file)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Gagal membuka share: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                viewModel.resetBackupState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetBackupState()
            }
            else -> {}
        }
    }

    // Handle restore result
    LaunchedEffect(restoreState) {
        when (restoreState) {
            is Resource.Success -> {
                showRestoreResultDialog = true
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (restoreState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetRestoreState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Backup & Restore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelola cadangan data keuangan",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Info Card
            InfoCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Backup Section
            SectionTitle("BACKUP DATA")

            Spacer(modifier = Modifier.height(8.dp))

            ActionCard(
                icon = Icons.Filled.CloudUpload,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "Buat Cadangan",
                description = "Export semua transaksi, budget, dan recurring ke file JSON",
                buttonText = "Buat Backup",
                isLoading = backupState is Resource.Loading,
                onClick = { showBackupDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Restore Section
            SectionTitle("RESTORE DATA")

            Spacer(modifier = Modifier.height(8.dp))

            ActionCard(
                icon = Icons.Filled.CloudDownload,
                iconColor = Color(0xFF43A047),
                title = "Pulihkan dari Backup",
                description = "Import data dari file backup JSON ke keluarga aktif",
                buttonText = "Pulihkan Data",
                isLoading = restoreState is Resource.Loading,
                onClick = { showRestoreDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Card
            WarningCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showBackupDialog) {
        ConfirmationDialog(
            title = "Buat Backup?",
            message = "File JSON berisi semua data keluarga akan dibuat. Anda bisa membagikannya via WhatsApp, Email, atau Google Drive.",
            confirmText = "Buat",
            dismissText = "Batal",
            isDestructive = false,
            onConfirm = {
                showBackupDialog = false
                viewModel.createBackup()
            },
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showRestoreDialog) {
        ConfirmationDialog(
            title = "Pulihkan Data?",
            message = "Data dari file backup akan diimport ke keluarga aktif. Data yang sudah ada tidak akan diduplikasi. Lanjutkan?",
            confirmText = "Pilih File",
            dismissText = "Batal",
            isDestructive = false,
            onConfirm = {
                showRestoreDialog = false
                filePickerLauncher.launch(arrayOf("application/json", "*/*"))
            },
            onDismiss = { showRestoreDialog = false }
        )
    }

    if (showRestoreResultDialog) {
        val result = (restoreState as? Resource.Success)?.data
        if (result != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showRestoreResultDialog = false
                    viewModel.resetRestoreState()
                },
                title = { Text("Restore Selesai") },
                text = {
                    Column {
                        Text(
                            text = "Berhasil import ${result.totalImported} data",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Transaksi: ${result.transactionsImported} baru, ${result.transactionsSkipped} skip")
                        Text("Budget: ${result.budgetsImported} baru, ${result.budgetsSkipped} skip")
                        Text("Recurring: ${result.recurringsImported} baru, ${result.recurringsSkipped} skip")

                        if (result.totalSkipped > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Data yang di-skip berarti sudah ada di aplikasi (mencegah duplikasi)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showRestoreResultDialog = false
                            viewModel.resetRestoreState()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = buttonText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Tentang Backup & Restore",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Simpan file backup di tempat aman seperti Google Drive. File ini bisa digunakan untuk memulihkan data jika HP hilang atau ganti device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun WarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Perhatian",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Restore hanya menambah data baru. Data yang sudah ada tidak akan diubah atau dihapus. File backup berisi data sensitif keuangan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}