# Cashflow Family — Android

Versi Android dari **Cashflow Family**, aplikasi manajemen keuangan keluarga. Cashflow Family membantu keluarga mencatat pemasukan, pengeluaran, dan budget bulanan secara kolaboratif dalam satu aplikasi, dengan data yang tersinkron real-time antar anggota keluarga lewat Firebase.

## Fitur

**Transaksi**
- Catat pemasukan dan pengeluaran dengan kategori
- Transaksi berulang (recurring) untuk gaji, tagihan, dan langganan
- Riwayat transaksi lengkap dengan filter

**Keluarga**
- Buat atau gabung ke grup keluarga lewat kode undangan
- Data transaksi, budget, dan anggaran tersinkron ke seluruh anggota keluarga secara real-time

**Budget & Analitik**
- Budget bulanan per kategori
- Analitik dan laporan pengeluaran keluarga
- Export laporan ke PDF dan CSV

**Notifikasi**
- Pengingat harian
- Peringatan saat budget mendekati atau melewati batas

**Akun & Keamanan**
- Login dengan email/password atau Google
- Kunci aplikasi dengan fingerprint / biometric
- Backup dan restore data

**Lainnya**
- Pengecekan update aplikasi otomatis lewat GitHub Releases
- Form feedback langsung dari dalam aplikasi

## Tech Stack

| | |
|---|---|
| UI | Kotlin, Jetpack Compose |
| Arsitektur | MVVM |
| Dependency Injection | Hilt |
| Backend | Firebase Firestore, Firebase Auth |
| Background task | WorkManager |
| Local storage | DataStore Preferences |
| Charting | YCharts |
| Image loading | Coil |
| Async | Kotlin Coroutines |

Struktur data di Firestore: `users`, `families`, `transactions`, `budgets`, `recurring_transactions`.

## Persiapan Menjalankan Proyek

File konfigurasi Firebase dan keystore signing tidak disertakan di repo ini karena alasan keamanan.

1. Buat project Firebase sendiri di [Firebase Console](https://console.firebase.google.com/)
2. Unduh `google-services.json` dan taruh di folder `app/`
3. Isi `local.properties` dengan lokasi Android SDK kamu

## Tampilan Aplikasi

| Beranda | Riwayat Transaksi | Tambah Transaksi |
|---|---|---|
| ![Beranda](docs/Screenshoot%20App/Beranda.jpeg) | ![Riwayat](docs/Screenshoot%20App/Riwayat.jpeg) | ![Tambah Transaksi](docs/Screenshoot%20App/Tambah-Transkasi.jpeg) |

| Login Biometric | Notifikasi | Feedback |
|---|---|---|
| ![Biometric](docs/Screenshoot%20App/Biometric.jpeg) | ![Notifikasi](docs/Screenshoot%20App/Notifikasi.jpeg) | ![Feedback](docs/Screenshoot%20App/Feedback.jpeg) |

| Setelan | Tentang |
|---|---|
| ![Setelan](docs/Screenshoot%20App/Setelan.jpeg) | ![Tentang](docs/Screenshoot%20App/Tentang.jpeg) |

## Unduh

Aplikasi siap dipasang.

[Download APK](https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.apk)

## License

Lihat file [LICENSE](LICENSE). Kode ini terbuka untuk dilihat siapa saja sebagai bahan portofolio dan referensi, tapi tidak boleh dipakai ulang, dimodifikasi, atau didistribusikan tanpa izin tertulis dari pemilik.

---

Dibuat oleh [Ridolf Widi Alfisa Lumba](https://github.com/a7x-rudolf)
