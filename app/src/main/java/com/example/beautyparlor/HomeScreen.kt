//package com.example.beautyparlor
//
//import android.util.Log
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.google.firebase.firestore.FirebaseFirestore
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//
//
//@Composable
//fun HomeScreen(
//    services: List<Service>,
//    onBookService: (Service) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier.fillMaxSize().padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(services) { service ->
//            ServiceCard(service = service, onBookService = onBookService)
//        }
//    }
//}
//
//@Composable
//fun ServiceCard(service: Service, onBookService: (Service) -> Unit) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8BBD0)), // The pink color for the card
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = service.name,
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Price: ₹${service.price}",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Black
//            )
//            Text(
//                text = "Duration: ${service.duration} mins",
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Black
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Button(
//                onClick = { onBookService(service) },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A))
//            ) {
//                Text("Book Now")
//            }
//        }
//    }
//}
