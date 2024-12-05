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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


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

                val db = Firebase.firestore
                val userData = remember { mutableStateOf<DocumentSnapshot?>(null) }

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
                        },
                        uid = firebaseAuth.currentUser?.uid ?: "",
                        db = db,
                        userData = userData.value,
                    )


                // Navigate after login only once
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        val docRef = db.collection("users").document(firebaseAuth.currentUser?.uid ?: "")
                        docRef.get().addOnSuccessListener { document ->
                            if (document != null) {
                                Log.d("Database", "DocumentSnapshot data: ${document.data}")
                                userData.value = document
                                Log.d("Database", "Data: $userData")
                            } else {
                                Log.d("Database", "No such document")
                            }
                        }.addOnFailureListener { exception ->
                            Log.d("Database", "get failed with", exception)
                        }
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
                    val user = firebaseAuth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    val username = user.displayName ?: "Unknown"

                    val db = Firebase.firestore
                    val userRef = db.collection("users").document(uid)

                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            onSuccess(true)
                        } else {
                            generateUniqueFriendCode(db, { friendCode ->
                                val userData = hashMapOf(
                                    "Username" to username,
                                    "FriendCode" to friendCode,
                                    "LastLocation" to null
                                )

                                userRef.set(userData)
                                    .addOnSuccessListener {
                                        val friendCodeRef = db.collection("FriendCodes").document(friendCode)
                                        val friendCodeData = hashMapOf(
                                            "userID" to uid
                                        )
                                        friendCodeRef.set(friendCodeData)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "Friend code saved successfully")
                                                onSuccess(true)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error saving friend code", e)
                                                onSuccess(false)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error saving user data", e)
                                        onSuccess(false)
                                    }
                            }, {
                                Log.e("Firestore", "Error generating unique code")
                                onSuccess(false)
                            })
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Error checking user existence", e)
                        onSuccess(false)
                    }
                } else {
                    onSuccess(false)
                }
            }
    }

    fun generateUniqueFriendCode(db: FirebaseFirestore, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        var newFriendCode: String
        do {
            newFriendCode = generateRandomCode()
        } while (isFriendCodeExists(db, newFriendCode))

        onSuccess(newFriendCode)
    }

    fun isFriendCodeExists(db: FirebaseFirestore, friendCode: String): Boolean {
        var exists = false
        db.collection("FriendCodes").document(friendCode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    exists = true
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error checking friend code existence", it)
            }
        return exists
    }

    fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
