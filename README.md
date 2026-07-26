<h1 align="center">Cashflow Family — Android</h1>

<p align="center">
  Aplikasi manajemen keuangan keluarga yang membantu mengelola pemasukan, pengeluaran, dan anggaran bulanan bersama-sama dalam satu aplikasi.
</p>

---

## Fitur

- Catat transaksi (pemasukan dan pengeluaran) dengan kategori
- Kelola keluarga — undang anggota lewat kode undangan
- Budget bulanan per kategori
- Transaksi berulang (recurring) untuk gaji, tagihan, langganan
- Notifikasi pengingat harian dan peringatan budget
- Login dengan fingerprint/biometric
- Analitik dan laporan pengeluaran keluarga

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Hilt** untuk dependency injection
- **Firebase / Firestore** sebagai backend (users, families, transactions, budgets, recurring_transactions)
- **MVVM** architecture

## Setup

Project ini membutuhkan file konfigurasi Firebase (`app/google-services.json`) dan keystore signing yang **tidak** disertakan di repo ini karena alasan keamanan. Untuk menjalankan project:

1. Buat project Firebase sendiri di [Firebase Console](https://console.firebase.google.com/)
2. Download `google-services.json` dan taruh di folder `app/`
3. Isi `local.properties` dengan lokasi Android SDK kamu

## Tampilan Aplikasi

| Beranda | Riwayat Transaksi | Tambah Transaksi |
|:---:|:---:|:---:|
| <img src="docs/Images/Home.jpeg" width="220" alt="Beranda"> | <img src="docs/Images/History.jpeg" width="220" alt="Riwayat"> | <img src="docs/Images/Add%20Transaction.jpeg" width="220" alt="Tambah Transaksi"> |

| Pengaturan | Tentang |
|:---:|:---:|
| <img src="docs/Images/Settings.jpeg" width="220" alt="Pengaturan"> | <img src="docs/Images/About.jpeg" width="220" alt="Tentang"> |

## Status

Ready to download and install.

<p align="center">
  <a href="https://github.com/a7x-rudolf/Cashflow-Family/releases/latest">
    <img src="https://img.shields.io/badge/Download%20APK-v1.0.0-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
</p>

## License

Lihat file [LICENSE](https://github.com/a7x-rudolf/Cashflow-Family/blob/main/LICENSE). Kode ini bisa dilihat siapa saja untuk keperluan portofolio/referensi, tapi **tidak boleh dipakai ulang, dimodifikasi, atau didistribusikan** tanpa izin tertulis dari pemilik.

---

<p align="center">
  Dibuat oleh <a href="https://github.com/a7x-rudolf">Ridolf Widi Alfisa Lumba</a>
</p>
