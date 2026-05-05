package com.example.plexus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plexus.ui.screens.ChatScreen
import com.example.plexus.ui.theme.HomeScreen
import com.example.plexus.ui.theme.LoginScreen
import com.example.plexus.ui.theme.OtpScreen
import com.example.plexus.ui.theme.ProfileScreen
import com.example.plexus.ui.theme.SplashScreen

// ─── Routes ───────────────────────────────────────────
object Routes {
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val OTP      = "otp/{phoneNumber}"
    const val HOME     = "home"
    const val CHAT     = "chat/{contactId}/{contactName}"
    const val PROFILE  = "profile"

    // Helper functions to build routes with arguments
    fun otp(phoneNumber: String)                       = "otp/$phoneNumber"
    fun chat(contactId: String, contactName: String)   = "chat/$contactId/$contactName"
}

// ─── NavGraph ─────────────────────────────────────────
@Composable
fun PlexusNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        // Splash
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true } // remove splash from backstack
                    }
                }
            )
        }

        // Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onContinue = { phoneNumber ->
                    navController.navigate(Routes.otp(phoneNumber))
                }
            )
        }

        // OTP — receives phoneNumber from login
        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onVerified = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // clear login + otp from backstack
                    }
                },
                onResend = {
                    // call your ViewModel resend function here
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home
        composable(Routes.HOME) {
            HomeScreen(
                chats = emptyList(), // pass real list from ViewModel here
                onChatClick = { contactId ->
                    navController.navigate(Routes.chat(contactId, "Contact"))
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onNewChat = {
                    // open new chat dialog or screen
                }
            )
        }

        // Chat — receives contactId and contactName
        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("contactId")   { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contactId   = backStackEntry.arguments?.getString("contactId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            ChatScreen(
                contactName = contactName,
                isOnline = false,      // pass from ViewModel
                isLocal = false,       // pass from ViewModel
                messages = emptyList(), // pass from ViewModel
                onSendMessage = { text ->
                    // call ViewModel sendMessage(text) here
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = "Varun",        // pass from ViewModel
                phoneNumber = "+91 XXXXX XXXXX", // pass from ViewModel
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}