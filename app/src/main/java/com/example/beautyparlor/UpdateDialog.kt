package com.example.beautyparlor

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun UpdateDialog(message: String) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { /* Do nothing - Strict Rule */ },
        title = { Text("App Update Required") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.example.beautyparlor")
                )
                context.startActivity(intent)
            }) {
                Text("Update Now")
            }
        }
    )
}