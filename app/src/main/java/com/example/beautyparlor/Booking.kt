package com.example.beautyparlor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.security.Timestamp

data class Booking(
    @DocumentId
    val documentId: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val userEmail: String? = null,
    val userName: String? = null,
    val serviceName: String? = null,
    val servicePrice: String? = null,
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val bookingNotes: String? = null,
    val bookingStatus: String = "",
    val paymentStatus: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deviceInfo: String? = null,
    val userMobile: String? = null,
    val appVersion: String? = null
) {
    companion object {
        /**
         * Safely creates a Booking object from a Firestore document snapshot.
         * This manual mapping is more robust than using toObject().
         */
        fun fromFirestore(doc: QueryDocumentSnapshot): Booking {
            val data = doc.data
            return Booking(
                documentId = doc.id,
                bookingId = data["bookingId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",

                userEmail = data["userEmail"] as? String,
                userMobile = data["userMobile"] as? String,
                userName = data["userName"] as? String,
                serviceName = data["serviceName"] as? String,
                servicePrice = data["servicePrice"] as? String,
                appointmentDate = data["appointmentDate"] as? String ?: "",
                appointmentTime = data["appointmentTime"] as? String ?: "",
                bookingNotes = data["bookingNotes"] as? String,
                bookingStatus = data["bookingStatus"] as? String ?: "",
                paymentStatus = data["paymentStatus"] as? String ?: "",
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp,
                deviceInfo = data["deviceInfo"] as? String,
                appVersion = data["appVersion"] as? String
            )
        }
    }
}


