package com.example.beautyparlor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthChecker(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var checked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check if user is already logged in
        if (auth.currentUser == null) {
            // Not logged in → navigate to LoginScreen
            navController.navigate("loginScreen") {
                popUpTo("authChecker") { inclusive = true }
            }
        } else {
            // Already logged in → navigate to MainScreen
            navController.navigate("mainScreen") {
                popUpTo("authChecker") { inclusive = true }
            }
        }
        checked = true
    }

    if (!checked) {
        // Show a loading indicator while checking
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF9C27B0))
        }
    }
}