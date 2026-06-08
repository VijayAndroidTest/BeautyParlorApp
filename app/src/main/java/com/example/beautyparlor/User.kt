package com.example.beautyparlor

import java.util.UUID

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val points: Long = 0,
    val referralCode: String = UUID.randomUUID().toString(),
    val referredBy: String? = null
)
