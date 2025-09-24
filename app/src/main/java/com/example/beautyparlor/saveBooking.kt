package com.example.beautyparlor

import com.google.firebase.firestore.FirebaseFirestore

fun saveBooking(booking: Booking, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("bookings")
        .add(booking)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e) }
}