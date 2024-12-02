package com.example.whereareyou.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
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
    return start.distanceTo(newLoc) / 1000
}

@Composable
fun MapComponent(users: ArrayList<Pair<String, LatLng>>) {
    //get location
    var location by remember { mutableStateOf<Location?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-33.852, 151.211), 10f)
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

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        val currentPosition = location?.let { LatLng(it.latitude, it.longitude) }
        if (currentPosition != null) {
            Marker(
                state = rememberMarkerState(position = currentPosition),
                title = "Moja lokalizacja",
            )
        }

        for (user in users) {
            Marker(
                state = rememberMarkerState(position = user.second),
                title = user.first,
                snippet = location?.let {
                    "Odległość: " + String.format("%.2f", calculateDistance(it, user.second)) + "km"
                } ?: "Lokalizacja niedostępna"
            )
            if (currentPosition != null) {
                Polyline(
                    points = listOf(currentPosition, user.second),
                    color = androidx.compose.ui.graphics.Color.Blue,
                    width = 5f
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController, users: ArrayList<Pair<String, LatLng>>) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text("Map")
                }
            )
        },
        bottomBar = {
            NavBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Card {
                MapComponent(users)
            }
        }
    }
}

