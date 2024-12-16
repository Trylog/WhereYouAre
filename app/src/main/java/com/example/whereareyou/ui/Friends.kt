package com.example.whereareyou.ui

import android.location.Location
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale


@Composable
fun FriendCard(name: String, distanceText: String, db: FirebaseFirestore, uid: String,
               location: Location,
               isRefreshing: MutableState<Boolean>,
               friendsSorted: MutableState<Map<String, Float>?>,
               shareLocation: MutableState<Boolean>) {
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
                IconButton(
                    onClick = { deleteFriend(name, db, uid, location, isRefreshing, friendsSorted, shareLocation) }
                ) {
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

fun deleteFriend(name: String, db: FirebaseFirestore, uid: String, location: Location,
                 isRefreshing: MutableState<Boolean>,
                 friendsSorted: MutableState<Map<String, Float>?>,
                 shareLocation: MutableState<Boolean>) {
    db.collection("users").whereEqualTo("Username", name).get()
        .addOnSuccessListener { document ->
            val id = document.first().id
            val doc = db.collection("users").document(uid)
            val doc1 = doc.collection("Friend").document(id)
            doc1.delete().addOnSuccessListener {
                Log.d("DELETING", "User $id deleted.")
                refresh(db, uid, location, isRefreshing, friendsSorted, shareLocation)
            }.addOnFailureListener { e ->
                Log.d("DELETING", "User $id deleting error.", e)
            }
        }.addOnFailureListener { e ->
            Log.d("DELETING", "Failed to find name $name.", e)
        }
}

fun refresh(
    db: FirebaseFirestore,
    uid: String,
    location: Location,
    isRefreshing: MutableState<Boolean>,
    friendsSorted: MutableState<Map<String, Float>?>,
    shareLocation: MutableState<Boolean>
) {
    Log.d("bb", "bbbb")
    isRefreshing.value = true
    if (shareLocation.value) {
        sendLocation(db, uid, location)
    }

    db.collection("users").document(uid).collection("Friend").get()
        .addOnSuccessListener { collection ->
            if (collection.isEmpty) {
                Log.d("FriendCollection", "No friends collection, creating an empty one.")

                db.collection("users").document(uid).collection("Friend").document("temporary").set(mapOf("placeholder" to "empty"))
                    .addOnSuccessListener {
                        Log.d("FriendCollection", "Temporary document created.")

                        db.collection("users").document(uid).collection("Friend").document("temporary").delete()
                            .addOnSuccessListener {
                                Log.d("FriendCollection", "Temporary document deleted, collection remains empty.")
                            }
                            .addOnFailureListener { e ->
                                Log.d("FriendCollection", "Error deleting temporary document: $e")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.d("FriendCollection", "Error creating temporary document: $e")
                    }
            } else {
                val friends = collection.documents.map { it.id }
                Log.d("GetFriends", "Got friends: $friends")
                //stop
                friends.let { e ->
                    db.collection("users").whereIn(FieldPath.documentId(), e).get()
                        .addOnSuccessListener { snapshot ->
                            Log.d("GettingFriendsData", "Success1")
                            val friendsCoords2 = snapshot.documents.mapNotNull { document ->
                                val name = document.getString("Username")
                                val loc = document.getGeoPoint("LastLocation")
                                if (name != null && loc != null) name to loc else null
                            }.toMap()
                            Log.d("GettingFriendsData", "Success2")
                            val friendsDistances = LinkedHashMap<String, Float>()
                            friendsCoords2.forEach { (name, coords) ->
                                friendsDistances[name] = calculateDistance(location, coords)
                            }
                            friendsCoords2.forEach { (name, coords) ->
                                friendsDistances[name] = calculateDistance(location, coords)
                            }
                            friendsSorted.value = friendsDistances.toList().sortedBy { it.second }.toMap()
                            isRefreshing.value = false
                        }
                        .addOnFailureListener { err ->
                            Log.d("GettingFriendsData", "Error occurred: $err")
                            isRefreshing.value = false
                        }
                }
            }
        }
        .addOnFailureListener { e ->
            Log.d("GetFriends", "Error getting friends: $e")
            isRefreshing.value = false
        }
}

fun sendLocation(db: FirebaseFirestore, uid: String, location: Location){
    Log.d("SendingLocation", "Start sending")
    val ref = db.collection("users").document(uid)
    ref.update("LastLocation", GeoPoint(location.latitude, location.longitude)).addOnSuccessListener {
        Log.d("SendingLocation", "Sent succesfully")
    }.addOnFailureListener { e ->
        Log.d("SendingLocation", "Error sending location: $e")
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavHostController, userData: DocumentSnapshot?, db: FirebaseFirestore,
                  uid: String, shareLocation: MutableState<Boolean>) {
    Log.d("TEST", "Friends: $userData")
    var location by remember { mutableStateOf<Location?>(null) }
    val isRefreshing = remember { mutableStateOf(false) }
    val friendsSorted = remember { mutableStateOf<Map<String, Float>?>(null) }
    val cameraPositionState = rememberCameraPositionState {}

    CurrentLocation { n ->
        location = n
        n?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                10f
            )
        }
    }

    val openDialog = remember { mutableStateOf(false) }
    if (openDialog.value) {
        AddFriendDialog(
            openDialog = openDialog,
            userData = userData,
            db = db,
            onSuccess = {
                location?.let { refresh(db, uid, it, isRefreshing, friendsSorted, shareLocation) }
            }
        )
    }

    LaunchedEffect(location) {
        location?.let {
            refresh(db, uid, location!!, isRefreshing, friendsSorted, shareLocation)
        }
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
        PullToRefreshBox(
            onRefresh = {
                location?.let { refresh(db, uid, location!!, isRefreshing, friendsSorted, shareLocation) }
            },
            isRefreshing = isRefreshing.value
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) { if (friendsSorted.value.isNullOrEmpty()) {
                Card{
                    Text("There's no one here")
                }
            }
                friendsSorted.value?.forEach { (name, distance) ->
                    var dist = distance
                    var distanceText = ""
                    if (dist > 1000) {
                        dist /= 1000
                        distanceText = distanceText.plus(
                            String.format(Locale.forLanguageTag("en"), "%.2f", dist)
                        ).plus(" km")
                    } else {
                        distanceText = distanceText.plus(
                            String.format(Locale.forLanguageTag("en"), "%.2f", dist)
                        ).plus(" m")
                    }
                    location?.let {
                        FriendCard(name, distanceText, db, uid,
                            it, isRefreshing, friendsSorted, shareLocation)
                    }
                }

            }
        }
    }
}
