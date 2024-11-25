package com.example.whereareyou

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.whereareyou.ui.theme.AppTheme
import com.example.whereareyou.ui.MainNavigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("628547724758-6tm8mavg3ju6orl4qhaharlcj24umj4u.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Launcher to handle sign-in result
        val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                firebaseAuthWithGoogle(account) { success ->
                    if (success) {
                        Log.d("SignIn", "Sign-in successful: ${firebaseAuth.currentUser?.displayName}")
                    } else {
                        Log.e("SignIn", "Sign-in failed")
                    }
                }
            } catch (e: Exception) {
                Log.e("SignIn", "Google Sign-In failed", e)
            }
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()

                // State to track current Firebase user
                var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
                val isLoggedIn = currentUser != null

                // Listen for auth state changes
                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener {
                        currentUser = it.currentUser
                    }
                    firebaseAuth.addAuthStateListener(authListener)
                    onDispose {
                        firebaseAuth.removeAuthStateListener(authListener)
                    }
                }

                // MainNavigation with login and logout actions
                MainNavigation(
                    navController = navController,
                    isLoggedIn = isLoggedIn,
                    onLoginClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        signInLauncher.launch(signInIntent)
                    },
                    onLogoutClick = {
                        firebaseAuth.signOut()
                        googleSignInClient.signOut()
                        navController.navigate("login") {
                            popUpTo("settings") { inclusive = true }
                        }
                    }
                )

                // Navigate after login only once
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        Log.d("Navigation", "Navigating to settings screen")
                        navController.navigate("settings") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?, onSuccess: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    onSuccess(true)  // Notify success
                } else {
                    onSuccess(false)  // Notify failure
                }
            }
    }
}
