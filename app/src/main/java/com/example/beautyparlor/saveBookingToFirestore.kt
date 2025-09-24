package com.example.beautyparlor

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

//fun saveBookingToFirestore(service: Service, date: String, time: String) {
//    val db = FirebaseFirestore.getInstance()
//    val booking = hashMapOf(
//        "serviceName" to service.name,
//        "price" to service.price,
//        "date" to date,
//        "time" to time,
//        "timestamp" to System.currentTimeMillis()
//    )
//
//    db.collection("bookings")
//        .add(booking)
//        .addOnSuccessListener { documentReference ->
//            Log.d("Booking", "Booking saved with ID: ${documentReference.id}")
//        }
//        .addOnFailureListener { e ->
//            Log.w("Booking", "Error saving booking", e)
//        }
//}