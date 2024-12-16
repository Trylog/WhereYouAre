package com.example.whereareyou.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import android.util.Log
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.tasks.await
import java.util.Locale

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

fun calculateDistance(start: Location, end: GeoPoint):Float{
    val newLoc = Location("new")
    newLoc.latitude = end.latitude
    newLoc.longitude = end.longitude
    return start.distanceTo(newLoc)
}

@Composable
fun MapComponent(friendsCoords: Map<String, GeoPoint>?) {
    // Przechowywanie lokalizacji użytkownika
    var location by remember { mutableStateOf<Location?>(null) }

    // Konwertuj dane z friendsCoords na listę użytkowników
    val users = friendsCoords?.map { entry ->
        entry.key to LatLng(entry.value.latitude, entry.value.longitude)
    } ?: emptyList()

    // Kamera mapy
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.107885, 17.0385367), 10f) // Domyślna pozycja (Wrocław)
    }

    // Pobierz bieżącą lokalizację użytkownika
    CurrentLocation { n ->
        location = n
        n?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                10f
            )
        }
    }

    // Renderowanie mapy
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Marker dla bieżącej lokalizacji użytkownika
        val currentPosition = location?.let { LatLng(it.latitude, it.longitude) }
        if (currentPosition != null) {
            Marker(
                state = rememberMarkerState(position = currentPosition),
                title = "My location"
            )
        }

        // Markery dla znajomych
        for ((name, latLng) in users) {
            Marker(
                state = rememberMarkerState(position = latLng),
                title = name,
                snippet = location?.let {
                    Log.d("lating",latLng.toString())
                    val distance = calculateDistance(it, GeoPoint(latLng.latitude, latLng.longitude))
                    Log.d("lating", distance.toString())
                    if (distance >= 1000) {
                        "Distance: ${String.format(Locale.forLanguageTag("en"), "%.2f", distance / 1000)} km"
                    } else {
                        "Distance: ${String.format(Locale.forLanguageTag("en"), "%.2f", distance)} m"
                    }
                } ?: "Location unavailable"
            )

            // Dodanie linii między użytkownikiem a znajomym
            if (currentPosition != null) {
                Polyline(
                    points = listOf(currentPosition, latLng),
                    color = androidx.compose.ui.graphics.Color.Blue,
                    width = 5f
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController, db: FirebaseFirestore, uid: String) {
    val friendsCoords2 = remember { mutableStateOf<Map<String, GeoPoint>?>(null) }

    // Pobieranie danych znajomych
    LaunchedEffect(Unit) {
        db.collection("users").document(uid).collection("Friend").get()
            .addOnSuccessListener { collection ->
                run {
                    if (!collection.isEmpty) {
                        val friendIds = collection.documents.map { it.id }
                        db.collection("users").whereIn(FieldPath.documentId(), friendIds).get()
                            .addOnSuccessListener { snapshot ->
                                friendsCoords2.value = snapshot.documents.mapNotNull { document ->
                                    val name = document.getString("Username")
                                    val loc = document.getGeoPoint("LastLocation")
                                    if (name != null && loc != null) name to loc else null
                                }.toMap()
                            }
                    }
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = { Text("Map") }
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
                MapComponent(friendsCoords = friendsCoords2.value)
            }
        }
    }
}


