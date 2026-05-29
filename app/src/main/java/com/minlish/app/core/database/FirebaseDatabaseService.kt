package com.minlish.app.core.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDatabaseService @Inject constructor() {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun createUserIfNotExists(
        uid: String,
        name: String,
        email: String
    ) {
        val userRef = usersRef.child(uid)
        val snapshot = userRef.get().await()
        
        if (!snapshot.exists()) {
            val userData = mapOf(
                "name" to name,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )
            userRef.setValue(userData).await()
        }
    }

    suspend fun getUser(uid: String): DataSnapshot? {
        return usersRef.child(uid).get().await()
    }

    fun observeUser(uid: String): Flow<DataSnapshot?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        usersRef.child(uid).addValueEventListener(listener)
        awaitClose { usersRef.child(uid).removeEventListener(listener) }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        usersRef.child(uid).updateChildren(updates).await()
    }

    suspend fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().await()
    }
}
