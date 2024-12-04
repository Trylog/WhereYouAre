package com.example.whereareyou.ui


import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.whereareyou.R
import com.google.firebase.firestore.DocumentSnapshot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendDialog(openDialog: MutableState<Boolean>, userData: DocumentSnapshot?, db: FirebaseFirestore) {
    if (openDialog.value) {
        var code = ""
        if (userData != null) {
//            code = userData.data?.get("FriendCode")?.toString()?.let { Text(it) }
            code = userData.data?.get("FriendCode").toString()
        }
        val clipboard = LocalClipboardManager.current
        val context = LocalContext.current
        var text by remember { mutableStateOf("") }
        Log.d("TEST", "Dialog: $userData")
        BasicAlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
        ) {
            Surface (
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ){
                Column (
                    modifier = Modifier.padding(bottom = 24.dp),
                ){
                    IconButton(
                        onClick = {
                            openDialog.value = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                    Column (
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ){
                        Icon(Icons.Default.Person, contentDescription = null)
                        Text(text = "Add a friend", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "Copy your code and send it to your friend" +
                                    " or input your friend’s code here",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Row (
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(code)
                            }
//                            label = {Text("XXXX")}
                        )
                        TextButton(
                            onClick = {
                                clipboard.setClip(ClipEntry(ClipData.newPlainText("code",code)))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_content_copy_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text("Copy to clipboard")
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Column (
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ){
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Your friend's code") }
                        )
                        TextButton(
                            onClick = {
                                db.collection("FriendCodes").document(text).get()
                                    .addOnSuccessListener { document ->
                                        val uid = document.data?.get("userID").toString()
                                        Log.d("AddingFriend", "UserID: $uid")
                                        if (userData != null) {
                                            val doc1 = db.collection("users").document(userData.id)
                                            Log.d("AddingFriend", "$doc1")
                                            val doc2 = doc1.collection("Friend").document(uid)
                                            Log.d("AddingFriend", "$doc2")
                                            doc2.set(HashMap<String, Any>()).addOnSuccessListener {
                                                Log.d("AddingFriend", "Success")
                                            }.addOnFailureListener { e ->
                                                Log.d("AddingFriend", "Error: $e")
                                            }
                                        }
                                    }
                            },
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                                )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text("Confirm code")
                        }
                    }
                }
            }
        }
    }
}