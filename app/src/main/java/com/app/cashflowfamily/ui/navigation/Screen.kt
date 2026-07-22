package com.app.cashflowfamily.ui.navigation

sealed class Screen(val route: String) {
    // Splash & Onboarding
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")

    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Terms : Screen("terms")

    // Family Setup
    object CreateOrJoinFamily : Screen("create_or_join_family")
    object CreateFamily : Screen("create_family")
    object JoinFamily : Screen("join_family")

    object FamilySuccess : Screen("family_success/{familyName}/{familyCode}") {
        fun createRoute(familyName: String, familyCode: String): String {
            return "family_success/$familyName/$familyCode"
        }
    }

    // MAIN SCREEN
    object Main : Screen("main")

    // Bottom Nav Tabs
    object Home : Screen("home")
    object History : Screen("history")
    object Family : Screen("family")
    object Settings : Screen("settings")

    // Sub-screens
    object AddTransaction : Screen("add_transaction")

    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String): String {
            return "transaction_detail/$transactionId"
        }
    }

    object Analytics : Screen("analytics")
    object Budget : Screen("budget")
    object RecurringList : Screen("recurring_list")
    object AddRecurring : Screen("add_recurring")
    object BackupRestore : Screen("backup_restore")

    // Biometric Lock
    object BiometricLock : Screen("biometric_lock")

    // ===== Notification =====
    object Notification : Screen("notification")

    // ===== FEEDBACK =====
    object Feedback : Screen("feedback")

}