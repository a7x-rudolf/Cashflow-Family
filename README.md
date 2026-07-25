# Cashflow Family

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black" alt="Firebase"/>
  <img src="https://img.shields.io/badge/Version-1.0.0-blue" alt="Version"/>
</p>

Aplikasi Android untuk manajemen keuangan keluarga dengan sinkronisasi real-time.

Cashflow Family membantu anggota keluarga mencatat pemasukan, pengeluaran, dan mengatur budget bulanan bersama-sama dalam satu aplikasi. Semua data tersinkronisasi secara real-time antar anggota melalui Firebase, sehingga setiap perubahan langsung terlihat oleh seluruh keluarga.

---

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Tech Stack](#tech-stack)
- [Struktur Project](#struktur-project)
- [Setup](#setup)
- [Tampilan Aplikasi](#tampilan-aplikasi)
- [Download](#download)
- [Lisensi](#lisensi)

---

## Fitur Utama

**Manajemen Transaksi**
- Pencatatan pemasukan dan pengeluaran dengan kategori
- Transaksi berulang (recurring) untuk gaji, tagihan, dan langganan
- Riwayat transaksi lengkap dengan filter dan pencarian

**Kolaborasi Keluarga**
- Sistem kode keluarga untuk mengundang anggota
- Sinkronisasi real-time antar semua anggota keluarga
- Manajemen anggota dengan role dan akses yang jelas

**Budgeting**
- Budget bulanan per kategori
- Peringatan otomatis saat mendekati atau melewati batas anggaran
- Visualisasi progres budget secara real-time

**Notifikasi & Keamanan**
- Push notification real-time via Firebase Cloud Messaging
- Notifikasi tetap terkirim meskipun aplikasi tertutup
- Autentikasi biometrik (fingerprint/face unlock)
- Pengecekan pembaruan aplikasi otomatis

---

## Tech Stack

### Android Application

| Kategori          | Teknologi                          |
| ----------------- | ---------------------------------- |
| Bahasa            | Kotlin                             |
| UI Framework      | Jetpack Compose (Material 3)       |
| Arsitektur        | MVVM                               |
| Dependency Inj.   | Hilt                               |
| Autentikasi       | Firebase Auth                      |
| Database          | Cloud Firestore                    |
| Notifikasi        | Firebase Cloud Messaging           |
| Image Loading     | Coil                               |
| Min SDK           | 26 (Android 8.0)                   |
| Target SDK        | 35 (Android 15)                    |

### Backend

| Kategori           | Teknologi                          |
| ------------------ | ---------------------------------- |
| Cloud Functions    | TypeScript (Firebase Functions)    |
| Security Rules     | Firestore Security Rules           |
| Push Delivery      | Firebase Admin SDK                 |

Cloud Functions bertugas mengirimkan push notification ke seluruh anggota keluarga ketika terjadi aktivitas baru (transaksi, perubahan budget, dsb.), bahkan saat aplikasi mereka dalam kondisi tertutup.

---

## Struktur Project

```
Cashflow-Family/
├── app/                    Source code aplikasi Android (Kotlin, Compose)
├── functions/              Firebase Cloud Functions (TypeScript)
├── docs/                   Screenshot dan aset dokumentasi
├── Firestore-rules.txt     Konfigurasi Firestore Security Rules
└── README.md
```

> **Catatan:** Server push notification tambahan (`cashflow-push-server`) dikelola di repository terpisah dan tidak termasuk dalam repo ini.

---

## Setup

Beberapa file konfigurasi **tidak disertakan** dalam repository ini karena alasan keamanan. Berikut langkah setup manual yang diperlukan:

### 1. Firebase Configuration

- Buat project baru di [Firebase Console](https://console.firebase.google.com/)
- Aktifkan **Authentication**, **Firestore**, dan **Cloud Messaging**
- Unduh file `google-services.json` dan letakkan di direktori `app/`

### 2. Signing Configuration

- Siapkan keystore signing sendiri untuk keperluan build release
- Konfigurasikan pada `app/build.gradle.kts` atau file `keystore.properties`

### 3. Local Environment

- Buat file `local.properties` di root project
- Isi dengan path Android SDK lokal:
  ```
  sdk.dir=C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
  ```

### 4. Cloud Functions (Opsional)

- Siapkan service account Firebase Admin SDK sendiri
- **Jangan pernah** commit file kredensial (`serviceAccount.json`) ke repository

---

## Tampilan Aplikasi

<table>
  <tr>
    <td align="center"><b>Beranda</b></td>
    <td align="center"><b>Riwayat</b></td>
    <td align="center"><b>Tambah Transaksi</b></td>
  </tr>
  <tr>
    <td><img src="docs/Images/Home.jpeg" width="250"/></td>
    <td><img src="docs/Images/History.jpeg" width="250"/></td>
    <td><img src="docs/Images/Add%20Transaction.jpeg" width="250"/></td>
  </tr>
  <tr>
    <td align="center"><b>Setelan</b></td>
    <td align="center"><b>Tentang</b></td>
    <td></td>
  </tr>
  <tr>
    <td><img src="docs/Images/Settings.jpeg" width="250"/></td>
    <td><img src="docs/Images/About.jpeg" width="250"/></td>
    <td></td>
  </tr>
</table>

---

## Download

**Status:** Ready to download & install

Aplikasi tersedia dalam bentuk APK dan dapat diinstal langsung di perangkat Android (minimal Android 8.0).

<p align="center">
  <a href="https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.v1.0.0.apk">
    <img src="https://img.shields.io/badge/Download%20APK-v1.0.0-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download APK"/>
  </a>
</p>

**Cara Install:**

1. Unduh file APK melalui tombol di atas
2. Buka file APK pada perangkat Android
3. Jika diminta, aktifkan opsi **Install from Unknown Sources** di pengaturan
4. Ikuti proses instalasi hingga selesai
5. Login dengan akun Google dan mulai kelola keuangan keluarga

---

## Lisensi

Repository ini dilisensikan di bawah ketentuan yang tertera pada file [LICENSE](LICENSE).

Kode sumber dalam repository ini dapat dilihat secara publik untuk keperluan **portofolio dan referensi**. Namun, kode **tidak diperkenankan** untuk:

- Digunakan ulang dalam project komersial maupun non-komersial
- Dimodifikasi dan didistribusikan ulang
- Dijadikan turunan produk tanpa izin

Segala bentuk penggunaan di luar tujuan referensi memerlukan **izin tertulis** dari pemilik.

---

<p align="center">
  Dibuat dengan dedikasi oleh <a href="https://github.com/a7x-rudolf"><b>Ridolf Widi Alfisa Lumba</b></a>
</p>
