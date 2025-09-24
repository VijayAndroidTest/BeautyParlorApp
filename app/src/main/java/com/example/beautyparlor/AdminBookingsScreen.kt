package com.example.beautyparlor



import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(navController: NavController) {
    val tabs = listOf("BOOKED", "CANCELLED", "COMPLETED")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    // Admin login state
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Function to update bookingStatus
    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        try {
            val query = db.collection("bookings")
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            if (!query.isEmpty) {
                val document = query.documents[0]
                db.collection("bookings")
                    .document(document.id)
                    .update("bookingStatus", newStatus)
                    .await()
                Log.d("AdminBookingsScreen", "Booking $bookingId updated to $newStatus")
            }
        } catch (e: Exception) {
            Log.e("AdminBookingsScreen", "Error updating booking $bookingId to $newStatus", e)
        }
    }

    // Firestore listener that reacts when page tab changes or isAdmin toggles
    DisposableEffect(pagerState.currentPage, isAdmin) {
        val currentUserId = auth.currentUser?.uid
        var listenerRegistration: ListenerRegistration? = null

        isLoading = true
        val statusToFilter = tabs[pagerState.currentPage].lowercase()
        val baseQuery = db.collection("bookings")
            .whereEqualTo("bookingStatus", statusToFilter)

        val finalQuery = if (isAdmin) {
            baseQuery
        } else {
            // If not admin, restrict to only current user's bookings
            baseQuery.whereEqualTo("userId", currentUserId)
        }

        listenerRegistration = finalQuery.addSnapshotListener { snapshot, e ->
            if (e != null) {
                bookings = emptyList()
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val newList = snapshot.documents.mapNotNull { doc ->
                    try {
                        Booking.fromFirestore(doc as QueryDocumentSnapshot)
                    } catch (ex: Exception) {
                        null
                    }
                }
                bookings = newList
                isLoading = false
            }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) "Admin - Manage Bookings" else "My Bookings",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdminDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Access",
                            tint = if (isAdmin) Color.Green else Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFFC2185B),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color(0xFF9C27B0),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                title,
                                color = if (pagerState.currentPage == index) Color(0xFF9C27B0) else Color.Gray,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    // show spinner
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF9C27B0))
                    }
                } else if (bookings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No ${tabs[pagerState.currentPage].lowercase()} bookings",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingCardAdmin(
                                booking = booking,
                                isAdmin = isAdmin,
                                onUpdateStatus = { id, status ->
                                    scope.launch { updateBookingStatus(id, status) }
                                },
                                onRescheduleCancel = {
                                    // optional: allow admin to open dialog or navigate
                                    selectedBooking = booking
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Admin password dialog
        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                title = { Text("Admin Login") },
                text = {
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Enter Admin Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (adminPasswordInput == "admin123") {
                            isAdmin = true
                        } else {
                            isAdmin = false
                        }
                        showAdminDialog = false
                        adminPasswordInput = ""
                    }) {
                        Text("LOGIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAdminDialog = false
                        adminPasswordInput = ""
                    }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Optional: dialog for reschedule / cancel
        if (showDialog && selectedBooking != null) {
            RescheduleCancelDialog(
                booking = selectedBooking!!,
                onDismiss = {
                    showDialog = false
                    selectedBooking = null
                },
                onCancel = {
                    scope.launch {
                        updateBookingStatus(selectedBooking!!.bookingId, "cancelled")
                    }
                    showDialog = false
                    selectedBooking = null
                },
                onReschedule = {
                    // maybe navigate to booking summary or editing screen
                    val b = selectedBooking!!
                    showDialog = false
                    selectedBooking = null
                    val encodedService = Uri.encode(b.serviceName)
                    val encodedDate = Uri.encode(b.appointmentDate)
                    val encodedTime = Uri.encode(b.appointmentTime)
                    navController.navigate("bookingSummary/$encodedService/${b.servicePrice}/$encodedDate/$encodedTime")
                }
            )
        }
    }
}
@Composable
fun BookingCardAdmin(
    booking: Booking,
    isAdmin: Boolean,
    onUpdateStatus: (String, String) -> Unit,
    onRescheduleCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Booking ID: ${booking.bookingId}", fontSize = 14.sp, color = Color.Gray)

            Text(
                text = "Status: ${booking.bookingStatus.uppercase()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when (booking.bookingStatus.lowercase()) {
                    "booked" -> Color(0xFF4CAF50)
                    "cancelled" -> Color.Red
                    "completed" -> Color(0xFF2196F3)
                    else -> Color.Gray
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Service: ${booking.serviceName}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Price: ₹${booking.servicePrice}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Date: ${booking.appointmentDate}", fontSize = 15.sp)
            Text("Time: ${booking.appointmentTime}", fontSize = 15.sp)

            if (isAdmin) {
                // show user info
                booking.userName?.let {
                    Text("User: $it", fontSize = 14.sp, color = Color.DarkGray)
                }
                booking.userMobile?.let {
                    Text("Mobile: $it", fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isAdmin) {
                when (booking.bookingStatus.lowercase()) {
                    "booked" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { onUpdateStatus(booking.bookingId, "completed") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Text("Mark Completed", color = Color.White)
                            }
                            Button(
                                onClick = { onUpdateStatus(booking.bookingId, "cancelled") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    }
                    "cancelled" -> {
                        Button(
                            onClick = { onUpdateStatus(booking.bookingId, "booked") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Restore to Booked", color = Color.White)
                        }
                    }
                    "completed" -> {
                        // maybe allow toggle back? Or just show that it is completed
                        Text("Completed", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else {
                // Non-admin can have reschedule/cancel if status is booked
                if (booking.bookingStatus.lowercase() == "booked") {
                    Button(
                        onClick = onRescheduleCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Text("Reschedule/Cancel", color = Color.White)
                    }
                }
            }
        }
    }
}