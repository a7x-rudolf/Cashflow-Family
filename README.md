<div align="center">

# 💰 Cashflow Family — Android

**Aplikasi manajemen keuangan keluarga.**
Kelola pemasukan, pengeluaran, dan anggaran bulanan secara kolaboratif — bersama seluruh anggota keluarga.

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Portfolio%20Only-lightgrey?style=for-the-badge)](#-lisensi)

[Unduh APK](#-unduh) · [Fitur](#-fitur) · [Teknologi](#-teknologi) · [Instalasi](#-persiapan-menjalankan-proyek)

</div>

<br>

## ✨ Fitur

**Transaksi**

| | |
|---|---|
| 💸 | Pencatatan transaksi pemasukan & pengeluaran dengan kategori |
| 🔁 | Transaksi berulang untuk gaji, tagihan, dan langganan |
| 🧾 | Riwayat transaksi lengkap dengan filter |

**Keluarga & Budget**

| | |
|---|---|
| 👨‍👩‍👧‍👦 | Manajemen keluarga dengan undangan anggota melalui kode |
| 🔄 | Data tersinkron real-time ke seluruh anggota keluarga |
| 📊 | Anggaran bulanan per kategori |
| 📈 | Analitik dan laporan pengeluaran keluarga |
| 📤 | Export laporan ke PDF dan CSV |

**Notifikasi & Keamanan**

| | |
|---|---|
| 🔔 | Notifikasi pengingat harian dan peringatan anggaran |
| 🔒 | Login dengan email/password atau Google |
| 🔐 | Kunci aplikasi dengan fingerprint / biometric |
| ☁️ | Backup dan restore data |

**Lainnya**

| | |
|---|---|
| 🆕 | Pengecekan update aplikasi otomatis lewat GitHub Releases |
| 💬 | Form feedback langsung dari dalam aplikasi |

<br>

## 🛠 Teknologi

| | |
|---|---|
| UI | **Kotlin** dengan **Jetpack Compose** |
| Arsitektur | **MVVM** |
| Dependency Injection | **Hilt** |
| Backend | **Firebase Firestore**, Firebase Auth |
| Background task | WorkManager |
| Local storage | DataStore Preferences |
| Charting | YCharts |
| Image loading | Coil |
| Async | Kotlin Coroutines |

Struktur data di Firestore: `users`, `families`, `transactions`, `budgets`, `recurring_transactions`.

<br>

## 🚀 Persiapan Menjalankan Proyek

> File konfigurasi Firebase dan keystore signing **tidak disertakan** dalam repositori ini.

1. Buat proyek Firebase di [Firebase Console](https://console.firebase.google.com)
2. Unduh `google-services.json` dan letakkan di folder `app/`
3. Isi `local.properties` dengan lokasi Android SDK

<br>

## 📱 Tampilan Aplikasi

<div align="center">

| Beranda | Riwayat Transaksi | Tambah Transaksi |
|:---:|:---:|:---:|
| <img src="docs/Screenshoot%20App/Beranda.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Riwayat.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Tambah-Transkasi.jpeg" width="220"> |

| Login Biometric | Notifikasi | Feedback |
|:---:|:---:|:---:|
| <img src="docs/Screenshoot%20App/Biometric.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Notifikasi.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Feedback.jpeg" width="220"> |

| Pengaturan | Tentang |
|:---:|:---:|
| <img src="docs/Screenshoot%20App/Setelan.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Tentang.jpeg" width="220"> |

</div>

<br>

## 📥 Unduh

Aplikasi siap diunduh dan dipasang.

<div align="center">

[![Download APK](https://img.shields.io/badge/Download-Cashflow%20Family%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.apk)

</div>

<br>

## 📄 Lisensi

Kode ini tersedia untuk tujuan **portofolio dan referensi**. Penggunaan ulang, modifikasi, atau distribusi tanpa izin tertulis dari pemilik tidak diperbolehkan.

<br>

<div align="center">

Dibuat oleh **[Ridolf Widi Alfisa Lumba](https://github.com/a7x-rudolf)**

</div>
