package com.example.beautyparlor

import BeautyParlorApp
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beautyparlor.ui.theme.BeautyParlorTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.example.beautyparlor.entities.ServiceSubItem as ServiceSubItemEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // Optional: Remove signOut for auto-login
        // FirebaseAuth.getInstance().signOut()

        setContent {
            BeautyParlorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val mainNavController = rememberNavController()
                    var cartItems by remember { mutableStateOf(listOf<ServiceSubItemEntity>()) }

                    val onAddToCart: (ServiceSubItemEntity) -> Unit = { item ->
                        cartItems = cartItems + item
                    }
                    val onRemoveFromCart: (ServiceSubItemEntity) -> Unit = { item ->
                        cartItems = cartItems.filter { it != item }
                    }

                    NavHost(navController = mainNavController, startDestination = "authChecker") {

                        // ---------- AuthChecker ----------
                        composable("authChecker") {
                            AuthChecker(navController = mainNavController)
                        }

                        // ---------- Auth Screens ----------
                        composable("loginScreen") { LoginScreen(navController = mainNavController) }
                        composable("signupScreen") { SignupScreen(navController = mainNavController) }

                        // ---------- Main App Screens ----------
                        composable("mainScreen") {
                            MainScreen(
                                mainNavController = mainNavController,
                                cartItems = cartItems,
                                onRemoveFromCart = onRemoveFromCart
                            )
                        }

                        composable(
                            route = "beautyParlor/{tab}",
                            arguments = listOf(navArgument("tab") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val tab = backStackEntry.arguments?.getString("tab") ?: "women"
                            BeautyParlorApp(
                                navController = mainNavController,
                                initialTab = if (tab == "women") 0 else 1,
                                onAddToCartClick = onAddToCart
                            )
                        }

                        // ---------- Booking Screens ----------
                        composable(
                            route = "bookingScreen/{parentServiceName}/{subItemName}/{servicePrice}",
                            arguments = listOf(
                                navArgument("parentServiceName") { type = NavType.StringType },
                                navArgument("subItemName") { type = NavType.StringType },
                                navArgument("servicePrice") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val parentServiceName = Uri.decode(backStackEntry.arguments?.getString("parentServiceName") ?: "")
                            val subItemName = Uri.decode(backStackEntry.arguments?.getString("subItemName") ?: "")
                            val servicePrice = backStackEntry.arguments?.getString("servicePrice") ?: "0"

                            val selectedServiceSubItem = ServiceSubItemEntity(
                                subItemName = subItemName,
                                priceRange = servicePrice,
                                serviceName = parentServiceName
                            )

                            BookingScreen(
                                parentServiceName = parentServiceName, // Now passing the parent service name
                                service = selectedServiceSubItem,
                                onConfirm = { date, time ->
                                    val encodedParentServiceName = Uri.encode(parentServiceName)
                                    val encodedSubItemName = Uri.encode(subItemName)
                                    val encodedDate = Uri.encode(date)
                                    val encodedTime = Uri.encode(time)
                                    mainNavController.navigate(
                                        "bookingSummary/$encodedParentServiceName/$encodedSubItemName/$servicePrice/$encodedDate/$encodedTime"
                                    )
                                },
                                navController = mainNavController
                            )
                        }

                        composable(
                            route = "bookingSummary/{parentServiceName}/{subItemName}/{price}/{date}/{time}",
                            arguments = listOf(
                                navArgument("parentServiceName") { type = NavType.StringType },
                                navArgument("subItemName") { type = NavType.StringType },
                                navArgument("price") { type = NavType.StringType },
                                navArgument("date") { type = NavType.StringType },
                                navArgument("time") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val parentServiceName = Uri.decode(backStackEntry.arguments?.getString("parentServiceName") ?: "")
                            val subItemName = Uri.decode(backStackEntry.arguments?.getString("subItemName") ?: "")
                            val price = backStackEntry.arguments?.getString("price") ?: "0"
                            val date = Uri.decode(backStackEntry.arguments?.getString("date") ?: "")
                            val time = Uri.decode(backStackEntry.arguments?.getString("time") ?: "")

                            val selectedService = ServiceSubItemEntity(
                                subItemName = subItemName,
                                priceRange = price,
                                serviceName = parentServiceName // Now storing this value
                            )

                            BookingSummaryScreen(
                                navController = mainNavController,
                                selectedService = selectedService,
                                serviceName = parentServiceName, // This fixes the error
                                selectedDate = date,
                                selectedTime = time
                            )
                        }

                        composable("multiBookingSummary") {
                            MultiBookingSummaryScreen(
                                navController = mainNavController,
                                cartItems = cartItems,
                                onRemoveFromCart = onRemoveFromCart
                            )
                        }

                        // ---------- Profile & Other Screens ----------
                        composable("myProfile") { MyProfileScreen(mainNavController) }
                        composable("myBookings") { MyBookingsScreen(mainNavController) }
                        composable("beautyPoints") { BeautyTab(mainNavController) }
                        composable("helpSupport") { HelpSupportScreen(mainNavController) }
                        composable("adminDashboard") { AdminDashboardScreen(navController = mainNavController) }

                        composable(
                            route = "bookingConfirmed/{service}/{date}/{time}",
                            arguments = listOf(
                                navArgument("service") { type = NavType.StringType },
                                navArgument("date") { type = NavType.StringType },
                                navArgument("time") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val service = Uri.decode(backStackEntry.arguments?.getString("service") ?: "")
                            val date = Uri.decode(backStackEntry.arguments?.getString("date") ?: "")
                            val time = Uri.decode(backStackEntry.arguments?.getString("time") ?: "")
                            BookingConfirmedScreen(service, date, time, mainNavController)
                        }
                    }
                }
            }
        }
    }
}