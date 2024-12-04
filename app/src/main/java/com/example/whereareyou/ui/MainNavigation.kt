package com.example.whereareyou.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    uid: String,
    db: FirebaseFirestore,
    userData: DocumentSnapshot?
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "settings" else "login",
    ) {
        composable("login") {
            LoginScreen(onLoginClick = onLoginClick)
        }
        composable("settings") {
            SettingsScreen( navController = navController, onLogoutClick = onLogoutClick, /*userData = userData*/)
        }
        composable("map") {
            MapScreen(navController = navController, /*userData = userData*/)
        }
        composable("friends") {
            FriendsScreen(navController = navController, userData = userData, uid = uid, db = db)
        }
    }
}
