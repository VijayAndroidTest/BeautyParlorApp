//package com.example.beautyparlor
//
//import HomeTab
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//
//@Composable
//fun MainScreenWithTabs(mainNavController: NavController, startTab: String) {
//    val bottomNavController = rememberNavController()
//
//    Scaffold(
//        bottomBar = { BottomNavigationBar(bottomNavController) }
//    ) { padding ->
//        NavHost(
//            navController = bottomNavController,
//            startDestination = startTab,
//            modifier = Modifier.padding(padding)
//        ) {
//            // These are the bottom tabs, they are part of a nested graph
//            composable("homeTab") { HomeTab(mainNavController) } // Pass the top-level controller
//            composable("servicesTab") { Text("Services Tab Screen") }
//            composable("bookingsTab") { Text("Bookings Tab Screen") }
//        }
//    }
//}