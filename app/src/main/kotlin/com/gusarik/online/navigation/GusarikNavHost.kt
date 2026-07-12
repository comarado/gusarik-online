package com.gusarik.online.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gusarik.core.domain.model.UserStats
import com.gusarik.feature.auth.ui.LoginScreen
import com.gusarik.feature.auth.ui.RegisterScreen
import com.gusarik.feature.game.ui.GameScreen
import com.gusarik.feature.history.ui.HistoryScreen
import com.gusarik.feature.lobby.ui.LobbyScreen
import com.gusarik.feature.menu.ui.MainMenuScreen
import com.gusarik.feature.settings.ui.SettingsScreen
import com.gusarik.feature.stats.ui.StatsScreen

/**
 * Navigation routes.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MENU = "menu"
    const val LOBBY = "lobby"
    const val GAME = "game/{roomCode}"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun game(roomCode: String) = "game/$roomCode"
}

@Composable
fun GusarikNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoogleSignIn = {
                    // Google Sign-In is handled by the activity
                    // The token is passed to the ViewModel
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                authRepository = // Would be injected via Hilt
                    throw NotImplementedError("Inject via Hilt in production")
            )
        }

        // Menu
        composable(Routes.MENU) {
            MainMenuScreen(
                onNavigateToPlay = { navController.navigate(Routes.LOBBY) },
                onNavigateToInvite = { navController.navigate(Routes.LOBBY) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        // Lobby
        composable(Routes.LOBBY) {
            LobbyScreen(
                onNavigateToGame = { roomCode ->
                    navController.navigate(Routes.game(roomCode)) {
                        popUpTo(Routes.LOBBY) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Game
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            GameScreen(
                roomCode = roomCode,
                onBack = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        // History
        composable(Routes.HISTORY) {
            HistoryScreen(
                matches = emptyList(), // Would be loaded from ViewModel
                onBack = { navController.popBackStack() },
                onMatchClick = { /* Navigate to match detail */ }
            )
        }

        // Stats
        composable(Routes.STATS) {
            StatsScreen(
                stats = UserStats(), // Would be loaded from ViewModel
                onBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
