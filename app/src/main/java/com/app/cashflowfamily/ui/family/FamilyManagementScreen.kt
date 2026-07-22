package com.app.cashflowfamily.ui.family

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.ui.components.ConfirmationDialog
import com.app.cashflowfamily.ui.components.FamilyCodeCard
import com.app.cashflowfamily.ui.components.MemberItem
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.FamilyManagementViewModel
import com.app.cashflowfamily.viewmodel.MemberInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementScreen(
    rootNavController: NavController,
    viewModel: FamilyManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var memberMenuState by remember { mutableStateOf<MemberInfo?>(null) }
    var memberKickDialog by remember { mutableStateOf<MemberInfo?>(null) }
    var memberPromoteDialog by remember { mutableStateOf<MemberInfo?>(null) }
    var topBarMenuExpanded by remember { mutableStateOf(false) }

    val isAdmin = uiState.currentUser?.role == "admin"
    val currentUser = uiState.currentUser
    val roleText = if (isAdmin) "Admin" else "Member"

    // Handle action state
    LaunchedEffect(actionState) {
        when (actionState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    (actionState as Resource.Success).data,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetActionState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (actionState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    // Handle leave family - navigate ke CreateOrJoinFamily
    val hasLeftFamily by viewModel.hasLeftFamily.collectAsState()
    LaunchedEffect(hasLeftFamily) {
        if (hasLeftFamily) {
            rootNavController.navigate(Screen.CreateOrJoinFamily.route) {
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
                            text = uiState.family?.familyName ?: "Kelola Keluarga",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )

                        val memberCount = uiState.members.size
                        val subtitleText = buildString {
                            append("$memberCount anggota")
                            if (currentUser != null) {
                                append(" · Anda [$roleText]")
                            }
                        }

                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // ===== SHARE / UNDANG ANGGOTA (soft circle) =====
                    if (uiState.family != null) {
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
                                    shareFamilyInvitation(
                                        context = context,
                                        familyName = uiState.family!!.familyName,
                                        familyCode = uiState.family!!.familyCode
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Undang Anggota",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // ===== MENU LAINNYA (⋮) dengan soft circle =====
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

                    // Dropdown Menu untuk TopBar
                    DropdownMenu(
                        expanded = topBarMenuExpanded,
                        onDismissRequest = { topBarMenuExpanded = false }
                    ) {
                        // Menu "Keluar dari Keluarga" - visible kalau bukan single admin
                        val canLeave = !isAdmin || uiState.members.count { it.user.role == "admin" } > 1

                        if (canLeave) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Keluar dari Keluarga",
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
                                    showLeaveDialog = true
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Tidak dapat keluar (satu-satunya admin)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                },
                                enabled = false,
                                onClick = {}
                            )
                        }
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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.family == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Data keluarga tidak ditemukan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Family Info - Tanggal dibuat
                        item {
                            Text(
                                text = "Dibuat pada ${DateFormatter.formatFullDate(uiState.family!!.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }

                        // Family Code Card
                        item {
                            FamilyCodeCard(
                                familyName = uiState.family!!.familyName,
                                familyCode = uiState.family!!.familyCode
                            )
                        }

                        // Members Section Header
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Anggota Keluarga (${uiState.members.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Members List
                        items(
                            items = uiState.members,
                            key = { it.user.userId }
                        ) { memberInfo ->
                            MemberItem(
                                user = memberInfo.user,
                                transactionCount = memberInfo.transactionCount,
                                isCurrentUser = memberInfo.isCurrentUser,
                                showMenu = isAdmin && !memberInfo.isCurrentUser,
                                onMenuClick = {
                                    memberMenuState = memberInfo
                                }
                            )
                        }

                        // Info untuk single admin
                        if (isAdmin && uiState.members.count { it.user.role == "admin" } == 1) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sebagai satu-satunya admin, Anda tidak dapat keluar dari keluarga. Promosikan anggota lain menjadi admin terlebih dahulu.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            // Dropdown menu untuk admin action
            memberMenuState?.let { member ->
                Box(modifier = Modifier.padding(16.dp)) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { memberMenuState = null }
                    ) {
                        if (member.user.role != "admin") {
                            DropdownMenuItem(
                                text = { Text("Jadikan Admin") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    memberMenuState = null
                                    memberPromoteDialog = member
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Keluarkan dari Keluarga",
                                    color = Color(0xFFE53935)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PersonRemove,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935)
                                )
                            },
                            onClick = {
                                memberMenuState = null
                                memberKickDialog = member
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Leave Family
    if (showLeaveDialog) {
        ConfirmationDialog(
            title = "Keluar dari Keluarga?",
            message = "Anda akan kehilangan akses ke semua transaksi keluarga ini. Tindakan ini tidak bisa dibatalkan. Yakin ingin keluar?",
            confirmText = "Keluar",
            dismissText = "Batal",
            isDestructive = true,
            onConfirm = {
                showLeaveDialog = false
                viewModel.leaveFamily()
            },
            onDismiss = { showLeaveDialog = false }
        )
    }

    // Dialog: Kick Member
    memberKickDialog?.let { member ->
        ConfirmationDialog(
            title = "Keluarkan Anggota?",
            message = "Apakah Anda yakin ingin mengeluarkan ${member.user.name} dari keluarga? Anggota ini akan kehilangan akses ke semua transaksi.",
            confirmText = "Keluarkan",
            dismissText = "Batal",
            isDestructive = true,
            onConfirm = {
                viewModel.kickMember(member.user.userId, member.user.name)
                memberKickDialog = null
            },
            onDismiss = { memberKickDialog = null }
        )
    }

    // Dialog: Promote to Admin
    memberPromoteDialog?.let { member ->
        ConfirmationDialog(
            title = "Jadikan Admin?",
            message = "${member.user.name} akan menjadi admin dan bisa mengelola anggota keluarga. Yakin?",
            confirmText = "Ya, Jadikan Admin",
            dismissText = "Batal",
            isDestructive = false,
            onConfirm = {
                viewModel.promoteToAdmin(member.user.userId, member.user.name)
                memberPromoteDialog = null
            },
            onDismiss = { memberPromoteDialog = null }
        )
    }
}

/**
 * Direct share family invitation via WhatsApp/Email/dll
 */
private fun shareFamilyInvitation(
    context: android.content.Context,
    familyName: String,
    familyCode: String
) {
    val message = """
        Halo! Saya mengundang Anda bergabung dengan keluarga "$familyName" di aplikasi Cashflow Family.
        
        Kode Undangan: $familyCode
        
        Cara bergabung:
        1. Download aplikasi Cashflow Family
        2. Daftar akun baru atau login
        3. Pilih "Gabung Keluarga"
        4. Masukkan kode di atas
        
        Yuk kelola keuangan keluarga bersama!
    """.trimIndent()

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Undangan Keluarga Cashflow Family")
        putExtra(Intent.EXTRA_TEXT, message)
    }

    val chooserIntent = Intent.createChooser(shareIntent, "Undang anggota via")
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooserIntent)
}