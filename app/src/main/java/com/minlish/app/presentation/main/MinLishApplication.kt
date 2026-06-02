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

        Log.d("MinLishApp", "Application onCreate - Starting Firebase Manual Init")

        try {
            // Check if already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                // IMPORTANT: In a real app, these values should come from google-services.json
                // or the Firebase Console. The current ones are placeholders.
                val options = FirebaseOptions.Builder()
                    .setApplicationId("com.minlish.app") // Use package name as fallback for App ID if real one is missing
                    .setApiKey("none") // Placeholder that might be safer than "placeholder_api_key"
                    .setDatabaseUrl("https://minlish-1e2ec-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .setProjectId("minlish-1e2ec")
                    .build()

                FirebaseApp.initializeApp(this, options)
                Log.i("MinLishApp", "Firebase manually initialized successfully.")
            }
        } catch (e: Exception) {
            Log.e("MinLishApp", "CRITICAL: Firebase manual initialization failed: ${e.message}")
        }
    }
}
