package com.example.beautyparlor

import android.app.Application
import com.google.firebase.FirebaseApp

class BeautyParlorApplication  : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase here. This is the recommended place for a robust setup.
        FirebaseApp.initializeApp(this)
    }
}