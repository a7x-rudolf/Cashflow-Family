package com.app.cashflowfamily.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.preferences.ThemeMode
import com.app.cashflowfamily.ui.components.ChangePasswordDialog
import com.app.cashflowfamily.ui.components.ConfirmationDialog
import com.app.cashflowfamily.ui.components.EditNameDialog
import com.app.cashflowfamily.ui.components.SettingsItem
import com.app.cashflowfamily.ui.components.SettingsSectionHeader
import com.app.cashflowfamily.ui.components.ThemeSelectorDialog
import com.app.cashflowfamily.ui.components.UpdateDialog
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.AuthViewModel
import com.app.cashflowfamily.viewmodel.SettingsViewModel
import com.app.cashflowfamily.viewmodel.ThemeViewModel
import kotlin.time.Duration.Companion.milliseconds
import com.app.cashflowfamily.MainActivity
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import com.app.cashflowfamily.ui.components.SettingsToggleItem
import com.app.cashflowfamily.viewmodel.NotificationSettingsViewModel
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Fingerprint
import com.app.cashflowfamily.utils.BiometricHelper
import com.app.cashflowfamily.viewmodel.BiometricViewModel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import com.app.cashflowfamily.viewmodel.UpdateViewModel
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.app.cashflowfamily.ui.components.UserAvatar
import com.app.cashflowfamily.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ===== TIDAK ADA IMPORT BuildConfig! =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    rootNavController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel(),
    biometricViewModel: BiometricViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = viewModel()
) {
    val isDailyReminderEnabled by notificationViewModel.isDailyReminderEnabled.collectAsState()
    val isBudgetWarningEnabled by notificationViewModel.isBudgetWarningEnabled.collectAsState()
    val isFamilyActivityEnabled by notificationViewModel.isFamilyActivityEnabled.collectAsState()
    val isBiometricEnabled by biometricViewModel.isBiometricEnabled.collectAsState()
    val context = LocalContext.current
    val appVersion = getAppVersion(context)

    val uiState by settingsViewModel.uiState.collectAsState()
    val updateNameState by settingsViewModel.updateNameState.collectAsState()
    val changePasswordState by settingsViewModel.changePasswordState.collectAsState()
    val updatePhotoState by settingsViewModel.updatePhotoState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val passwordChangedSuccessfully by settingsViewModel.passwordChangedSuccessfully.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isProcessingPhoto by remember { mutableStateOf(false) }

    // Photo Picker bawaan Android - tidak perlu izin storage runtime
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isProcessingPhoto = true
            coroutineScope.launch {
                val dataUri = withContext(Dispatchers.IO) {
                    ImageUtils.compressImageToDataUri(context, uri)
                }
                isProcessingPhoto = false
                if (dataUri != null) {
                    settingsViewModel.updatePhoto(dataUri)
                } else {
                    Toast.makeText(context, "Gagal memproses foto, coba foto lain", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val onPickPhoto: () -> Unit = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // ===== UPDATE STATE =====
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val isChecking by updateViewModel.isChecking.collectAsState()
    val error by updateViewModel.error.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showLatestVersionStatus by remember { mutableStateOf(false) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var topBarMenuExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Reset status "Versi Terbaru" setelah 3 detik
    LaunchedEffect(updateInfo) {
        if (updateInfo == null && !isChecking) {
            showLatestVersionStatus = true
            delay(3000.milliseconds)
            showLatestVersionStatus = false
        }
    }

    // Tampilkan dialog jika ada update
    LaunchedEffect(updateInfo) {
        if (updateInfo != null) {
            showUpdateDialog = true
        }
    }

    // Tampilkan Toast jika error
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            updateViewModel.clearUpdateInfo()
        }
    }

    // Handle update name result
    LaunchedEffect(updateNameState) {
        when (updateNameState) {
            is Resource.Success -> {
                Toast.makeText(context, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                showEditNameDialog = false
                settingsViewModel.resetUpdateNameState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (updateNameState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                settingsViewModel.resetUpdateNameState()
            }
            else -> {}
        }
    }

    // Handle update photo result
    LaunchedEffect(updatePhotoState) {
        when (updatePhotoState) {
            is Resource.Success -> {
                Toast.makeText(context, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
                settingsViewModel.resetUpdatePhotoState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (updatePhotoState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                settingsViewModel.resetUpdatePhotoState()
            }
            else -> {}
        }
    }

    // Handle change password result
    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    "Password berhasil diubah. Silakan login kembali dengan password baru",
                    Toast.LENGTH_LONG
                ).show()
                showChangePasswordDialog = false
                settingsViewModel.resetChangePasswordState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (changePasswordState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                settingsViewModel.resetChangePasswordState()
            }
            else -> {}
        }
    }

    // Handle auto logout setelah password berhasil diubah
    LaunchedEffect(passwordChangedSuccessfully) {
        if (passwordChangedSuccessfully) {
            delay(1500.milliseconds)
            settingsViewModel.resetPasswordChangedFlag()
            authViewModel.logout()
            rootNavController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Setelan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Kelola akun & preferensi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // Notification Quick Toggle
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Scroll ke section Notifikasi untuk mengatur",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            val hasActiveNotif = isDailyReminderEnabled || isBudgetWarningEnabled || isFamilyActivityEnabled
                            Icon(
                                imageVector = if (hasActiveNotif) {
                                    Icons.Filled.NotificationsActive
                                } else {
                                    Icons.Filled.NotificationsOff
                                },
                                contentDescription = "Notifikasi",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Menu Lainnya
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { topBarMenuExpanded = !topBarMenuExpanded }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu Lainnya",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = topBarMenuExpanded,
                        onDismissRequest = { topBarMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tentang Aplikasi") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                topBarMenuExpanded = false
                                showAboutDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Logout",
                                    color = Color(0xFFE53935)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935)
                                )
                            },
                            onClick = {
                                topBarMenuExpanded = false
                                showLogoutDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Profile Card
                    ProfileCard(
                        userName = uiState.user?.name ?: "-",
                        userEmail = uiState.user?.email ?: "-",
                        userRole = uiState.user?.role ?: "member",
                        familyName = uiState.family?.familyName,
                        photoUrl = uiState.user?.photoUrl,
                        isPhotoLoading = isProcessingPhoto,
                        onEditPhotoClick = onPickPhoto
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // AKUN Section
                    SettingsSectionHeader(title = "AKUN")

                    SettingsItem(
                        icon = Icons.Filled.Person,
                        title = "Edit Profil",
                        subtitle = "Ubah nama Anda",
                        onClick = { showEditNameDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.Filled.CameraAlt,
                        title = "Ganti Foto Profil",
                        subtitle = when {
                            ImageUtils.isDataUri(uiState.user?.photoUrl ?: "") -> "Menggunakan foto kustom"
                            ImageUtils.isHttpUrl(uiState.user?.photoUrl ?: "") -> "Menggunakan foto akun Google"
                            else -> "Belum ada foto, pakai avatar inisial"
                        },
                        onClick = onPickPhoto
                    )

                    if (ImageUtils.isDataUri(uiState.user?.photoUrl ?: "")) {
                        SettingsItem(
                            icon = Icons.Filled.DeleteOutline,
                            title = "Hapus Foto Profil",
                            subtitle = "Kembali ke avatar inisial",
                            onClick = { settingsViewModel.removePhoto() }
                        )
                    }

                    SettingsItem(
                        icon = Icons.Filled.Lock,
                        title = "Ganti Password",
                        subtitle = "Ubah password akun",
                        onClick = { showChangePasswordDialog = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // KEAMANAN Section
                    SettingsSectionHeader(title = "KEAMANAN")

                    SettingsToggleItem(
                        icon = Icons.Filled.Fingerprint,
                        title = "Login dengan Fingerprint",
                        subtitle = "Kunci aplikasi dengan fingerprint",
                        checked = isBiometricEnabled,
                        onCheckedChange = { newValue ->
                            handleBiometricToggle(
                                context = context,
                                enable = newValue,
                                onEnable = {
                                    biometricViewModel.setBiometricEnabled(true)
                                },
                                onDisable = {
                                    biometricViewModel.setBiometricEnabled(false)
                                }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // NOTIFIKASI Section
                    SettingsSectionHeader(title = "NOTIFIKASI")

                    SettingsToggleItem(
                        icon = Icons.Filled.NotificationsActive,
                        title = "Pengingat Harian",
                        subtitle = "Notifikasi jam 20:00 setiap hari",
                        checked = isDailyReminderEnabled,
                        onCheckedChange = {
                            notificationViewModel.setDailyReminderEnabled(it)
                        }
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.Notifications,
                        title = "Peringatan Budget",
                        subtitle = "Notifikasi saat budget mendekati/terlampaui",
                        checked = isBudgetWarningEnabled,
                        onCheckedChange = {
                            notificationViewModel.setBudgetWarningEnabled(it)
                        }
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.People,
                        title = "Aktivitas Keluarga",
                        subtitle = "Notifikasi saat anggota lain menambah transaksi",
                        checked = isFamilyActivityEnabled,
                        onCheckedChange = {
                            notificationViewModel.setFamilyActivityEnabled(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // APLIKASI Section
                    SettingsSectionHeader(title = "APLIKASI")

                    SettingsItem(
                        icon = Icons.Filled.Wallet,
                        title = "Budget Bulanan",
                        subtitle = "Kelola anggaran per kategori",
                        onClick = {
                            rootNavController.navigate(Screen.Budget.route)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Filled.Repeat,
                        title = "Transaksi Berulang",
                        subtitle = "Kelola tagihan & gaji rutin",
                        onClick = {
                            rootNavController.navigate(Screen.RecurringList.route)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Filled.Backup,
                        title = "Backup & Restore",
                        subtitle = "Cadangkan atau pulihkan data",
                        onClick = {
                            rootNavController.navigate(Screen.BackupRestore.route)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Filled.DarkMode,
                        title = "Tema",
                        subtitle = "Ubah tampilan aplikasi",
                        trailing = when (themeMode) {
                            ThemeMode.LIGHT -> "Terang"
                            ThemeMode.DARK -> "Gelap"
                            ThemeMode.SYSTEM -> "Otomatis"
                        },
                        onClick = { showThemeDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.Filled.CurrencyExchange,
                        title = "Mata Uang",
                        subtitle = "Format nominal",
                        trailing = "IDR",
                        onClick = {
                            Toast.makeText(
                                context,
                                "Fitur mata uang akan segera hadir",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tentang Section
                    SettingsSectionHeader(title = "TENTANG")

                    // 1. Cek Update
                    SettingsItem(
                        icon = Icons.Filled.SystemUpdate,
                        title = "Cek Update",
                        subtitle = when {
                            isChecking -> "Memeriksa update..."
                            showLatestVersionStatus -> "Aplikasi sudah versi terbaru"
                            updateInfo != null -> "Update tersedia"
                            else -> "Periksa versi terbaru"
                        },
                        onClick = {
                            if (!isChecking) {
                                showLatestVersionStatus = false
                                updateViewModel.checkForUpdate(silent = false)
                            }
                        }
                    )

                    // 2. Tentang Aplikasi
                    SettingsItem(
                        icon = Icons.Filled.Info,
                        title = "Tentang Aplikasi",
                        subtitle = "Informasi Cashflow Family",
                        onClick = { showAboutDialog = true }
                    )

                    // 3. Versi Aplikasi
                    SettingsItem(
                        icon = Icons.Filled.PhoneAndroid,
                        title = "Versi Aplikasi",
                        trailing = "v$appVersion",
                        showArrow = false,
                        onClick = { }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialogs
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = uiState.user?.name ?: "",
            onConfirm = { newName ->
                settingsViewModel.updateName(newName)
            },
            onDismiss = { showEditNameDialog = false }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isLoading = changePasswordState is Resource.Loading,
            onConfirm = { current, new ->
                settingsViewModel.changePassword(current, new)
            },
            onDismiss = {
                if (changePasswordState !is Resource.Loading) {
                    showChangePasswordDialog = false
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentMode = themeMode,
            onModeSelected = { newMode ->
                themeViewModel.setThemeMode(newMode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout?",
            message = "Anda akan keluar dari aplikasi. Data Anda tetap aman di cloud dan bisa diakses saat login kembali.",
            confirmText = "Logout",
            dismissText = "Batal",
            isDestructive = true,
            onConfirm = {
                showLogoutDialog = false
                (context as? MainActivity)?.stopFamilyListenerBeforeLogout()
                biometricViewModel.setBiometricEnabled(false)
                authViewModel.logout()
                rootNavController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false },
            appVersion = appVersion
        )
    }

    // Dialog Update
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = {
                showUpdateDialog = false
                updateViewModel.clearUpdateInfo()
            },
            onDownloadStart = {
                // Optional: log analytics
            }
        )
    }
}

// ===== HELPER FUNCTION =====
private fun getAppVersion(context: android.content.Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "1.0.0"
    } catch (_: Exception) {
        "1.0.0"
    }
}

private fun handleBiometricToggle(
    context: android.content.Context,
    enable: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit
) {
    if (!enable) {
        onDisable()
        Toast.makeText(
            context,
            "Login fingerprint dinonaktifkan",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    when (val availability = BiometricHelper.checkBiometricAvailability(context)) {
        BiometricHelper.BiometricAvailability.AVAILABLE -> {
            val activity = BiometricHelper.getActivity(context)
            if (activity == null) {
                Toast.makeText(
                    context,
                    "Error: Activity tidak valid",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Aktifkan Fingerprint",
                subtitle = "Verifikasi identitas untuk mengaktifkan fitur",
                negativeButtonText = "Batal",
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Login fingerprint diaktifkan",
                        Toast.LENGTH_SHORT
                    ).show()
                    onEnable()
                },
                onError = { _, errString ->
                    Toast.makeText(context, "Batal: $errString", Toast.LENGTH_SHORT).show()
                }
            )
        }
        else -> {
            val message = BiometricHelper.getBiometricStatusMessage(availability)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    userEmail: String,
    userRole: String,
    familyName: String?,
    photoUrl: String?,
    isPhotoLoading: Boolean,
    onEditPhotoClick: () -> Unit
) {
    val isAdmin = userRole == "admin"
    val avatarSize = 76.dp
    val badgeSize = 28.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(avatarSize + 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        .padding(3.dp)
                ) {
                    UserAvatar(
                        name = userName,
                        photoUrl = photoUrl,
                        size = avatarSize - 6.dp,
                        backgroundColor = Color.White.copy(alpha = 0.22f),
                        textColor = Color.White
                    )
                }

                // Badge edit foto — nempel di sudut kanan-bawah avatar
                Surface(
                    onClick = onEditPhotoClick,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(badgeSize)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isPhotoLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Ganti foto profil",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text(
                                text = if (isAdmin) "Admin" else "Member",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = Color.White.copy(alpha = 0.18f),
                            disabledLabelColor = Color.White
                        ),
                        border = BorderStroke(0.dp, Color.Transparent),
                        modifier = Modifier.height(26.dp)
                    )

                    if (familyName != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    text = familyName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = Color.White.copy(alpha = 0.18f),
                                disabledLabelColor = Color.White
                            ),
                            border = BorderStroke(0.dp, Color.Transparent),
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit,
    appVersion: String
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tentang Aplikasi") },
        text = {
            Column {
                Text(
                    text = "Cashflow Family",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Versi $appVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aplikasi pencatatan keuangan keluarga yang memungkinkan setiap anggota keluarga mencatat pemasukan dan pengeluaran, sehingga arus kas keluarga dapat dipantau secara transparan.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dikembangkan oleh",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = "Ridolf Widi Alfisa Lumba",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "© 2026 Ridolf Widi Alfisa Lumba",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}