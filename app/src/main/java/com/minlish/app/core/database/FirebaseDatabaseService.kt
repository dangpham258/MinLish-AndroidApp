package com.minlish.app.core.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minlish.app.features.auth.domain.model.User
import com.minlish.app.features.auth.domain.model.UserProfile
import com.minlish.app.features.auth.domain.model.UserSetting
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
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val createdAt = dateFormat.format(java.util.Date())
            
            val userData = mapOf(
                "id" to uid,
                "name" to name,
                "email" to email,
                "createdAt" to createdAt,
                "userProfile" to mapOf(
                    "learningGoals" to emptyList<String>(),
                    "initialLevel" to "B1"
                ),
                "userSetting" to mapOf(
                    "dailyNewWordGoal" to 10,
                    "dailyReviewGoal" to 50
                )
            )
            userRef.setValue(userData).await()
        }
    }

    suspend fun getUser(uid: String): DataSnapshot? {
        return usersRef.child(uid).get().await()
    }

    suspend fun getUserFromSnapshot(uid: String): User? {
        val snapshot = usersRef.child(uid).get().await()
        return snapshot.toUser()
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

    suspend fun updateUserProfile(uid: String, learningGoals: List<String>, initialLevel: String) {
        val updates = mapOf(
            "userProfile/learningGoals" to learningGoals,
            "userProfile/initialLevel" to initialLevel
        )
        usersRef.child(uid).updateChildren(updates).await()
    }

    suspend fun updateUserSetting(uid: String, dailyNewWordGoal: Int, dailyReviewGoal: Int) {
        val updates = mapOf(
            "userSetting/dailyNewWordGoal" to dailyNewWordGoal,
            "userSetting/dailyReviewGoal" to dailyReviewGoal
        )
        usersRef.child(uid).updateChildren(updates).await()
    }

    suspend fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().await()
    }

    private fun DataSnapshot.toUser(): User? {
        return try {
            User(
                id = key ?: "",
                name = child("name").getValue(String::class.java) ?: "",
                email = child("email").getValue(String::class.java) ?: "",
                createdAt = child("createdAt").getValue(String::class.java) ?: "",
                userProfile = UserProfile(
                    learningGoals = child("userProfile/learningGoals").children.mapNotNull {
                        it.getValue(String::class.java)
                    },
                    initialLevel = child("userProfile/initialLevel").getValue(String::class.java) ?: "B1"
                ),
                userSetting = UserSetting(
                    dailyNewWordGoal = child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 10,
                    dailyReviewGoal = child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 50
                )
            )
        } catch (e: Exception) {
            null
        }
    }
}
