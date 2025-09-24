package com.example.beautyparlor

import com.google.firebase.firestore.FirebaseFirestore

fun getBookingsForDate(date: String, onResult: (List<Booking>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("bookings")
        .whereEqualTo("date", date)
        .get()
        .addOnSuccessListener { result ->
            val bookings = result.toObjects(Booking::class.java)
            onResult(bookings)
        }
}