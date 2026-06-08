package com.example.beautyparlor

import com.google.firebase.firestore.PropertyName

data class ServiceItem(
    val name: String = "",
    val count: Long = 0,
    val imageUrl: String = ""
)
