package com.app.cashflowfamily.ui.navigation

sealed class Screen(val route: String) {
    // ===== EXISTING — TIDAK DIUBAH =====
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Terms : Screen("terms")
    object CreateOrJoinFamily : Screen("create_or_join_family")
    object CreateFamily : Screen("create_family")
    object JoinFamily : Screen("join_family")
    object FamilySuccess : Screen("family_success/{familyName}/{familyCode}") {
        fun createRoute(familyName: String, familyCode: String) =
            "family_success/$familyName/$familyCode"
    }
    object Main : Screen("main")
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object AddTransaction : Screen("add_transaction")
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
    object Analytics : Screen("analytics")
    object Budget : Screen("budget")
    object RecurringList : Screen("recurring_list")
    object AddRecurring : Screen("add_recurring")
    object BackupRestore : Screen("backup_restore")
    object BiometricLock : Screen("biometric_lock")
    object Notification : Screen("notification")
    object Feedback : Screen("feedback")

    // ===== REMOVED dari bottom nav (opsi C) =====
    // Family masih ada sebagai route tapi tidak di bottom nav
    object Family : Screen("family")

    // ===== BARU — Event Module =====
    object EventList : Screen("event_list")

    object EventCreate : Screen("event_create")

    object EventDashboard : Screen("event_dashboard/{eventId}") {
        fun createRoute(eventId: String) = "event_dashboard/$eventId"
    }

    object EventEdit : Screen("event_edit/{eventId}") {
        fun createRoute(eventId: String) = "event_edit/$eventId"
    }

    object EventBudget : Screen("event_budget/{eventId}") {
        fun createRoute(eventId: String) = "event_budget/$eventId"
    }

    object EventTransactions : Screen("event_transactions/{eventId}?categoryId={categoryId}") {
        fun createRoute(eventId: String, categoryId: String = "") =
            "event_transactions/$eventId?categoryId=$categoryId"
    }

    object EventAddTransaction : Screen("event_add_transaction/{eventId}?categoryId={categoryId}") {
        fun createRoute(eventId: String, categoryId: String = "") =
            "event_add_transaction/$eventId?categoryId=$categoryId"
    }

    object EventTransfer : Screen("event_transfer/{eventId}") {
        fun createRoute(eventId: String) = "event_transfer/$eventId"
    }

    object EventReport : Screen("event_report/{eventId}") {
        fun createRoute(eventId: String) = "event_report/$eventId"
    }
    object EventEditTransaction : Screen("event_edit_transaction/{eventId}/{transactionId}") {
        fun createRoute(eventId: String, transactionId: String) =
            "event_edit_transaction/$eventId/$transactionId"
    }
}