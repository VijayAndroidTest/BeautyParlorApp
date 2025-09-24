package com.example.beautyparlor

// Data classes for better type safety and response handling
data class BookingResponse(
    val success: Boolean = false,
    val documentId: String = "",
    val bookingId: String = "",
    val message: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val serviceName: String = "",
    val totalAmount: String = ""
)
