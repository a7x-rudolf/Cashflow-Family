```
┌────────────────────────────────────────────────┐
│                                                  │
│   CASHFLOW FAMILY  —  ANDROID                   │
│                                                  │
└────────────────────────────────────────────────┘
```

Versi Android dari **Cashflow Family** — aplikasi manajemen keuangan keluarga. Satu tempat untuk mencatat pemasukan, pengeluaran, dan budget bulanan, dikelola bersama-sama oleh seluruh anggota keluarga.

<br>

### ────  fitur

```
catat transaksi pemasukan & pengeluaran, per kategori
kelola keluarga — undang anggota lewat kode undangan
budget bulanan per kategori
transaksi berulang — gaji, tagihan, langganan
notifikasi pengingat harian & peringatan budget
login dengan fingerprint / biometric
analitik & laporan pengeluaran keluarga
```

<br>

### ────  stack

`Kotlin` `Jetpack Compose` `Hilt` `Firebase / Firestore` `MVVM`

Firestore menyimpan struktur `users`, `families`, `transactions`, `budgets`, dan `recurring_transactions`.

<br>

### ────  menjalankan project

File konfigurasi Firebase dan keystore signing sengaja tidak disertakan di repo ini.

```
1. buat project Firebase sendiri di Firebase Console
2. unduh google-services.json, taruh di folder app/
3. isi local.properties dengan lokasi Android SDK kamu
```

<br>

### ────  tampilan

<div align="center">

| Beranda | Riwayat | Tambah Transaksi |
|:---:|:---:|:---:|
| <img src="docs/Screenshoot%20App/Beranda.jpeg" width="200"> | <img src="docs/Screenshoot%20App/Riwayat.jpeg" width="200"> | <img src="docs/Screenshoot%20App/Tambah-Transkasi.jpeg" width="200"> |

| Biometric | Notifikasi | Feedback |
|:---:|:---:|:---:|
| <img src="docs/Screenshoot%20App/Biometric.jpeg" width="200"> | <img src="docs/Screenshoot%20App/Notifikasi.jpeg" width="200"> | <img src="docs/Screenshoot%20App/Feedback.jpeg" width="200"> |

| Setelan | Tentang |
|:---:|:---:|
| <img src="docs/Screenshoot%20App/Setelan.jpeg" width="200"> | <img src="docs/Screenshoot%20App/Tentang.jpeg" width="200"> |

</div>

<br>

### ────  unduh

Aplikasi siap dipasang.

**[→ Download APK](https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.apk)**

<br>

### ────  lisensi

Kode ini terbuka untuk dilihat siapa saja sebagai bahan portofolio dan referensi. Tidak untuk dipakai ulang, dimodifikasi, atau didistribusikan tanpa izin tertulis dari pemilik — detail lengkap di [LICENSE](LICENSE).

<br>

---

<div align="center">

dibuat oleh **[Ridolf Widi Alfisa Lumba](https://github.com/a7x-rudolf)**

</div>
