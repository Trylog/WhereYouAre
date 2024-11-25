package com.example.whereareyou.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun NavBar() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Friends", "Map", "Settings")
    val selectedIcons = listOf(Icons.Filled.Person, Icons.Filled.LocationOn, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.Person, Icons.Outlined.LocationOn, Icons.Outlined.Settings)
    NavigationBar(){
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {selectedItem = index}
            )
        }
    }
}
