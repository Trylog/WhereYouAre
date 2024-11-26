package com.example.whereareyou.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "settings" else "login"
    ) {
        composable("login") {
            LoginScreen(onLoginClick = onLoginClick)
        }
        composable("settings") {
            SettingsScreen(onLogoutClick = onLogoutClick, navController = navController)
        }
        composable("map") {
            MapScreen(navController = navController)
        }
        composable("friends") {
            FriendsScreen(navController = navController)
        }
    }
}