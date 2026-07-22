package com.app.cashflowfamily.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.cashflowfamily.ui.auth.LoginScreen
import com.app.cashflowfamily.ui.auth.RegisterScreen
import com.app.cashflowfamily.ui.auth.ForgotPasswordScreen
import com.app.cashflowfamily.ui.auth.TermsScreen
import com.app.cashflowfamily.ui.family.CreateFamilyScreen
import com.app.cashflowfamily.ui.family.CreateOrJoinFamilyScreen
import com.app.cashflowfamily.ui.family.FamilySuccessScreen
import com.app.cashflowfamily.ui.family.JoinFamilyScreen
import com.app.cashflowfamily.ui.main.MainScreen
import com.app.cashflowfamily.ui.splash.SplashScreen
import com.app.cashflowfamily.ui.transaction.AddTransactionScreen
import com.app.cashflowfamily.ui.transaction.TransactionDetailScreen
import com.app.cashflowfamily.ui.analytics.AnalyticsScreen
import com.app.cashflowfamily.ui.budget.BudgetScreen
import com.app.cashflowfamily.ui.onboarding.OnboardingScreen
import com.app.cashflowfamily.ui.recurring.AddRecurringScreen
import com.app.cashflowfamily.ui.recurring.RecurringListScreen
import com.app.cashflowfamily.ui.backup.BackupRestoreScreen
import com.app.cashflowfamily.ui.auth.BiometricLockScreen
import com.app.cashflowfamily.ui.notification.NotificationScreen
import com.app.cashflowfamily.ui.feedback.FeedbackScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
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
            val familyName = backStackEntry.arguments?.getString("familyName") ?: ""
            val familyCode = backStackEntry.arguments?.getString("familyCode") ?: ""
            FamilySuccessScreen(
                navController = navController,
                familyName = familyName,
                familyCode = familyCode
            )
        }

        // MAIN SCREEN (Bottom Nav Container)
        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }

        // Sub-screens (dibuka dari dalam MainScreen)
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController = navController)
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionDetailScreen(
                navController = navController,
                transactionId = transactionId
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

        // ===== FEEDBACK SCREEN =====
        composable(Screen.Feedback.route) {
            FeedbackScreen(navController = navController)
        }

    }
}