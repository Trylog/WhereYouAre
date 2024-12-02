package com.example.whereareyou.ui


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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.whereareyou.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendDialog(openDialog: MutableState<Boolean>) {
    if (openDialog.value) {
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
                            label = { Text("XXXX") }
                        )
                        TextButton(
                            onClick = {}
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
                            value = "",
                            onValueChange = {},
                            label = {Text("Your friend's code")}
                        )
                        TextButton(
                            onClick = {},
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