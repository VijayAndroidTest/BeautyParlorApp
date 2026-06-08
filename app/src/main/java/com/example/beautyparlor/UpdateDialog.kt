package com.example.beautyparlor

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun UpdateDialog(message: String, updateUrl: String) { // Added updateUrl parameter
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { /* Strict: User cannot close this */ },
        title = { Text(text = "App Update Required") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = {
                // Uses the dynamic URL passed from Firebase
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(updateUrl)
                )
                context.startActivity(intent)
            }) {
                Text("Update Now")
            }
        }
    )
}