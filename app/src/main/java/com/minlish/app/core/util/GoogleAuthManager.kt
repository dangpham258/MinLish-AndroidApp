package com.minlish.app.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    fun initialize() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("607388926260-net4bpfn8pof9tv8l72q80e67d3g0unk.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn(activity: Activity) {
        if (!::googleSignInClient.isInitialized) {
            initialize()
        }
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onError: (Exception) -> Unit) {
        if (data == null) {
            onError(Exception("Khong co ket qua dang nhap Google"))
            return
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        task.addOnSuccessListener { account ->
            if (account != null && account.idToken != null) {
                onSuccess(account)
            } else {
                onError(Exception("Tai khoan Google khong hop le"))
            }
        }.addOnFailureListener { exception ->
            Log.e("GoogleAuthManager", "Google Sign-In failed", exception)
            onError(exception)
        }
    }

    fun getIdToken(account: GoogleSignInAccount): String? {
        return account.idToken
    }

    fun signInWithCredential(idToken: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("GoogleAuthManager", "Firebase Auth failed", exception)
                onError(exception)
            }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        if (!::googleSignInClient.isInitialized) {
            initialize()
        }
        googleSignInClient.signOut()
            .addOnCompleteListener {
                auth.signOut()
                onComplete()
            }
    }

    fun signOutSilently() {
        try {
            if (!::googleSignInClient.isInitialized) {
                initialize()
            }
            googleSignInClient.signOut()
        } catch (_: Exception) {
            // Ignore errors
        }
    }

    fun getSignInIntent(): Intent {
        if (!::googleSignInClient.isInitialized) {
            initialize()
        }
        return googleSignInClient.signInIntent
    }

    fun getCurrentUser() = auth.currentUser

    companion object {
        const val RC_SIGN_IN = 9001
    }
}
