package com.example.beautyparlor.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.beautyparlor.entities.Service
import com.example.beautyparlor.entities.ServiceSubItem

@Dao
interface ServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<Service>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubItems(subItems: List<ServiceSubItem>)

    @Query("SELECT * FROM services WHERE category = :category")
    fun getServicesByCategory(category: String): Flow<List<Service>>

    @Query("SELECT * FROM sub_items WHERE serviceName = :serviceName")
    fun getSubItemsForService(serviceName: String): Flow<List<ServiceSubItem>>
}