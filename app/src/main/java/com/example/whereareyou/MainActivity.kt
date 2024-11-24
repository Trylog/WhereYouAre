package com.example.whereareyou

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.whereareyou.ui.theme.WhereAreYouTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
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
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Log.e("SignIn", "Google Sign-In failed", e)
            }
        }

        setContent {
            var user by remember { mutableStateOf(firebaseAuth.currentUser) }

            WhereAreYouTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (user == null) {
                        // Sign-In Screen
                        SignInScreen(
                            modifier = Modifier.padding(innerPadding),
                            onSignInClick = {
                                val signInIntent = googleSignInClient.signInIntent
                                signInLauncher.launch(signInIntent)
                            }
                        )
                    } else {
                        // Logged-In Screen
                        LoggedInScreen(
                            modifier = Modifier.padding(innerPadding),
                            userName = user?.displayName ?: "No Name",
                            userEmail = user?.email ?: "No Email",
                            onSignOutClick = {
                                firebaseAuth.signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    user = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("SignIn", "Sign-in successful: ${firebaseAuth.currentUser?.displayName}")
                } else {
                    Log.e("SignIn", "Sign-in failed", task.exception)
                }
            }
    }
}

@Composable
fun SignInScreen(modifier: Modifier = Modifier, onSignInClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to WhereAreYou!")
        Button(onClick = onSignInClick) {
            Text(text = "Sign in with Google")
        }
    }
}

@Composable
fun LoggedInScreen(
    modifier: Modifier = Modifier,
    userName: String,
    userEmail: String,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, $userName!")
        Text(text = "Email: $userEmail")
        Button(onClick = onSignOutClick) {
            Text(text = "Sign out")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    WhereAreYouTheme {
        SignInScreen(onSignInClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoggedInScreenPreview() {
    WhereAreYouTheme {
        LoggedInScreen(
            userName = "John Doe",
            userEmail = "john.doe@example.com",
            onSignOutClick = {}
        )
    }
}
