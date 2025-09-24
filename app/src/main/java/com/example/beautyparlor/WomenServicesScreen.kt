package com.example.beautyparlor

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WomenServicesScreen(navController: NavController) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            ServiceCard("Bridal Package", "10000") {
                navController.navigate("booking/Bridal%20Package/10000")
            }
        }
        item {
            ServiceCard("Facial", "1500") {
                navController.navigate("booking/Facial/1500")
            }
        }
        item {
            ServiceCard("Hair Spa", "1200") {
                navController.navigate("booking/Hair%20Spa/1200")
            }
        }
    }
}