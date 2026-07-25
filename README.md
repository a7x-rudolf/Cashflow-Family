<div align="center">

# Cashflow Family — Android

Aplikasi manajemen keuangan keluarga yang dirancang untuk mengelola pemasukan, pengeluaran, dan anggaran bulanan secara kolaboratif bersama seluruh anggota keluarga.

[Unduh APK](#-unduh) · [Fitur](#-fitur) · [Teknologi](#-teknologi) · [Instalasi](#-persiapan-menjalankan-proyek)

</div>

---

## Fitur Utama

### Transaksi
* Pencatatan transaksi pemasukan dan pengeluaran dengan sistem kategori.
* Transaksi berulang otomatis untuk gaji, tagihan rutin, dan langganan.
* Riwayat transaksi lengkap dengan opsi filter lanjutan.

### Keluarga dan Anggaran
* Manajemen anggota keluarga melalui sistem undangan menggunakan kode unik.
* Sinkronisasi data secara real-time ke seluruh perangkat anggota keluarga.
* Pengaturan anggaran bulanan berdasarkan kategori pengeluaran.
* Analitik dan laporan ringkas pengeluaran keluarga.
* Ekspor laporan keuangan ke format PDF dan CSV.

### Keamanan dan Sistem
* Notifikasi pengingat harian dan peringatan batas anggaran.
* Autentikasi pengguna menggunakan email/password atau akun Google.
* Pengamanan akses aplikasi dengan pemindai sidik jari atau biometrik.
* Cadangkan dan pulihkan data secara berkala.
* Pemeriksaan pembaruan aplikasi otomatis melalui GitHub Releases.
* Formulir masukan dan laporan kendala langsung dari dalam aplikasi.

---

## Teknologi yang Digunakan

* **Bahasa & UI:** Kotlin, Jetpack Compose
* **Arsitektur:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** Hilt
* **Backend & Layanan:** Firebase Firestore, Firebase Authentication
* **Latar Belakang & Penyimpanan:** WorkManager, DataStore Preferences
* **Visualisasi & Media:** YCharts, Coil Async, Kotlin Coroutines
* **Struktur Database (Firestore):** `users`, `families`, `transactions`, `budgets`, `recurring_transactions`

---

## Persiapan Menjalankan Proyek

> Catatan: File konfigurasi Firebase dan *keystore signing* tidak disertakan di dalam repositori ini demi keamanan.

1. Buat dan konfigurasikan proyek baru melalui [Firebase Console](https://console.firebase.google.com/).
2. Unduh file konfigurasi `google-services.json` dan letakkan ke dalam direktori `app/`.
3. Buat dan isi file `local.properties` pada root direktori proyek untuk mendefinisikan lokasi Android SDK Anda.

---

## Tampilan Aplikasi

<div align="center">

| Beranda | Riwayat Transaksi | Tambah Transaksi |
| :---: | :---: | :---: |
| <img src="docs/Screenshoot%20App/Beranda.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Riwayat.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Tambah-Transkasi.jpeg" width="220"> |

| Login Biometric | Notifikasi | Feedback |
| :---: | :---: | :---: |
| <img src="docs/Screenshoot%20App/Biometric.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Notifikasi.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Feedback.jpeg" width="220"> |

| Pengaturan | Tentang |
| :---: | :---: |
| <img src="docs/Screenshoot%20App/Setelan.jpeg" width="220"> | <img src="docs/Screenshoot%20App/Tentang.jpeg" width="220"> |

</div>

---

## Unduh

Aplikasi siap diunduh dan dipasang pada perangkat Android melalui halaman [GitHub Releases](https://github.com/username/repository/releases). *(Sesuaikan tautan dengan repositori Anda)*

---

## Lisensi

Kode ini tersedia untuk tujuan portofolio dan referensi akademik maupun profesional. Penggunaan ulang, modifikasi, atau distribusi tanpa izin tertulis dari pemilik tidak diperbolehkan.

---

<div align="center">

Dibuat oleh **Ridolf Widi Alfisa Lumba**

</div>
