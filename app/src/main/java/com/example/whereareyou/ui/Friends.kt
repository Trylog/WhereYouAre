package com.example.whereareyou.ui

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.whereareyou.ui.theme.AppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import kotlinx.coroutines.tasks.await

@Composable
fun CurrentLocation(onLocationChanged: (Location?) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var location by remember { mutableStateOf<Location?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    LaunchedEffect(Unit) {
        hasPermission = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    if (hasPermission) {
        LaunchedEffect(Unit) {
            location = getLastKnownLocation(fusedLocationClient)
            onLocationChanged(location)
        }
    } else {
        val activity = context as? Activity
        activity?.requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
        hasPermission = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

@SuppressLint("MissingPermission")
suspend fun getLastKnownLocation(client: FusedLocationProviderClient): Location? {
    return try {
        client.lastLocation.await()
    } catch (e: Exception) {
        null
    }
}

fun calculateDistance(start: Location, end: LatLng):Float{
    val newLoc = Location("new")
    newLoc.latitude = end.latitude
    newLoc.longitude = end.longitude
    return start.distanceTo(newLoc)
}

@Composable
fun FriendCard(name: String, distanceText: String) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavHostController) {

    var location by remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState {

    }
    CurrentLocation { n ->
        location = n
        n?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                10f
            )
        }
    }

    val openDialog = remember{ mutableStateOf(false) }
    val friendsCoords = mapOf(
        "Aaaaaa" to LatLng(51.10845, 17.05750), 
        "Bbbbb" to LatLng(51.09923, 17.03675), 
        "Cccccc" to LatLng(51.11873, 16.99022), 
        "Ddddddd" to LatLng(51.11065, 17.03358)
    )
    val friendsDistances = LinkedHashMap<String, Float>()
    friendsCoords.forEach{ (name, coords) ->
        friendsDistances[name] = location?.let{calculateDistance(it, coords)} ?: 0.0.toFloat()
    }
    val friendsSorted = friendsDistances.toList().sortedBy { it.second }.toMap()
    if (openDialog.value) {
        AddFriendDialog(openDialog = openDialog)
    }
    
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
                onClick = {
                    openDialog.value = true
                },
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            NavBar(navController = navController)
        }
    ){ innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ){
            friendsSorted.forEach { (name, distance) ->
                var dist = distance
                var distanceText = ""
                if (dist > 1000) {
                    dist /= 1000
                    distanceText = distanceText.plus(
                        String.format(Locale.forLanguageTag("pl"), "%.2f", dist)
                    ).plus(" km")
                } else {
                    distanceText = distanceText.plus(
                        String.format(Locale.forLanguageTag("pl"), "%.2f", dist)
                    ).plus(" m")
                }
                FriendCard(name, distanceText)
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
