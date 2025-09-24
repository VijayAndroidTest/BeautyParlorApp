package com.example.beautyparlor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.example.beautyparlor.R // Make sure this R is correct for your project

// Create a new composable for the splash screen
//@Composable
//fun SplashScreen(navController: NavHostController) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        // Replace 'R.drawable.ic_splash_logo' with your actual logo resource
//        Image(
//            painter = painterResource(id = R.drawable.golden_logo),
//            contentDescription = "Splash Logo"
//        )
//    }
//
//    LaunchedEffect(key1 = true) {
//        delay(2000) // Delay for 2 seconds
//        navController.popBackStack() // Remove the splash screen from the back stack
//        navController.navigate("mainScreen") // Navigate to the main screen
//    }
//}