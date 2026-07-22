package com.app.cashflowfamily.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

object OnboardingPages {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.Wallet,
            title = "Selamat Datang!",
            description = "Cashflow Family membantu Anda mengelola keuangan keluarga dengan mudah, transparan, dan terorganisir."
        ),
        OnboardingPage(
            icon = Icons.Filled.Group,
            title = "Kelola Bersama Keluarga",
            description = "Undang anggota keluarga untuk mencatat pemasukan & pengeluaran bersama secara real-time."
        ),
        OnboardingPage(
            icon = Icons.Filled.Insights,
            title = "Insight & Budget Cerdas",
            description = "Set budget bulanan, lihat grafik interaktif, dan dapatkan insight otomatis untuk keuangan yang lebih sehat."
        )
    )
}