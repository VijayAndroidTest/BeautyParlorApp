package com.example.beautyparlor

data class BookingData(
    val bookingId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val serviceName: String = "",
    val servicePrice: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val bookingNotes: String = "",
    val bookingStatus: String = "confirmed",
    val paymentStatus: String = "pending",
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val deviceInfo: String = "",
    val appVersion: String = ""
)
