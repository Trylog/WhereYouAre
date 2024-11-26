package com.example.whereareyou.ui

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.whereareyou.ui.theme.AppTheme
import com.google.android.gms.maps.model.LatLng


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen() {
    val friends = mapOf(
        "Aaaaaa" to LatLng(51.10845, 17.05750), 
        "Bbbbb" to LatLng(51.09923, 17.03675), 
        "Cccccc" to LatLng(51.11873, 16.99022), 
        "Ddddddd" to LatLng(51.11065, 17.03358)
    )
    val location = LatLng(51.10725, 17.06246)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text("Friends")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add");
            }
        },
        bottomBar = {
            NavBar(0)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ){ innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ){
            friends.forEach { (name, coords) ->
                val locationA: Location = Location("current")
                locationA.latitude = location.latitude
                locationA.longitude = location.longitude
                val locationB: Location = Location(name)
                locationB.latitude = coords.latitude
                locationB.longitude = coords.longitude
                var distance: Float = locationA.distanceTo(locationB)
                var distanceText = ""
                if (distance > 1000) {
                    distance /= 1000
                    distanceText = distanceText.plus(String.format("%.2f", distance)).plus(" km")
                } else {
                    distanceText = distanceText.plus(String.format("%.2f", distance)).plus(" m")
                }
                Card (
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ){
                    Row (
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ){
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box (modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = name[0].toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(text = name, style = MaterialTheme.typography.titleMedium)
                                Text(text = distanceText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Box (
                            Modifier.align(Alignment.CenterVertically)
                        ){
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsPreview() {
    AppTheme {
        FriendsScreen()
    }
}
