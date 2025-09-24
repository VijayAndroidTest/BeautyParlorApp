package com.example.beautyparlor

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<UserPoints>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    val fetchUsers: suspend () -> Unit = {
        isLoading = true
        try {
            val querySnapshot = db.collection("users").get().await()
            users = querySnapshot.documents.mapNotNull { doc ->
                UserPoints(
                    userId = doc.id,
                    userName = doc.getString("name") ?: "N/A",
                    points = doc.getLong("points") ?: 0L,
                    mobileNumber = doc.getString("mobileNumber") ?: "N/A",
                    email = doc.getString("email") ?: "N/A"
                )
            }
        } catch (e: Exception) {
            Log.e("AdminDashboardScreen", "Error fetching users", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    val onReducePoints: (UserPoints, Long) -> Unit = { user, pointsToReduce ->
        val newPoints = user.points - pointsToReduce
        db.collection("users").document(user.userId)
            .update("points", newPoints)
            .addOnSuccessListener {
                Log.d("AdminDashboardScreen", "Points for user ${user.userName} reduced to $newPoints")
                users = users.map {
                    if (it.userId == user.userId) it.copy(points = newPoints) else it
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminDashboardScreen", "Error reducing points", e)
            }
    }

    val onAddPoints: (UserPoints, Long) -> Unit = { user, pointsToAdd ->
        val newPoints = user.points + pointsToAdd
        db.collection("users").document(user.userId)
            .update("points", newPoints)
            .addOnSuccessListener {
                Log.d("AdminDashboardScreen", "Points for user ${user.userName} added to $newPoints")
                users = users.map {
                    if (it.userId == user.userId) it.copy(points = newPoints) else it
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminDashboardScreen", "Error adding points", e)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Golden TrendZ Users List",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    containerColor = Color(0xD29504AD)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (users.isEmpty()) {
                Text("No users found.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserPointsCard(
                            user = user,
                            onReducePoints = onReducePoints,
                            onAddPoints = onAddPoints
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPointsCard(user: UserPoints, onReducePoints: (UserPoints, Long) -> Unit, onAddPoints: (UserPoints, Long) -> Unit) {
    var pointsToReduce by remember { mutableStateOf("") }
    var pointsToAdd by remember { mutableStateOf("") }
    var showReduceDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User: ${user.userName}", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
            Text("Mobile: ${user.mobileNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Points: ${user.points}", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

//            Spacer(modifier = Modifier.height(16.dp))
//
//            OutlinedTextField(
//                value = pointsToReduce,
//                onValueChange = { newValue -> pointsToReduce = newValue.filter { it.isDigit() } },
//                label = { Text("Enter Points to Reduce") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true
//            )
            Spacer(modifier = Modifier.height(8.dp))

//            Button(
//                onClick = {
//                    val points = pointsToReduce.toLongOrNull()
//                    if (points != null && points > 0) {
//                        showReduceDialog = true
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
//            ) {
//                Text("Reduce Points")
//            }

//            Spacer(modifier = Modifier.height(16.dp))
//
//            OutlinedTextField(
//                value = pointsToAdd,
//                onValueChange = { newValue -> pointsToAdd = newValue.filter { it.isDigit() } },
//                label = { Text("Enter Points to Add") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true
//            )
            Spacer(modifier = Modifier.height(8.dp))

//            Button(
//                onClick = {
//                    val points = pointsToAdd.toLongOrNull()
//                    if (points != null && points > 0) {
//                        showAddDialog = true
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
//            ) {
//                Text("Add Points")
//            }
        }
    }

    if (showReduceDialog) {
        AlertDialog(
            onDismissRequest = { showReduceDialog = false },
            title = { Text("Confirm Point Reduction") },
            text = { Text("Are you sure you want to reduce ${pointsToReduce.toLongOrNull()} points from ${user.userName}'s account?") },
            confirmButton = {
                Button(onClick = {
                    val points = pointsToReduce.toLongOrNull()
                    if (points != null && points > 0) {
                        onReducePoints(user, points)
                        pointsToReduce = ""
                    }
                    showReduceDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                Button(onClick = { showReduceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Confirm Point Addition") },
            text = { Text("Are you sure you want to add ${pointsToAdd.toLongOrNull()} points to ${user.userName}'s account?") },
            confirmButton = {
                Button(onClick = {
                    val points = pointsToAdd.toLongOrNull()
                    if (points != null && points > 0) {
                        onAddPoints(user, points)
                        pointsToAdd = ""
                    }
                    showAddDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}