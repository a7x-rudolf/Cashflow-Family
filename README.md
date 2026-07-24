<h1 align="center">Cashflow Family</h1>

<p align="center">
  <strong>Family Finance Management App for Android</strong><br>
  Manage income, expenses, budgets, and family finances collaboratively in one application.
</p>

<p align="center">
  <a href="https://github.com/a7x-rudolf/Cashflow-Family/releases/latest">
    <img src="https://img.shields.io/github/v/release/a7x-rudolf/Cashflow-Family?label=Latest%20Release" alt="Latest Release">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack-Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Architecture-MVVM-blue" alt="MVVM">
  <img src="https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase" alt="Firebase">
  <img src="https://img.shields.io/badge/License-Custom-red" alt="License">
</p>

---

## Overview

Cashflow Family is an Android application designed to help families manage their finances together. The application enables family members to record income and expenses, monitor monthly budgets, receive reminders, and view financial reports in one centralized platform.

Whether managing household expenses, shared savings, or monthly bills, Cashflow Family helps every family member stay informed with synchronized financial data and real-time activity updates.

---

## Features

### Transaction Management
- Record income and expenses.
- Custom transaction categories.
- Complete transaction history.
- Transaction detail view.
- Search and filter transactions.

### Family Management
- Create family groups.
- Invite members using invitation codes.
- Shared financial management.
- Member role management.

### Monthly Budget
- Create monthly budgets for each category.
- Monitor budget usage in real time.
- Receive warnings when spending approaches or exceeds the budget.

### Recurring Transactions
Schedule recurring transactions such as:
- Salary
- Bills
- Subscriptions
- Installments
- Other routine expenses

### Realtime Notifications
- Receive notifications when family members add, edit, or delete transactions.
- Instant updates for budget changes.
- Activity synchronization across family members.

### Reminder Notifications
- Daily transaction reminders.
- Upcoming recurring transaction reminders.
- Budget limit warnings.

### Security
- PIN authentication.
- Fingerprint / Biometric login.
- Secure Firebase Authentication.

### Analytics & Reports
- Income and expense summaries.
- Current balance overview.
- Category-based statistics.
- Financial trend charts.
- Monthly reports.

### Auto Update
- Automatic update checking.
- Notification when a new version is available.
- Easy application updates without manually checking releases.

---

## Screenshots

| Home | Transaction History | Add Transaction |
|------|----------------------|-----------------|
| ![](docs/screenshots/beranda.jpeg) | ![](docs/screenshots/riwayat.jpeg) | ![](docs/screenshots/tambah-transaksi.jpeg) |

| Monthly Budget | Recurring Transactions | Family |
|----------------|-------------------------|---------|
| ![](docs/screenshots/budget.jpeg) | ![](docs/screenshots/recurring.jpeg) | ![](docs/screenshots/keluarga.jpeg) |

| Settings | Promotion |
|----------|-----------|
| ![](docs/screenshots/setelan.jpeg) | ![](docs/screenshots/promo.jpeg) |

---

## Download

Download the latest APK from the GitHub Releases page.

**Latest Release**

https://github.com/a7x-rudolf/Cashflow-Family/releases/latest

---

## Tech Stack

### Language
- Kotlin

### UI
- Jetpack Compose
- Material Design 3
- Navigation Compose

### Architecture
- MVVM (Model–View–ViewModel)
- Repository Pattern
- Hilt Dependency Injection

### Backend
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging (FCM)

### Android Jetpack
- ViewModel
- StateFlow
- Coroutines
- DataStore
- WorkManager

### Image Loading
- Coil

---

## Project Structure

```text
app/
├── data/
│   ├── local/
│   ├── remote/
│   ├── model/
│   └── repository/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── presentation/
│   ├── home/
│   ├── transaction/
│   ├── budget/
│   ├── recurring/
│   ├── family/
│   ├── settings/
│   └── auth/
│
├── navigation/
├── ui/
├── di/
└── utils/
```

---

## Firebase Collections

```text
users
families
transactions
budgets
recurring_transactions
notifications
```

---

## Setup

This repository does **not** include Firebase configuration files or signing keys for security reasons.

### Requirements

- Android Studio Hedgehog or newer
- Android SDK
- JDK 17+
- Firebase Project

### Installation

1. Clone this repository.

```bash
git clone https://github.com/a7x-rudolf/Cashflow-Family.git
```

2. Create your own Firebase project.

3. Register an Android application.

4. Download

```text
google-services.json
```

5. Copy it into

```text
app/google-services.json
```

6. Configure your Android SDK path in

```text
local.properties
```

7. Build and run the project.

---

## Roadmap

- [x] Authentication
- [x] Family Management
- [x] Transactions
- [x] Categories
- [x] Monthly Budget
- [x] Recurring Transactions
- [x] Realtime Notifications
- [x] Reminder Notifications
- [x] Analytics
- [x] Auto Update
- [ ] Export PDF
- [ ] Export Excel
- [ ] Multi Currency
- [ ] Widgets
- [ ] Wear OS Support

---

## Contributing

This repository is published as a portfolio project.

Bug reports and feature suggestions are welcome through the GitHub Issues page.

Pull Requests are currently not accepted.

---

## License

See the [LICENSE](LICENSE) file.

This repository is publicly available for documentation, learning, and portfolio purposes only.

You may **not** copy, reuse, modify, redistribute, republish, or use any part of this source code in another project without prior written permission from the copyright owner.

---

## Developer

**Ridolf Widi Alfisa Lumba**

IT Support • Android Developer • Software Engineer

GitHub:
https://github.com/a7x-rudolf

---

<p align="center">
Made with ❤️ in Indonesia
</p>
````
