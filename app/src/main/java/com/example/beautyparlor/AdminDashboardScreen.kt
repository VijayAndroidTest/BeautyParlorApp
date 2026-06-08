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
                        "Golden TrendZ Admin",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // New "View Stats" button at the top
            Button(
                onClick = { navController.navigate("adminStats") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("View Daily Stats", color = Color.White)
            }

            Box(
                modifier = Modifier.fillMaxSize(),
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pointsToAdd,
                    onValueChange = { pointsToAdd = it },
                    label = { Text("Add Points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val points = pointsToAdd.toLongOrNull()
                    if (points != null && points > 0) {
                        onAddPoints(user, points)
                        pointsToAdd = ""
                    }
                }) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pointsToReduce,
                    onValueChange = { pointsToReduce = it },
                    label = { Text("Reduce Points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val points = pointsToReduce.toLongOrNull()
                    if (points != null && points > 0) {
                        onReducePoints(user, points)
                        pointsToReduce = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("-")
                }
            }
        }
    }
}