package com.example.beautyparlor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Booking,
        BottomNavItem.Beauty,
        BottomNavItem.MyCart
    )

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Responsive sizing based on screen dimensions
    val bottomBarHeight = when {
        screenHeight < 600.dp -> 70.dp  // Small screens (older phones)
        screenHeight < 800.dp -> 60.dp  // Medium screens (most phones)
        else -> 60.dp                  // Large screens (tablets, newer phones)
    }

    val fontSize = when {
        screenWidth < 360.dp -> 9.sp    // Very small screens
        screenWidth < 400.dp -> 10.sp   // Small screens
        else -> 11.sp                   // Normal and large screens
    }

    val iconSize = when {
        screenWidth < 360.dp -> 20.dp   // Very small screens
        screenWidth < 400.dp -> 22.dp   // Small screens
        else -> 24.dp                   // Normal and large screens
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars), // Handle system navigation bar
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBarHeight)
                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFFF648BD)
                        )
                    )
                )
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                items.forEach { item ->
                    val isSelected = currentDestination?.route?.startsWith(item.route) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(iconSize),
                                tint = if (isSelected) Color.White else Color(0xFF00E5FF)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) Color.White else Color(0xFF00E5FF),
                                fontSize = fontSize,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}