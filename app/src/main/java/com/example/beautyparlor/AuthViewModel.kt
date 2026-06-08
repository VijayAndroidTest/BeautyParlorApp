package com.example.beautyparlor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun createNewUser(
        uid: String,
        email: String,
        name: String,
        referralCode: String?
    ) {
        viewModelScope.launch {
            val user = User(
                uid = uid,
                name = name,
                email = email,
                points = 0,
                referralCode = UUID.randomUUID().toString(), // Generates a unique code for the new user
                referredBy = referralCode
            )

            // Save the new user to Firestore
            db.collection("users").document(uid).set(user).await()

            // Check if a referral code was provided and award points
            if (!referralCode.isNullOrBlank()) {
                awardReferralPoints(referralCode)
            }
        }
    }

    // This function awards 200 points to the referrer
    private suspend fun awardReferralPoints(referrerCode: String) {
        try {
            // Find the user who has this referral code
            val querySnapshot = db.collection("users").whereEqualTo("referralCode", referrerCode).get().await()

            if (!querySnapshot.isEmpty) {
                val referrerDocRef = querySnapshot.documents[0].reference

                // Use a transaction to safely update the points
                db.runTransaction { transaction ->
                    val referrerDoc = transaction.get(referrerDocRef)
                    val currentPoints = referrerDoc.getLong("points") ?: 0L
                    val newPoints = currentPoints + 200

                    transaction.update(referrerDocRef, "points", newPoints)
                }.await()
                Log.d("AuthViewModel", "Successfully awarded 200 points to referrer: $referrerCode")
            } else {
                Log.e("AuthViewModel", "Referral code not found: $referrerCode")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error awarding referral points: ", e)
        }
    }
}