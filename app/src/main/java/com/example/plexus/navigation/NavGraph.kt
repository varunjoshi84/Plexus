package com.example.plexus.navigation

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plexus.ui.screens.*
import com.example.plexus.viewmodel.AuthState
import com.example.plexus.viewmodel.AuthViewModel
import com.example.plexus.viewmodel.ChatViewModel
import com.example.plexus.viewmodel.LocalChatViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.plexus.data.model.UserModel
import com.example.plexus.utils.TimeUtils
import kotlinx.coroutines.tasks.await
import com.example.plexus.ui.theme.PlexusColors

object Routes {
    const val SPLASH        = "splash"
    const val LOGIN         = "login"
    const val OTP           = "otp/{phoneNumber}"
    const val HOME          = "home"
    const val COMPLETE_PROFILE = "complete_profile"
    const val CHAT          = "chat/{contactId}/{contactName}"
    const val PROFILE       = "profile"
    const val EDIT_PROFILE  = "edit_profile"
    const val LOCAL_DEVICES = "local_devices"
    const val NEW_CHAT = "new_chat"

    fun otp(phoneNumber: String)                     = "otp/$phoneNumber"
    fun chat(contactId: String, contactName: String) = "chat/$contactId/$contactName"
    const val LOCAL_CHAT = "local_chat/{deviceName}/{deviceHost}"
    fun localChat(deviceName: String, deviceHost: String) = "local_chat/$deviceName/$deviceHost"
}

@Composable
fun PlexusNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // declare all ViewModels once at the top
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    val localViewModel: LocalChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn) Routes.HOME else Routes.SPLASH,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(300)
            ) + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(300)
            ) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(300)
            ) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(300)
            ) + fadeOut(tween(300))
        }
    ) {

        // Splash
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    val dest = if (authViewModel.isLoggedIn) Routes.HOME else Routes.LOGIN
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onContinue = { phoneNumber ->
                    authViewModel.sendOtp(phoneNumber, context as Activity)
                    navController.navigate(Routes.otp(phoneNumber))
                }
            )
        }

        // OTP
        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Verified -> {
                        chatViewModel.saveFcmToken()
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                        authViewModel.resetState()
                    }
                    is AuthState.NeedsProfile -> {
                        navController.navigate(Routes.COMPLETE_PROFILE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }

            OtpScreen(
                phoneNumber = phoneNumber,
                onVerified = { otpCode ->
                    authViewModel.verifyOtp(otpCode)
                },
                onResend = {
                    authViewModel.sendOtp(phoneNumber, context as Activity)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Complete Profile
        composable(Routes.COMPLETE_PROFILE) {
            val authState by authViewModel.authState.collectAsState()
            
            CompleteProfileScreen(
                onComplete = { username, displayName, bio ->
                    authViewModel.saveUserProfile(username, displayName, bio)
                },
                errorMessage = (authState as? AuthState.Error)?.message,
                isLoading = authState is AuthState.Loading
            )

            LaunchedEffect(authState) {
                if (authState is AuthState.Verified) {
                    chatViewModel.saveFcmToken()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                    authViewModel.resetState()
                }
            }
        }

        // Home
        composable(Routes.HOME) {
            val chats by chatViewModel.chats.collectAsState()
            val profiles by chatViewModel.participantProfiles.collectAsState()

            LaunchedEffect(Unit) {
                chatViewModel.listenToChats()
            }

            HomeScreen(
                chats = chats.map { chat ->
                    // Resolve the "other" participant's name
                    val otherUid = chat.participants.firstOrNull { it != chatViewModel.currentUserId }
                    val otherProfile = profiles[otherUid]
                    val displayName = otherProfile?.displayName?.ifEmpty { otherProfile.username }
                        ?: otherUid?.take(8) // Fallback to start of UID if profile not loaded
                        ?: "Unknown"

                    ChatPreviewUiModel(
                        id = chat.chatId,
                        name = displayName,
                        lastMessage = chat.lastMessage,
                        time = TimeUtils.formatChatPreviewTime(chat.lastTime),
                        isOnline = otherProfile?.isOnline ?: false,
                        isLocal = false
                    )
                },
                onChatClick = { contactId ->
                    // Navigate with current resolved name if possible
                    val otherUid = chats.find { it.chatId == contactId }?.participants?.firstOrNull { it != chatViewModel.currentUserId }
                    val name = profiles[otherUid]?.displayName?.ifEmpty { profiles[otherUid]?.username } ?: "Chat"
                    navController.navigate(Routes.chat(contactId, name))
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onNewChat = {
                    navController.navigate(Routes.NEW_CHAT)
                }
            )
        }

        // Chat
        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("contactId")   { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId      = backStackEntry.arguments?.getString("contactId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            val messages by chatViewModel.messages.collectAsState()

            LaunchedEffect(chatId) {
                chatViewModel.listenToMessages(chatId)
            }

            ChatScreen(
                contactName = contactName,
                isOnline = false,
                isLocal = false,
                messages = messages.map { msg ->
                    MessageUiModel(
                        id = msg.id,
                        text = msg.text,
                        isMine = msg.senderId == chatViewModel.currentUserId,
                        time = TimeUtils.formatMessageTime(msg.timestamp),
                        status = MessageStatus.SENT
                    )
                },
                onSendMessage = { text ->
                    chatViewModel.sendMessage(chatId, text)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = authViewModel.currentUser?.displayName
                    ?: authViewModel.currentUser?.phoneNumber
                    ?: "User",
                phoneNumber = authViewModel.currentUser?.phoneNumber ?: "",
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()  // ✅ now accessible
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true } // clear entire backstack
                    }
                },
                onLocalNetworkClick = {
                    navController.navigate(Routes.LOCAL_DEVICES)
                },
                onInternetModeClick = { },
                onEditProfileClick = { 
                    navController.navigate(Routes.EDIT_PROFILE)
                },
                onNotificationsClick = { }
            )
        }

        // Edit Profile
        composable(Routes.EDIT_PROFILE) {
            val authState by authViewModel.authState.collectAsState()
            
            var userProfile by remember { mutableStateOf<UserModel?>(null) }
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val uid = authViewModel.currentUser?.uid ?: ""

            LaunchedEffect(uid) {
                if (uid.isNotEmpty()) {
                    try {
                        val doc = db.collection("users").document(uid).get().await()
                        userProfile = doc.toObject(UserModel::class.java)
                    } catch (e: Exception) {
                        // handle error
                    }
                }
            }

            if (userProfile != null) {
                EditProfileScreen(
                    currentName = userProfile?.displayName ?: "",
                    currentUsername = userProfile?.username ?: "",
                    onUpdate = { username, displayName ->
                        authViewModel.updateUserProfile(username, displayName)
                    },
                    onBack = { navController.popBackStack() },
                    errorMessage = (authState as? AuthState.Error)?.message,
                    isLoading = authState is AuthState.Loading
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PlexusColors.CyanPrimary)
                }
            }

            LaunchedEffect(authState) {
                if (authState is AuthState.Verified) {
                    navController.popBackStack()
                    authViewModel.resetState()
                }
            }
        }

        // Local Devices
        composable(Routes.LOCAL_DEVICES) {
            val localViewModel: LocalChatViewModel = viewModel()
            val devices by localViewModel.discoveredDevices.collectAsState()
            val connectionState by localViewModel.connectionState.collectAsState()

            LaunchedEffect(Unit) {
                val name = authViewModel.currentUser?.displayName
                    ?: authViewModel.currentUser?.phoneNumber
                    ?: "User"
                localViewModel.initialize(context, name)
            }
            LocalDevicesScreen(
                discoveredDevices = devices,
                connectionState = connectionState,
                onDeviceClick = { device ->
                    localViewModel.connectToDevice(device)
                    navController.navigate(
                        Routes.localChat(
                            device.name.removePrefix("PlexusChat-").ifEmpty { "Device" },
                            device.host
                        )
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NEW_CHAT) {
            val searchResults by chatViewModel.searchResults.collectAsState()
            val isSearching by chatViewModel.isSearching.collectAsState()
            val searchError by chatViewModel.searchError.collectAsState()

            NewChatScreen(
                searchResults = searchResults,
                isSearching = isSearching,
                errorMessage = searchError,
                onSearch = { query ->
                    chatViewModel.searchUserByUsername(query)
                },
                onUserClick = { user ->
                    chatViewModel.createChat(user.uid) { chatId ->
                        navController.navigate(Routes.chat(chatId, user.displayName.ifEmpty { user.username })) {
                            popUpTo(Routes.NEW_CHAT) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    chatViewModel.clearSearch()
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.LOCAL_CHAT,
            arguments = listOf(
                navArgument("deviceName") { type = NavType.StringType },
                navArgument("deviceHost") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "Device"
            val messages by localViewModel.messages.collectAsState()

            ChatScreen(
                contactName = deviceName,
                isOnline = true,
                isLocal = true,
                messages = messages.map { msg ->
                    MessageUiModel(
                        id = msg.id,
                        text = msg.text,
                        isMine = msg.isMine,
                        time = TimeUtils.formatMessageTime(msg.timestamp),
                        status = MessageStatus.SENT
                    )
                },
                onSendMessage = { text ->
                    localViewModel.sendMessage(text)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

}
