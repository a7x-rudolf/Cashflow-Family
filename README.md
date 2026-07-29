# Cashflow Family — Android

> Ini adalah versi Android dari aplikasi Cashflow Family — aplikasi manajemen keuangan keluarga.

Cashflow Family membantu keluarga mengelola pemasukan, pengeluaran, dan budget bulanan bersama-sama dalam satu aplikasi.

## Fitur Utama

- Pencatatan Transaksi: Catat pemasukan dan pengeluaran harian dengan kategori terstruktur.
- Kolaborasi Keluarga: Undang anggota keluarga menggunakan kode unik untuk memantau keuangan bersama.
- Anggaran Bulanan: Tetapkan dan pantau batas budget per kategori agar pengeluaran tetap terkontrol.
- Transaksi Berulang (Recurring): Otomatisasi pencatatan untuk gaji, tagihan bulanan, atau langganan rutin.
- Notifikasi dan Pengingat: Dapatkan pengingat harian serta peringatan jika budget mulai menipis.
- Keamanan Biometrik: Dukungan login menggunakan kunci sidik jari atau enkripsi lokal yang aman.
- Analitik dan Laporan: Visualisasi data keuangan dalam bentuk grafik yang interaktif dan mudah dibaca.

## Tech Stack

- UI Framework: Jetpack Compose (Modern Declarative UI)
- Architecture: MVVM (Model-View-ViewModel) + Repository Pattern
- Dependency Injection: Hilt
- Backend dan Database: Firebase (Firestore, Authentication, Cloud Messaging)
- Language: 100% Kotlin

## Panduan Setup Lokal

Project ini memerlukan konfigurasi Firebase pribadi dan keystore penandatanganan untuk alasan keamanan. Ikuti langkah berikut untuk menjalankan project di komputer lokal:

1. Clone repository ini:
   git clone https://github.com/a7x-rudolf/Cashflow-Family.git
2. Konfigurasi Firebase:
   - Buat project baru melalui Firebase Console.
   - Unduh file google-services.json dan letakkan di dalam folder app/.
3. Konfigurasi Lokal:
   - Sesuaikan path direktori Android SDK Anda di dalam file local.properties.
4. Build dan Run:
   - Buka project menggunakan Android Studio dan sinkronkan dengan Gradle.

## Tampilan Antarmuka (Screenshots)

| Home (Beranda) | Add Transaction | History |
| :---: | :---: | :---: |
| ![Home](docs/Images/Home.jpeg) | ![Add Transaction](docs/Images/Add%20Transaction.jpeg) | ![History](docs/mages/History.jpeg) |

| Settings | About |
| :---: | :---: |
| ![Settings](docs/Images/Settings.jpeg) | ![About](docs/Images/About.jpeg) |

## Download dan Instalasi

Anda dapat mengunduh versi stabil terbaru aplikasi ini langsung melalui halaman rilis repository:

[Unduh Cashflow Family APK (Latest Release)](https://github.com/a7x-rudolf/Cashflow-Family/releases/latest)

## License dan Kebijakan Hak Cipta

Lihat detail lengkap pada file [LICENSE](LICENSE).

Repository ini dipublikasikan untuk keperluan portofolio dan referensi edukasi. Tidak diizinkan untuk mendistribusikan ulang, memodifikasi secara komersial, atau mengklaim ulang tanpa izin tertulis dari pembuat.

---

Dibuat oleh Ridolf Widi Alfisa Lumba (https://github.com/a7x-rudolf)
