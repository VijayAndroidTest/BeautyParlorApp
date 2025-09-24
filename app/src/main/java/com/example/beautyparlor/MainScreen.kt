package com.example.beautyparlor


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.beautyparlor.entities.ServiceSubItem as ServiceSubItemEntity

@Composable
fun MainScreen(
    mainNavController: NavController,
    cartItems: List<ServiceSubItemEntity>,
    onRemoveFromCart: (ServiceSubItemEntity) -> Unit
) {
    val bottomNavController = rememberNavController()
    var isAdmin by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Check admin status on initial composition or when the user changes
    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser
        isAdmin = if (user != null) {
            try {
                val doc = db.collection("admins").document(user.uid).get().await()
                doc.exists()
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(bottomNavController) }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "homeTab",
            modifier = Modifier.padding(padding)
        ) {
            composable("homeTab") {
                HomeTab(mainNavController = mainNavController, isAdmin = isAdmin)
            }
            composable("bookingTab") { MyBookingsScreen(mainNavController) }
            composable("beautyTab") { BeautyTab(mainNavController) }
            composable("cartTab") {
                CartTab(
                    navController = mainNavController,
                    cartItems = cartItems,
                    onRemoveFromCart = onRemoveFromCart
                )
            }
            composable("bookings_tab") { MyBookingsScreen(mainNavController) }
        }
    }
}