package com.example.whereareyou.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import com.example.whereareyou.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) { innerPadding -> // Przekazanie innerPadding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Użycie innerPadding tutaj
                .padding(16.dp), // Opcjonalne dodatkowe wcięcie wewnętrzne

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(39.dp, Alignment.CenterVertically)
        ) {
            // Tytuł
            Text(
                text = "WhereYouAre",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Obrazek użytkownika
            Box(
                modifier = Modifier
                    .size(120.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            // Przycisk logowania
            Button(
                onClick = { /* TODO: Akcja logowania */ },
            ) {
                Text(
                    text = "Login with Google",
                )
            }
        }
    }
}
