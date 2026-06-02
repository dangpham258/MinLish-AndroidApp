package com.minlish.app.presentation.main

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MinLishApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // The google-services.json plugin automatically initializes Firebase.
        // Manual initialization here with placeholder values was overriding the correct config
        // and causing Google Sign-In to fail.
        Log.i("MinLishApp", "Application onCreate - Firebase auto-initialized by google-services plugin.")
    }
}
