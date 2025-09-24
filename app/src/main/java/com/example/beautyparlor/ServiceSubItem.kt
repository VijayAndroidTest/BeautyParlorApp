package com.example.beautyparlor.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sub_items")
data class ServiceSubItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serviceName: String,
    val subItemName: String,
    val priceRange: String
)
