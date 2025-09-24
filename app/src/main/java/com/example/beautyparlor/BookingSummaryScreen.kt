package com.example.beautyparlor

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beautyparlor.entities.ServiceSubItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    navController: NavController,
    selectedService: ServiceSubItem?,
    serviceName: String?,
    selectedDate: String?,
    selectedTime: String?
) {
    val context = LocalContext.current
    var bookingNotes by remember { mutableStateOf("") }
    var isBooking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Firebase instances
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Your business WhatsApp number
    val businessWhatsAppNumber = "917092842454"

    suspend fun getUserInfo(): Pair<String?, String?> {
        val currentUser = auth.currentUser ?: return Pair(null, null)
        return try {
            val userDocument = firestore.collection("users").document(currentUser.uid).get().await()
            val phoneNumber = userDocument.getString("mobileNumber")
            // The previous code had a bug here, getting "name" instead of "fullName".
            // It is now corrected to get "name" to match your Firestore schema.
            val name = userDocument.getString("name")
            Pair(phoneNumber, name)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    suspend fun saveBookingToFirestore(): Result<BookingResponse> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                throw Exception("User not authenticated")
            }

            if (selectedService == null || selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty()) {
                throw Exception("Missing required booking information")
            }

            val bookingId = "BK${System.currentTimeMillis()}"
            val currentTimestamp = com.google.firebase.Timestamp.now()

            val (phoneNumber, customerName) = getUserInfo()

            val bookingData = hashMapOf(
                "bookingId" to bookingId,
                "userId" to currentUser.uid,
                "userEmail" to (currentUser.email ?: ""),
                "userName" to (customerName ?: currentUser.displayName ?: ""),
                "userPhone" to (phoneNumber ?: ""),
                "serviceName" to (serviceName ?: "N/A"),
                "subItemName" to selectedService.subItemName,
                "servicePrice" to selectedService.priceRange,
                "appointmentDate" to selectedDate,
                "appointmentTime" to selectedTime,
                "bookingNotes" to bookingNotes.trim(),
                "bookingStatus" to "booked",
                "paymentStatus" to "pending",
                "createdAt" to currentTimestamp,
                "updatedAt" to currentTimestamp,
                "deviceInfo" to "Android",
                "appVersion" to "1.0.0"
            )

            val documentReference = firestore
                .collection("bookings")
                .add(bookingData)
                .await()

            val savedDocument = documentReference.get().await()

            if (savedDocument.exists()) {
                val response = BookingResponse(
                    success = true,
                    documentId = documentReference.id,
                    bookingId = bookingId,
                    message = "Appointment booked successfully",
                    appointmentDate = selectedDate,
                    appointmentTime = selectedTime,
                    serviceName = selectedService.subItemName,
                    totalAmount = selectedService.priceRange
                )
                Result.success(response)
            } else {
                throw Exception("Failed to verify booking save")
            }
        } catch (e: Exception) {
            val errorResponse = BookingResponse(
                success = false,
                message = "Booking failed: ${e.message}"
            )
            Result.failure(Exception(errorResponse.message))
        }
    }

    fun sendBookingToBusinessWhatsApp(
        bookingResponse: BookingResponse,
        customerPhone: String?,
        customerName: String?,
        parentServiceName: String?
    ) {
        val serviceDetails = if (parentServiceName.isNullOrBlank()) {
            bookingResponse.serviceName
        } else {
            "$parentServiceName - ${bookingResponse.serviceName}"
        }

        val message = """
            🔔 *New Appointment Booked!*

            📋 *Booking Details:*
            • Booking ID: ${bookingResponse.bookingId}
            • Service: $serviceDetails
            • Date: ${bookingResponse.appointmentDate}
            • Time: ${bookingResponse.appointmentTime}
            • Amount: ₹${bookingResponse.totalAmount}

            👤 *Customer Info:*
            • Name: ${customerName ?: "N/A"}
            • Phone: ${customerPhone ?: "N/A"}
            • Email: ${auth.currentUser?.email ?: "N/A"}

            📝 *Notes:* ${if (bookingNotes.isNotBlank()) bookingNotes else "No special notes"}

            ⏰ *Booked at:* ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}

            Please confirm this appointment with the customer.
        """.trimIndent()

        val encodedMessage = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$businessWhatsAppNumber&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b")
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "WhatsApp not available. Please install WhatsApp to receive notifications.", Toast.LENGTH_LONG).show()
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    context.startActivity(browserIntent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "Unable to send WhatsApp message", Toast.LENGTH_SHORT).show()
                }
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
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = !isBooking
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Services Added",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // ➡️ UNDERLINE AND BOLD SERVICE SUB-ITEM
                            Text(
                                text = selectedService?.subItemName ?: "N/A",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = Color.Black
                            )
                            Text(
                                text = serviceName ?: "Beauty Service",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${selectedService?.priceRange ?: "0"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Service",
                                tint = Color.Red,
                                modifier = Modifier.clickable {
                                    if (!isBooking) {
                                        Toast.makeText(context, "Service removed!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Total *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "₹${selectedService?.priceRange ?: "0"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Appointment Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                color = Color.DarkGray
            )

            val formattedDateTime = try {
                if (selectedDate != null && selectedTime != null) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val date = inputFormat.parse(selectedDate)
                    "${outputFormat.format(date!!)} at $selectedTime"
                } else {
                    "N/A"
                }
            } catch (e: Exception) {
                "${selectedDate ?: "N/A"} ${selectedTime ?: "N/A"}"
            }

            Text(
                text = formattedDateTime,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Booking Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = bookingNotes,
                onValueChange = { if (!isBooking) bookingNotes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                enabled = !isBooking,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (bookingNotes.isEmpty()) {
                            Text(
                                text = "Type your notes....",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (auth.currentUser == null) {
                            Toast.makeText(context, "Please log in to book appointment", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        isBooking = true

                        val result = saveBookingToFirestore()

                        result.fold(
                            onSuccess = { response ->
                                isBooking = false

                                Toast.makeText(
                                    context,
                                    "✓ Appointment booked successfully!",
                                    Toast.LENGTH_LONG
                                ).show()

                                coroutineScope.launch {
                                    val (customerPhone, customerName) = getUserInfo()
                                    sendBookingToBusinessWhatsApp(response, customerPhone, customerName, serviceName)
                                }

                                navController.navigate("mainScreen") {
                                    popUpTo("mainScreen") { inclusive = true }
                                }
                            },
                            onFailure = { error ->
                                isBooking = false
                                Toast.makeText(
                                    context,
                                    "❌ Booking failed: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isBooking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isBooking) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "BOOKING...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "BOOK APPOINTMENT",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}