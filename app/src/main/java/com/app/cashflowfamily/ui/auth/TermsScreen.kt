package com.app.cashflowfamily.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * STEP F2: Terms & Conditions (isi placeholder).
 * Konten lengkap & final bisa dipoles nanti di STEP F12 kalau diperlukan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Syarat & Ketentuan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Syarat & Ketentuan Penggunaan Cashflow Family",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                SectionTitle("1. Penggunaan Aplikasi")
                SectionBody(
                    "Cashflow Family disediakan untuk membantu Anda dan keluarga mencatat " +
                        "serta mengelola arus kas rumah tangga. Anda bertanggung jawab atas " +
                        "keakuratan data yang dimasukkan."
                )

                SectionTitle("2. Data Keluarga")
                SectionBody(
                    "Data transaksi dan anggota keluarga yang Anda buat hanya dapat diakses " +
                        "oleh anggota grup keluarga yang sama, sesuai peran (Admin/Member) " +
                        "yang ditetapkan."
                )

                SectionTitle("3. Privasi")
                SectionBody(
                    "Kami menyimpan data Anda menggunakan layanan Firebase. Kami tidak " +
                        "membagikan data pribadi Anda kepada pihak ketiga tanpa persetujuan Anda."
                )

                SectionTitle("4. Akun Pengguna")
                SectionBody(
                    "Anda bertanggung jawab menjaga kerahasiaan email dan kata sandi akun " +
                        "Anda. Segera laporkan jika ada aktivitas mencurigakan pada akun Anda."
                )

                SectionTitle("5. Perubahan Ketentuan")
                SectionBody(
                    "Ketentuan ini dapat diperbarui dari waktu ke waktu. Perubahan akan " +
                        "berlaku setelah dipublikasikan melalui aplikasi."
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SectionBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    )
}
