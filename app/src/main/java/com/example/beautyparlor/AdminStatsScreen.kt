package com.example.beautyparlor

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var loggedInUsers by remember { mutableStateOf(0) }
    var bookedUsers by remember { mutableStateOf(0) }
    var completedBookings by remember { mutableStateOf(0) }
    var referrals by remember { mutableStateOf(emptyList<Referral>()) }
    var isLoading by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Date picker dialog state
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val fetchData: suspend () -> Unit = {
        isLoading = true
        val dateString = dateFormat.format(selectedDate.time)

        try {
            // Booked bookings count
            val bookedQuery = db.collection("bookings")
                .whereEqualTo("bookingStatus", "booked")
                .whereEqualTo("appointmentDate", dateString)
                .get()
                .await()
            bookedUsers = bookedQuery.size()

            // Completed bookings count
            val completedQuery = db.collection("bookings")
                .whereEqualTo("bookingStatus", "completed")
                .whereEqualTo("appointmentDate", dateString)
                .get()
                .await()
            completedBookings = completedQuery.size()

            // Referrals data
            // Fetch all referredUsers subcollections and filter by date
            val fetchedReferrals = mutableListOf<Referral>()

            val usersQuery = db.collection("users").get().await()
            for (userDoc in usersQuery.documents) {
                val userMobileNumber = userDoc.getString("mobileNumber") ?: "N/A"

                val referredUsersQuery = userDoc.reference.collection("referredUsers")
                    .get()
                    .await()

                for (referredUserDoc in referredUsersQuery.documents) {
                    val referredDateTimestamp = referredUserDoc.getTimestamp("referredDate")
                    val referredMobile = referredUserDoc.getString("referredUserMobile") ?: "N/A"

                    // Corrected logic: format the Timestamp to a date string for comparison
                    if (referredDateTimestamp != null) {
                        val referredDateString = dateFormat.format(referredDateTimestamp.toDate())
                        if (referredDateString == dateString) {
                            fetchedReferrals.add(Referral(userMobileNumber, referredMobile))
                        }
                    }
                }
            }
            referrals = fetchedReferrals

            // To count users logged in, you need a way to track logins.
            // This query iterates through all users and checks for a login on the selected date.
            val loggedInQuery = db.collection("users")
                .get()
                .await()
            loggedInUsers = loggedInQuery.documents.filter { userDoc ->
                val creationDate = userDoc.getDate("creationDate")
                val lastLogin = userDoc.getDate("lastLogin")
                val targetDate = dateFormat.parse(dateString)

                (creationDate != null && dateFormat.format(creationDate) == dateString) ||
                        (lastLogin != null && dateFormat.format(lastLogin) == dateString)
            }.size

        } catch (e: Exception) {
            Log.e("AdminStatsScreen", "Error fetching admin data", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedDate) {
        fetchData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Stats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date Picker
            OutlinedButton(
                onClick = { showDatePickerDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Date: ${dateFormat.format(selectedDate.time)}")
            }

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedDate.timeInMillis = it
                                }
                                showDatePickerDialog = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                StatsCard(title = "Users Logged In", count = loggedInUsers)
                Spacer(Modifier.height(16.dp))
                StatsCard(title = "Bookings", count = bookedUsers)
                Spacer(Modifier.height(16.dp))
                StatsCard(title = "Bookings Completed", count = completedBookings)
                Spacer(Modifier.height(16.dp))
                //ReferralsCard(referrals = referrals) // New Composable Call
            }
        }
    }
}

// Data class for a referral record
data class Referral(
    val referrerMobile: String,
    val referredMobile: String
)

@Composable
fun StatsCard(title: String, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
        }
    }
}

//@Composable
//fun ReferralsCard(referrals: List<Referral>) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(24.dp)
//        ) {
//            Text("Referrals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
//            if (referrals.isEmpty()) {
//                Text("No referrals on this date.", color = Color.Gray)
//            } else {
//                LazyColumn(
//                    modifier = Modifier.height(200.dp) // Set a fixed height or a max height
//                ) {
//                    items(referrals) { referral ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 4.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text("Referrer: ${referral.referrerMobile}", fontSize = 14.sp)
//                            Text("Referred: ${referral.referredMobile}", fontSize = 14.sp)
//                        }
//                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
//                    }
//                }
//            }
//        }
//    }
//}