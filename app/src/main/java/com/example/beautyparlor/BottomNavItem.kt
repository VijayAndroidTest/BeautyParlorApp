package com.example.beautyparlor

sealed class BottomNavItem(val route: String, val label: String, val icon: Int) {
    object Home : BottomNavItem("homeTab", "Home", R.drawable.homee)
    object Booking : BottomNavItem("bookingTab", "Booking", R.drawable.booking)
    object Beauty : BottomNavItem("beautyTab", "Beauty", R.drawable.beautygirlll)
    object MyCart : BottomNavItem("cartTab", "My Cart", R.drawable.cart)
}