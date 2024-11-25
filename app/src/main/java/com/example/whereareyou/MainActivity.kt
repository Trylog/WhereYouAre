package com.example.whereareyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.whereareyou.ui.FriendsScreen
import com.example.whereareyou.ui.LoginScreen
import com.example.whereareyou.ui.MapScreen
import com.example.whereareyou.ui.SettingsScreen
import com.example.whereareyou.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SettingsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarPreview() {
    AppTheme {
        LoginScreen()
    }
}