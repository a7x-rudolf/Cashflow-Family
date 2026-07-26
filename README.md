<div align="center">

# 💰 Cashflow Family — Android

**Family Financial Management Application**

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)]()
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.10.00-4285F4.svg)]()
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](LICENSE)
[![Status](https://img.shields.io/badge/status-Production%20Ready-brightgreen.svg)]()

*A comprehensive family financial management app that helps manage income, expenses, and monthly budgets together in one application.*

[Features](#-features) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [Screenshots](#-screenshots) • [Status](#-status) • [License](#-license)

</div>

---

<div align="center">

### 👨‍💻 Developer & Copyright Owner

**RIDOLF WIDI ALFISA LUMBA**

*Solo Developer, Architect & Copyright Holder*

Copyright © 2025 RIDOLF WIDI ALFISA LUMBA. All Rights Reserved.
Licensed under the [Proprietary License](LICENSE).

</div>

---

## 📖 Overview

**Cashflow Family** is a modern family financial management application built for Android. Designed to help families track income, expenses, and budgets collaboratively, it provides a seamless experience for managing household finances together.

Built with **Kotlin** and **Jetpack Compose**, the app follows **MVVM architecture** with **Firebase/Firestore** as the backend, ensuring real-time synchronization across family members.

---

## ✨ Features

### 📊 Financial Management
- **Transaction Recording** — Log income and expenses with detailed categories
- **Family Management** — Invite family members via unique invitation codes
- **Monthly Budgets** — Set and track budgets per category
- **Recurring Transactions** — Auto-schedule for salaries, bills, and subscriptions
- **Smart Reminders** — Daily notifications and budget alert warnings
- **Biometric Login** — Secure access with fingerprint authentication
- **Analytics Reports** — Comprehensive expense reports and family spending insights

### 💎 Premium User Experience
- **Modern UI** — Built with Jetpack Compose for smooth, responsive interfaces
- **Real-time Sync** — Instant updates across family members via Firestore
- **Secure Authentication** — Firebase Authentication with biometric support
- **Offline Capability** — Local caching for uninterrupted access
- **Intuitive Navigation** — Clean, user-friendly navigation patterns

---

## 🖼️ Screenshots

| Beranda | Riwayat Transaksi | Tambah Transaksi |
|:---:|:---:|:---:|
| <img src="docs/Images/Home.jpeg" width="220" alt="Beranda"> | <img src="docs/Images/History.jpeg" width="220" alt="Riwayat"> | <img src="docs/Images/Add%20Transaction.jpeg" width="220" alt="Tambah Transaksi"> |

| Pengaturan | Tentang |
|:---:|:---:|
| <img src="docs/Images/Settings.jpeg" width="220" alt="Pengaturan"> | <img src="docs/Images/About.jpeg" width="220" alt="Tentang"> |

---

## 💻 System Requirements

### Minimum Requirements
- **OS**: Android 7.0 (API 24) or later
- **RAM**: 2 GB
- **Storage**: 50 MB
- **Permissions**: Internet access, Fingerprint sensor (for biometric login)

### Recommended
- **OS**: Android 10 (API 29) or later
- **RAM**: 4 GB or more
- **Display**: 1080×1920 resolution or higher

---

## 📥 Installation

### Option 1: Direct APK Download (Recommended)
1. Download the latest APK from [Releases](https://github.com/a7x-rudolf/Cashflow-Family/releases/latest)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK and follow the installation prompts
4. Launch the app and sign in with your Firebase account

### Option 2: Build from Source
**Prerequisites:**
- Android Studio Hedgehog or later
- Firebase account with Firestore enabled

**Steps:**
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Download `google-services.json` and place it in the `app/` folder
3. Clone this repository
4. Open the project in Android Studio
5. Configure Firebase Authentication and Firestore Database
6. Build and run the app

**Important:** The `google-services.json` file and signing keystore are **not** included in this repository for security reasons. You must create your own Firebase configuration.

---

## 🏗️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 1.9.0 |
| **UI Framework** | Jetpack Compose 2024.10.00 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **DI Framework** | Hilt (Dagger) |
| **Backend** | Firebase Authentication + Firestore |
| **Local Storage** | Room Database |
| **Biometrics** | AndroidX Biometric |
| **IDE** | Android Studio Hedgehog |

---

## 🚀 Quick Start

1. **Install the app** via APK download
2. **Launch the app** and create a family group
3. **Invite members** using the generated invitation code
4. **Add transactions** — record income or expenses with categories
5. **Set budgets** — define monthly budget limits per category
6. **Monitor progress** — view analytics and spending reports
7. **Enable reminders** — set notification schedules for daily tracking

---

## 📚 Documentation

Comprehensive documentation is available in the repository:
- 📋 [Project Structure](docs/PROJECT_STRUCTURE.md) — Architecture and code organization
- 🗄️ [Database Schema](docs/DATABASE_SCHEMA.md) — Firestore data models (coming soon)
- 🔐 [Authentication Guide](docs/AUTHENTICATION.md) — Firebase Auth setup (coming soon)
- 📝 [Changelog](CHANGELOG.md) — Version history and updates

---

## ⚠️ Important Notice

Cashflow Family handles sensitive financial data including:
- Transaction records
- Family member information
- Budget settings
- User authentication credentials

**Security measures implemented:**
- Encrypted Firebase communication
- Biometric authentication support
- Secure local storage with encryption
- No sensitive data stored in plain text

**Please read the [License](LICENSE) before use.**

---

## 🤝 Contributing

This is currently a solo developer project. While the source code is public for portfolio/reference purposes, **modification, redistribution, or reuse without written permission is strictly prohibited** as per the license terms.

For bug reports and feature suggestions, please use the [Issues](../../issues) section.

---

## 📄 License

This project is licensed under a **Proprietary License** — see the [LICENSE](LICENSE) file for details.

**⚠️ Important:** The code is publicly viewable for portfolio/reference purposes but **may not be reused, modified, or redistributed** without explicit written permission from the owner.

---

## 🙏 Credits & Acknowledgments

### Development
- **Solo Developer & Architect**: **RIDOLF WIDI ALFISA LUMBA**
- **Copyright Owner**: RIDOLF WIDI ALFISA LUMBA © 2025
- **Project Type**: Independent Solo Developer Project

### Technologies & Libraries
- **UI Framework**: Jetpack Compose (Google)
- **DI Framework**: Hilt (Google)
- **Backend**: Firebase (Google)
- **Iconography**: Material Icons (Google)

---

## 📮 Contact & Support

- **Issues**: [GitHub Issues](../../issues)
- **Releases**: [GitHub Releases](https://github.com/a7x-rudolf/Cashflow-Family/releases)
- **Documentation**: [`docs/`](docs/) folder

---

<div align="center">

**Made with ❤️ by RIDOLF WIDI ALFISA LUMBA**

*Version 1.0.0 - Production Ready*

*Copyright © 2025 RIDOLF WIDI ALFISA LUMBA. All Rights Reserved.*

*Licensed under the Proprietary License.*

</div><div align="center">

# 💰 Cashflow Family — Android

**Family Financial Management Application**

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)]()
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.10.00-4285F4.svg)]()
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](LICENSE)
[![Status](https://img.shields.io/badge/status-Production%20Ready-brightgreen.svg)]()

*A comprehensive family financial management app that helps manage income, expenses, and monthly budgets together in one application.*

[Features](#-features) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [Screenshots](#-screenshots) • [Status](#-status) • [License](#-license)

</div>

---

<div align="center">

### 👨‍💻 Developer & Copyright Owner

**RIDOLF WIDI ALFISA LUMBA**

*Solo Developer, Architect & Copyright Holder*

Copyright © 2025 RIDOLF WIDI ALFISA LUMBA. All Rights Reserved.
Licensed under the [Proprietary License](LICENSE).

</div>

---

## 📖 Overview

**Cashflow Family** is a modern family financial management application built for Android. Designed to help families track income, expenses, and budgets collaboratively, it provides a seamless experience for managing household finances together.

Built with **Kotlin** and **Jetpack Compose**, the app follows **MVVM architecture** with **Firebase/Firestore** as the backend, ensuring real-time synchronization across family members.

---

## ✨ Features

### 📊 Financial Management
- **Transaction Recording** — Log income and expenses with detailed categories
- **Family Management** — Invite family members via unique invitation codes
- **Monthly Budgets** — Set and track budgets per category
- **Recurring Transactions** — Auto-schedule for salaries, bills, and subscriptions
- **Smart Reminders** — Daily notifications and budget alert warnings
- **Biometric Login** — Secure access with fingerprint authentication
- **Analytics Reports** — Comprehensive expense reports and family spending insights

### 💎 Premium User Experience
- **Modern UI** — Built with Jetpack Compose for smooth, responsive interfaces
- **Real-time Sync** — Instant updates across family members via Firestore
- **Secure Authentication** — Firebase Authentication with biometric support
- **Offline Capability** — Local caching for uninterrupted access
- **Intuitive Navigation** — Clean, user-friendly navigation patterns

---

## 🖼️ Screenshots

| Beranda | Riwayat Transaksi | Tambah Transaksi |
|:---:|:---:|:---:|
| <img src="docs/Images/Home.jpeg" width="220" alt="Beranda"> | <img src="docs/Images/History.jpeg" width="220" alt="Riwayat"> | <img src="docs/Images/Add%20Transaction.jpeg" width="220" alt="Tambah Transaksi"> |

| Pengaturan | Tentang |
|:---:|:---:|
| <img src="docs/Images/Settings.jpeg" width="220" alt="Pengaturan"> | <img src="docs/Images/About.jpeg" width="220" alt="Tentang"> |

---

## 💻 System Requirements

### Minimum Requirements
- **OS**: Android 7.0 (API 24) or later
- **RAM**: 2 GB
- **Storage**: 50 MB
- **Permissions**: Internet access, Fingerprint sensor (for biometric login)

### Recommended
- **OS**: Android 10 (API 29) or later
- **RAM**: 4 GB or more
- **Display**: 1080×1920 resolution or higher

---

## 📥 Installation

### Option 1: Direct APK Download (Recommended)
1. Download the latest APK from [Releases](https://github.com/a7x-rudolf/Cashflow-Family/releases/latest)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK and follow the installation prompts
4. Launch the app and sign in with your Firebase account

### Option 2: Build from Source
**Prerequisites:**
- Android Studio Hedgehog or later
- Firebase account with Firestore enabled

**Steps:**
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Download `google-services.json` and place it in the `app/` folder
3. Clone this repository
4. Open the project in Android Studio
5. Configure Firebase Authentication and Firestore Database
6. Build and run the app

**Important:** The `google-services.json` file and signing keystore are **not** included in this repository for security reasons. You must create your own Firebase configuration.

---

## 🏗️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 1.9.0 |
| **UI Framework** | Jetpack Compose 2024.10.00 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **DI Framework** | Hilt (Dagger) |
| **Backend** | Firebase Authentication + Firestore |
| **Local Storage** | Room Database |
| **Biometrics** | AndroidX Biometric |
| **IDE** | Android Studio Hedgehog |

---

## 🚀 Quick Start

1. **Install the app** via APK download
2. **Launch the app** and create a family group
3. **Invite members** using the generated invitation code
4. **Add transactions** — record income or expenses with categories
5. **Set budgets** — define monthly budget limits per category
6. **Monitor progress** — view analytics and spending reports
7. **Enable reminders** — set notification schedules for daily tracking

---

## 📚 Documentation

Comprehensive documentation is available in the repository:
- 📋 [Project Structure](docs/PROJECT_STRUCTURE.md) — Architecture and code organization
- 🗄️ [Database Schema](docs/DATABASE_SCHEMA.md) — Firestore data models (coming soon)
- 🔐 [Authentication Guide](docs/AUTHENTICATION.md) — Firebase Auth setup (coming soon)
- 📝 [Changelog](CHANGELOG.md) — Version history and updates

---

## ⚠️ Important Notice

Cashflow Family handles sensitive financial data including:
- Transaction records
- Family member information
- Budget settings
- User authentication credentials

**Security measures implemented:**
- Encrypted Firebase communication
- Biometric authentication support
- Secure local storage with encryption
- No sensitive data stored in plain text

**Please read the [License](LICENSE) before use.**

---

## 🤝 Contributing

This is currently a solo developer project. While the source code is public for portfolio/reference purposes, **modification, redistribution, or reuse without written permission is strictly prohibited** as per the license terms.

For bug reports and feature suggestions, please use the [Issues](../../issues) section.

---

## 📄 License

This project is licensed under a **Proprietary License** — see the [LICENSE](LICENSE) file for details.

**⚠️ Important:** The code is publicly viewable for portfolio/reference purposes but **may not be reused, modified, or redistributed** without explicit written permission from the owner.

---

## 🙏 Credits & Acknowledgments

### Development
- **Solo Developer & Architect**: **RIDOLF WIDI ALFISA LUMBA**
- **Copyright Owner**: RIDOLF WIDI ALFISA LUMBA © 2025
- **Project Type**: Independent Solo Developer Project

### Technologies & Libraries
- **UI Framework**: Jetpack Compose (Google)
- **DI Framework**: Hilt (Google)
- **Backend**: Firebase (Google)
- **Iconography**: Material Icons (Google)

---

## 📮 Contact & Support

- **Issues**: [GitHub Issues](../../issues)
- **Releases**: [GitHub Releases](https://github.com/a7x-rudolf/Cashflow-Family/releases)
- **Documentation**: [`docs/`](docs/) folder

---

<div align="center">

**Made with ❤️ by RIDOLF WIDI ALFISA LUMBA**

*Version 1.0.0 - Production Ready*

*Copyright © 2025 RIDOLF WIDI ALFISA LUMBA. All Rights Reserved.*

*Licensed under the Proprietary License.*

</div>
