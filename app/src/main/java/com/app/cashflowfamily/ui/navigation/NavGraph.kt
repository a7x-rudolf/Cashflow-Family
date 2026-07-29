package com.app.cashflowfamily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.cashflowfamily.ui.analytics.AnalyticsScreen
import com.app.cashflowfamily.ui.auth.BiometricLockScreen
import com.app.cashflowfamily.ui.auth.ForgotPasswordScreen
import com.app.cashflowfamily.ui.auth.LoginScreen
import com.app.cashflowfamily.ui.auth.RegisterScreen
import com.app.cashflowfamily.ui.auth.TermsScreen
import com.app.cashflowfamily.ui.backup.BackupRestoreScreen
import com.app.cashflowfamily.ui.budget.BudgetScreen
import com.app.cashflowfamily.ui.event.EventAddTransactionScreen
import com.app.cashflowfamily.ui.event.EventBudgetScreen
import com.app.cashflowfamily.ui.event.EventCreateScreen
import com.app.cashflowfamily.ui.event.EventDashboardScreen
import com.app.cashflowfamily.ui.event.EventEditScreen
import com.app.cashflowfamily.ui.event.EventEditTransactionScreen
import com.app.cashflowfamily.ui.event.EventReportScreen
import com.app.cashflowfamily.ui.event.EventTransactionScreen
import com.app.cashflowfamily.ui.event.EventTransferScreen
import com.app.cashflowfamily.ui.family.CreateFamilyScreen
import com.app.cashflowfamily.ui.family.CreateOrJoinFamilyScreen
import com.app.cashflowfamily.ui.family.FamilyManagementScreen
import com.app.cashflowfamily.ui.family.FamilySuccessScreen
import com.app.cashflowfamily.ui.family.JoinFamilyScreen
import com.app.cashflowfamily.ui.feedback.FeedbackScreen
import com.app.cashflowfamily.ui.main.MainScreen
import com.app.cashflowfamily.ui.notification.NotificationScreen
import com.app.cashflowfamily.ui.onboarding.OnboardingScreen
import com.app.cashflowfamily.ui.recurring.AddRecurringScreen
import com.app.cashflowfamily.ui.recurring.RecurringListScreen
import com.app.cashflowfamily.ui.splash.SplashScreen
import com.app.cashflowfamily.ui.transaction.AddTransactionScreen
import com.app.cashflowfamily.ui.transaction.TransactionDetailScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ===== EXISTING — TIDAK DIUBAH =====
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Terms.route) {
            TermsScreen(navController = navController)
        }
        composable(Screen.CreateOrJoinFamily.route) {
            CreateOrJoinFamilyScreen(navController = navController)
        }
        composable(Screen.CreateFamily.route) {
            CreateFamilyScreen(navController = navController)
        }
        composable(Screen.JoinFamily.route) {
            JoinFamilyScreen(navController = navController)
        }
        composable(Screen.RecurringList.route) {
            RecurringListScreen(navController = navController)
        }
        composable(Screen.AddRecurring.route) {
            AddRecurringScreen(navController = navController)
        }
        composable(
            route = Screen.FamilySuccess.route,
            arguments = listOf(
                navArgument("familyName") { type = NavType.StringType },
                navArgument("familyCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            FamilySuccessScreen(
                navController = navController,
                familyName = backStackEntry.arguments?.getString("familyName") ?: "",
                familyCode = backStackEntry.arguments?.getString("familyCode") ?: ""
            )
        }
        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController = navController)
        }
        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            TransactionDetailScreen(
                navController = navController,
                transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }
        composable(Screen.Budget.route) {
            BudgetScreen(navController = navController)
        }
        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(navController = navController)
        }
        composable(Screen.BiometricLock.route) {
            BiometricLockScreen(navController = navController)
        }
        composable(Screen.Notification.route) {
            NotificationScreen(navController = navController)
        }
        composable(Screen.Feedback.route) {
            FeedbackScreen(navController = navController)
        }
        composable(Screen.Family.route) {
            FamilyManagementScreen(rootNavController = navController)
        }

        // ===== BARU — Event Module Sub-Screens =====
        composable(Screen.EventCreate.route) {
            EventCreateScreen(navController = navController)
        }

        composable(
            route = Screen.EventDashboard.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventDashboardScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            )
        }

        composable(
            route = Screen.EventEdit.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventEditScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            )
        }

        composable(
            route = Screen.EventBudget.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventBudgetScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            )
        }

        composable(
            route = Screen.EventTransactions.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("categoryId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            EventTransactionScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: "",
                categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            )
        }

        composable(
            route = Screen.EventAddTransaction.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("categoryId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            EventAddTransactionScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: "",
                preselectedCategoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            )
        }
        composable(
            route = Screen.EventEditTransaction.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            EventEditTransactionScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: "",
                transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            )
        }

        composable(
            route = Screen.EventTransfer.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventTransferScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            )
        }

        composable(
            route = Screen.EventReport.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            EventReportScreen(
                navController = navController,
                eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            )
        }
    }
}