package com.example.beautyparlor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class MyProfileScreenData(
    val email: String,
    val mobileNumber: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(navController: NavController) {
    var profileData by remember { mutableStateOf<MyProfileScreenData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Fetch user data from Firestore when the screen is first created
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            try {
                // Get the document for the current user's UID
                val documentSnapshot = firestore.collection("users").document(user.uid).get().await()
                val data = documentSnapshot.data

                if (data != null) {
                    // ✅ FIX: Directly get 'name' and 'mobileNumber' which are the correct keys from SignupScreen.kt
                    val userName = data["name"] as? String ?: user.displayName ?: "User"
                    val userMobile = data["mobileNumber"] as? String ?: "N/A"
                    val userEmail = data["dummyEmail"] as? String ?: user.email ?: "N/A"

                    profileData = MyProfileScreenData(
                        email = userEmail,
                        mobileNumber = userMobile,
                        name = userName
                    )

                } else {
                    // No document data, use Firebase Auth info as fallback
                    profileData = MyProfileScreenData(
                        email = user.email ?: "N/A",
                        mobileNumber = "N/A",
                        name = user.displayName ?: "User"
                    )
                }
            } catch (e: Exception) {
                // Handle potential errors (e.g., Firestore permission issues)
                println("Error fetching user data: ${e.message}")

                // Fallback to Firebase Auth data
                profileData = MyProfileScreenData(
                    email = user.email ?: "N/A",
                    mobileNumber = "N/A",
                    name = user.displayName ?: "User"
                )
            } finally {
                isLoading = false
            }
        } else {
            // No logged-in user, navigate back to login
            navController.navigate("loginScreen") {
                popUpTo("myProfile") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF9C27B0))
            }
        } else if (profileData != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profileData!!.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            color = Color(0xFF666666),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    ProfileInfoItem(
                        label = "Mobile Number",
                        value = profileData!!.mobileNumber
                    )
                }
                item {
                    ProfileInfoItem(
                        label = "Email Address",
                        value = if (profileData!!.email.contains("@beautyparlor.app")) "N/A" else profileData!!.email
                    )
                }
                item {
                    ProfileMenuItem(title = "My Booking", onClick = { navController.navigate("myBookings") })
                }
                item {
                    ProfileMenuItem(title = "My Referral", onClick = { /* Handle my referral click */ })
                }
                item {
                    ProfileMenuItem(title = "Help & Support", onClick = { navController.navigate("helpSupport") })
                }
                item {
                    ProfileMenuItem(
                        title = "Logout",
                        onClick = {
                            auth.signOut()
                            navController.navigate("loginScreen") {
                                popUpTo("mainScreen") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 16.sp, color = Color(0xFF666666), fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFF666666), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp, color = Color(0xFF666666), fontWeight = FontWeight.Normal)
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
        }
        Divider(color = Color(0xFF666666), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
    }
}