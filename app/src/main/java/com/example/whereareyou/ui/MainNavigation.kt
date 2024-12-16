package com.example.whereareyou.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
    userData: DocumentSnapshot?,
    shareLocation: MutableState<Boolean>
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "settings" else "login",
    ) {
        composable("login") {
            LoginScreen(onLoginClick = onLoginClick)
        }
        composable("settings") {
            SettingsScreen( navController = navController, onLogoutClick = onLogoutClick, shareLocation = shareLocation, /*userData = userData*/)
        }
        composable("map") {
            MapScreen(navController = navController, uid = uid, db = db)
        }
        composable("friends") {
            FriendsScreen(navController = navController, userData = userData, uid = uid, db = db, shareLocation = shareLocation)
        }
    }
}
