package com.example.beautyparlor



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beautyparlor.entities.ServiceSubItem as ServiceSubItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiBookingSummaryScreen(
    navController: NavController,
    cartItems: List<ServiceSubItemEntity>,
    onRemoveFromCart: (ServiceSubItemEntity) -> Unit
) {
    val totalAmount = remember(cartItems) {
        cartItems.sumOf { item ->
            val priceText = item.priceRange.replace("₹", "").replace("Rs.", "").replace("Rs", "").trim()
            when {
                priceText.contains("-") -> {
                    val parts = priceText.split("-").map {
                        it.trim().replace(",", "").toIntOrNull() ?: 0
                    }
                    if (parts.size == 2 && parts[1] > 0) (parts[0] + parts[1]) / 2 else parts.firstOrNull() ?: 0
                }
                priceText.contains("to") -> {
                    val parts = priceText.split("to").map {
                        it.trim().replace(",", "").toIntOrNull() ?: 0
                    }
                    if (parts.size == 2 && parts[1] > 0) (parts[0] + parts[1]) / 2 else parts.firstOrNull() ?: 0
                }
                else -> priceText.replace(",", "").toIntOrNull() ?: 0
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Booking Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(cartItems) { item ->
                    ServiceItem(item = item)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 12.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "Rs. $totalAmount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            navController.navigate("bookingConfirmed/Multiple Services/${System.currentTimeMillis()}/Now")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Confirm Booking",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(item: ServiceSubItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = item.subItemName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )
            if (item.serviceName.isNotEmpty()) {
                Text(
                    text = item.serviceName,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
        }
        Text(
            text = "Rs. ${item.priceRange}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )
    }
    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
}
