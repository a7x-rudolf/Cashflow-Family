# Cashflow Family — Android

> 📱 Aplikasi Android untuk manajemen keuangan keluarga.

Cashflow Family membantu keluarga mengelola pemasukan, pengeluaran, dan budget bulanan bersama-sama dalam satu aplikasi, dengan sinkronisasi real-time antar anggota keluarga lewat Firebase.

## Fitur

- 💰 Catat transaksi (pemasukan & pengeluaran) dengan kategori
- 👨‍👩‍👧 Kelola keluarga — undang anggota lewat kode keluarga
- 📊 Budget bulanan per kategori, dengan peringatan saat mendekati/melewati batas
- 🔁 Transaksi berulang (recurring) untuk gaji, tagihan, langganan
- 🔔 Notifikasi push real-time (Firebase Cloud Messaging) untuk aktivitas keluarga & peringatan budget
- 🔐 Login dengan fingerprint/biometric
- 🔄 Pengecekan update aplikasi otomatis (in-app update checker)

## Tech Stack

**Android App**
- Kotlin + Jetpack Compose (Material 3)
- Hilt untuk dependency injection
- Firebase Auth, Firestore, Cloud Messaging
- Coil untuk image loading
- MVVM architecture
- Min SDK 26, Target SDK 35

**Backend**
- Firebase Cloud Functions (TypeScript) — mengirim push notification ke anggota keluarga saat ada transaksi/aktivitas baru, termasuk saat app mereka tertutup
- Firestore Security Rules (lihat [`Firestore-rules.txt`](Firestore-rules.txt))

## Struktur Project

```
app/            -> source code Android app (Kotlin, Jetpack Compose)
functions/      -> Firebase Cloud Functions (TypeScript) untuk push notification
docs/           -> screenshot & aset dokumentasi
```

> Catatan: server push notification tambahan (`cashflow-push-server`) dikelola di repo terpisah, tidak termasuk di sini.

## Setup

Project ini butuh file konfigurasi yang **tidak** disertakan di repo ini karena alasan keamanan:

1. Buat project di [Firebase Console](https://console.firebase.google.com/), lalu download `google-services.json` dan taruh di folder `app/`
2. Siapkan keystore signing sendiri untuk build release
3. Isi `local.properties` dengan lokasi Android SDK di mesin kamu
4. Untuk Cloud Functions, siapkan service account Firebase Admin SDK sendiri (jangan commit file kredensial ke repo)

## Tampilan Aplikasi

| Beranda | Riwayat | Tambah Transaksi |
|---|---|---|
| ![Beranda](docs/Images/Home.jpeg) | ![Riwayat](docs/Images/History.jpeg) | ![Tambah Transaksi](docs/Images/Add%20Transaction.jpeg) |

| Setelan | Tentang |
|---|---|
| ![Setelan](docs/Images/Settings.jpeg) | ![Tentang](docs/Images/About.jpeg) |

## Status

Ready to download & install

[📥 Download APK](https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.v1.0.0.apk)

## License

Lihat file [LICENSE](LICENSE). Kode ini bisa dilihat siapa saja untuk keperluan portofolio/referensi, tapi **tidak boleh dipakai ulang, dimodifikasi, atau didistribusikan** tanpa izin tertulis dari pemilik.

---

Dibuat oleh [Ridolf Widi Alfisa Lumba](https://github.com/a7x-rudolf)
