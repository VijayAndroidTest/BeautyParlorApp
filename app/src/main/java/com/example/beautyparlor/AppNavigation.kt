//package com.example.beautyparlor
//
//import MainScreen
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.beautyparlor.MainScreen // Assuming MainScreen is your bottom navigation setup
//import com.example.beautyparlor.BeautyParlorApp // Your destination for Women's Services
//
//@Composable
//fun AppNavigation() {
//    val mainNavController = rememberNavController()
//    NavHost(navController = mainNavController, startDestination = "mainScreen") {
//        composable("mainScreen") {
//            // This is where your bottom navigation bar and its tabs are hosted.
//            MainScreen(mainNavController)
//        }
//
//        // This is the missing route for the Women's Services page.
//        // It must be defined in the main NavHost, not the bottom navigation's NavHost.
//        composable("goldenTrends") {
//            BeautyParlorApp(mainNavController) // Pass the mainNavController to navigate back.
//        }
//
//        // Add other top-level routes from your drawer here
//        composable("myProfile") { /* MyProfileScreen() */ }
//        composable("myBookings") { /* MyBookingsScreen() */ }
//        composable("beautyPoints") { /* BeautyPointsScreen() */ }
//        composable("helpSupport") { /* HelpSupportScreen() */ }
//    }
//}