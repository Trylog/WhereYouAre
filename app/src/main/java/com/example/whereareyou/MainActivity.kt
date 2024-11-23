@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.whereareyou

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.strictmode.UntaggedSocketViolation
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.whereareyou.ui.theme.WhereAreYouTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var users = ArrayList<Pair<String, LatLng>>()
            users.add(Pair("test", LatLng(52.397850, 16.923709)))
            MapScreen(users)
        }
    }
}

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
        }
        if(location != null)onLocationChanged(location)
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


@Composable
fun MapScreen(users: ArrayList<Pair<String, LatLng>>) {
    //get location
    var location by remember { mutableStateOf<Location?>(null) }

    CurrentLocation { n -> location = n }

    val currentPosition = if(location != null)LatLng(location!!.latitude, location!!.longitude) else LatLng(-33.852, 151.211) // Lokalizacja w Sydney
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPosition, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ){
        Marker(
            state = rememberMarkerState(position = currentPosition),
            title = "Moja lokalizacja",
        )
        for (user in users) {
            Marker(
                state = rememberMarkerState(position = user.second),
                title = user.first
            )
        }
    }

}
