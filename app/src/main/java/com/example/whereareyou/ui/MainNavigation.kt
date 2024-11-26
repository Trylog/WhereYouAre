package com.example.whereareyou.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng

@Composable
fun MainNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = if (isLoggedIn) "settings" else "login") {
        composable("login") { LoginScreen(onLoginClick = onLoginClick) }
        composable("settings") {
            SettingsScreen(
                onLogoutClick = onLogoutClick
            )
        }
        composable("map"){
            var users_test = ArrayList<Pair<String, LatLng>>()
            users_test.add(Pair("test", LatLng(52.397850, 16.923709)))

            MapScreen(users_test)
        }
    }
}
