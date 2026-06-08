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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beautyparlor.Booking
import com.example.beautyparlor.RescheduleCancelDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(navController: NavController) {
    val tabs = listOf("BOOKED", "CANCELLED", "COMPLETED")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    // 🔑 Admin state
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Function to check if the current user is an admin by checking Firestore
    suspend fun checkIsAdmin(uid: String?): Boolean {
        if (uid == null) return false
        return try {
            val doc = db.collection("admins").document(uid).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.e("MyBookingsScreen", "Error checking admin status", e)
            false
        }
    }

    // 🔄 LaunchedEffect to determine admin status on initial composition or auth state change
    LaunchedEffect(auth.currentUser) {
        val user = auth.currentUser
        isAdmin = if (user != null) {
            checkIsAdmin(user.uid)
        } else {
            false
        }
    }

    suspend fun updateBookingAndPoints(booking: Booking, newStatus: String) {
        val db = FirebaseFirestore.getInstance()

        if (booking.bookingStatus.lowercase() == "completed" && newStatus.lowercase() == "completed") {
            Log.d("MyBookingsScreen", "Booking already marked as completed. No points awarded again.")
            return
        }

        try {
            val bookingQuery = db.collection("bookings")
                .whereEqualTo("bookingId", booking.bookingId)
                .get()
                .await()

            if (bookingQuery.isEmpty) {
                Log.e("MyBookingsScreen", "Booking with ID ${booking.bookingId} not found.")
                return
            }

            val bookingDocRef = bookingQuery.documents[0].reference

            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(booking.userId)
                val userDoc = transaction.get(userRef)
                val currentPoints = userDoc.getLong("points") ?: 0L

                transaction.update(bookingDocRef, "bookingStatus", newStatus)
                Log.d("MyBookingsScreen", "Booking ${booking.bookingId} updated to $newStatus")

                if (newStatus.lowercase() == "completed") {
                    val priceString = booking.servicePrice ?: "0"
                    val priceRegex = "(\\d+)".toRegex()
                    val matchResult = priceRegex.find(priceString)
                    val priceValue = matchResult?.groupValues?.get(1)?.toLongOrNull() ?: 0L

                    // ⚠️ FIX: New logic to check for 10% discount eligibility
                    val pointsRequiredFor10PercentDiscount = (priceValue * 0.10).toLong()

                    if (currentPoints >= pointsRequiredFor10PercentDiscount) {
                        // User is eligible for the full 10% discount
                        val pointsToDeduct = pointsRequiredFor10PercentDiscount
                        val newPoints = currentPoints - pointsToDeduct
                        val discountedPrice = (priceValue - pointsToDeduct).coerceAtLeast(0L)

                        transaction.update(userRef, "points", newPoints)
                        transaction.update(bookingDocRef, "finalPrice", discountedPrice)
                        transaction.update(bookingDocRef, "pointsUsed", pointsToDeduct)
                        Log.d("MyBookingsScreen", "Applied 10% discount. Deducted $pointsToDeduct points. New total: $newPoints. Final price: $discountedPrice")
                    } else {
                        // User not eligible, so deduct all available points
                        val pointsToDeduct = currentPoints
                        val newPoints = 0L
                        val finalPrice = (priceValue - pointsToDeduct).coerceAtLeast(0L)

                        transaction.update(userRef, "points", newPoints)
                        transaction.update(bookingDocRef, "finalPrice", finalPrice)
                        transaction.update(bookingDocRef, "pointsUsed", pointsToDeduct)
                        Log.d("MyBookingsScreen", "User not eligible for 10% discount. Deducted all available points ($pointsToDeduct). Final price: $finalPrice")
                    }
                }
            }.await()
        } catch (e: Exception) {
            Log.e("MyBookingsScreen", "Transaction failed: ${e.message}", e)
        }
    }

    suspend fun cancelBooking(bookingId: String) {
        try {
            val query = db.collection("bookings")
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            if (!query.isEmpty) {
                val document = query.documents[0]
                db.collection("bookings")
                    .document(document.id)
                    .update("bookingStatus", "cancelled")
                    .await()
                Log.d("MyBookingsScreen", "Booking $bookingId cancelled successfully")
            }
        } catch (e: Exception) {
            Log.e("MyBookingsScreen", "Error cancelling booking: $bookingId", e)
        }
    }

    DisposableEffect(pagerState.currentPage, isAdmin) {
        val userId = auth.currentUser?.uid
        var listenerRegistration: ListenerRegistration? = null
        isLoading = true
        val collectionRef = db.collection("bookings")

        val finalQuery = if (isAdmin) {
            collectionRef
        } else {
            collectionRef
                .whereEqualTo("bookingStatus", tabs[pagerState.currentPage].lowercase())
                .whereEqualTo("userId", userId)
        }

        listenerRegistration = finalQuery.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("MyBookingsScreen", "Listen failed with error", e)
                bookings = emptyList()
                isLoading = false
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val newBookings = snapshot.documents.mapNotNull { doc ->
                    try {
                        Booking.fromFirestore(doc as QueryDocumentSnapshot)
                    } catch (ex: Exception) {
                        Log.e("MyBookingsScreen", "Failed to parse booking document: ${doc.id}", ex)
                        null
                    }
                }
                bookings = if (isAdmin) {
                    newBookings.filter { it.bookingStatus.lowercase() == tabs[pagerState.currentPage].lowercase() }
                } else {
                    newBookings
                }
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
                    Text("My Bookings", fontWeight = FontWeight.Bold, color = Color.Black)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.popBackStack()) {
                        } else {
                            navController.navigate("mainScreen") {
                                popUpTo("loginScreen") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            scope.launch { pagerState.animateScrollToPage(index) }
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF9C27B0))
                    } else if (bookings.isEmpty()) {
                        Text(
                            "No ${tabs[page].lowercase()} bookings available",
                            color = Color(0xFF9C27B0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(bookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onRescheduleClick = {
                                        selectedBooking = booking
                                        showDialog = true
                                    },
                                    isAdmin = isAdmin,
                                    onUpdateStatus = { newStatus ->
                                        scope.launch {
                                            if (newStatus.lowercase() == "completed" && isAdmin) {
                                                selectedBooking = booking
                                                showPaymentDialog = true
                                            } else {
                                                updateBookingAndPoints(booking, newStatus)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                title = { Text("Admin Status") },
                text = {
                    Text(
                        if (isAdmin) "You are logged in as an admin. No password needed."
                        else "Login with an admin account to get admin access."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showAdminDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showDialog && selectedBooking != null) {
            RescheduleCancelDialog(
                booking = selectedBooking!!,
                onDismiss = {
                    showDialog = false
                    selectedBooking = null
                },
                onCancel = {
                    scope.launch {
                        cancelBooking(selectedBooking!!.bookingId)
                        showDialog = false
                        selectedBooking = null
                    }
                },
                onReschedule = {
                    showDialog = false
                    val booking = selectedBooking
                    selectedBooking = null
                    booking?.let {
                        navController.navigate("bookingScreen/${it.serviceName}/${it.servicePrice}")
                    }
                }
            )
        }

        if (showPaymentDialog && selectedBooking != null) {
            PaymentDialog(
                booking = selectedBooking!!,
                onConfirmPayment = {
                    scope.launch {
                        updateBookingAndPoints(selectedBooking!!, "completed")
                        showPaymentDialog = false
                        selectedBooking = null
                    }
                },
                onDismiss = {
                    showPaymentDialog = false
                    selectedBooking = null
                }
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onRescheduleClick: () -> Unit,
    isAdmin: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Booking ID: ${booking.bookingId}", fontSize = 14.sp, color = Color.Gray)

            Text(
                "Status: ${booking.bookingStatus.uppercase()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when (booking.bookingStatus.lowercase()) {
                    "booked" -> Color(0xFF4CAF50)
                    "cancelled" -> Color.Red
                    "completed" -> Color(0xFF2196F3)
                    else -> Color.Gray
                }
            )

            Spacer(Modifier.height(8.dp))

            Text("Service: ${booking.serviceName}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Price: ₹${booking.servicePrice}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Date: ${booking.appointmentDate}", fontSize = 15.sp)
            Text("Time: ${booking.appointmentTime}", fontSize = 15.sp)

            Spacer(Modifier.height(12.dp))

            if (booking.bookingStatus.lowercase() == "booked") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onRescheduleClick) {
                        Text("Reschedule/Cancel")
                    }
                    if (isAdmin) {
                        Button(
                            onClick = { onUpdateStatus("completed") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("Mark Completed", color = Color.White)
                        }
                    }
                }
            } else if (booking.bookingStatus.lowercase() == "cancelled" && isAdmin) {
                Button(
                    onClick = { onUpdateStatus("booked") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Restore to Booked", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(booking: Booking, onConfirmPayment: () -> Unit, onDismiss: () -> Unit) {
    var points by remember { mutableStateOf(0L) }
    var finalPrice by remember { mutableStateOf<Long?>(null) }
    var pointsUsed by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    LaunchedEffect(booking.userId) {
        isLoading = true
        try {
            val userDoc = db.collection("users").document(booking.userId).get().await()
            val userPoints = userDoc.getLong("points") ?: 0L
            points = userPoints

            val priceString = booking.servicePrice ?: "0"
            val priceRegex = "(\\d+)".toRegex()
            val matchResult = priceRegex.find(priceString)
            val priceValue = matchResult?.groupValues?.get(1)?.toLongOrNull() ?: 0L

            // ⚠️ FIX: New logic to check for 10% discount eligibility
            val pointsRequiredFor10PercentDiscount = (priceValue * 0.10).toLong()

            if (userPoints >= pointsRequiredFor10PercentDiscount) {
                // User is eligible for the full 10% discount
                pointsUsed = pointsRequiredFor10PercentDiscount
                finalPrice = priceValue - pointsUsed
            } else {
                // User not eligible, so deduct all available points
                pointsUsed = userPoints
                finalPrice = (priceValue - pointsUsed).coerceAtLeast(0L)
            }
        } catch (e: Exception) {
            Log.e("PaymentDialog", "Error fetching user points or calculating price", e)
            val priceString = booking.servicePrice ?: "0"
            val priceRegex = "(\\d+)".toRegex()
            val matchResult = priceRegex.find(priceString)
            finalPrice = matchResult?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            pointsUsed = 0L
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Payment Confirmation") },
            text = {
                Column {
                    Text("Booking ID: ${booking.bookingId}")
                    Text("Service: ${booking.serviceName}")
                    Spacer(Modifier.height(8.dp))
                    Text("Original Price: ₹${booking.servicePrice}", fontWeight = FontWeight.Bold)

                    if (pointsUsed > 0) {
                        Text("Points Used: $pointsUsed points", color = Color.Green, fontSize = 14.sp)
                    }

                    Text("Final Price: ₹$finalPrice", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 20.sp)
                }
            },
            confirmButton = {
                Button(onClick = onConfirmPayment) {
                    Text("Mark as Paid & Complete Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}