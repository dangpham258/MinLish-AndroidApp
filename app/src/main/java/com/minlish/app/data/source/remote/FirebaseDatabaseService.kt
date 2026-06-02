package com.minlish.app.data.source.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minlish.app.domain.model.Account
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.UserProfile
import com.minlish.app.domain.model.UserSetting
import com.minlish.app.domain.model.enumration.InitialLevel
import com.minlish.app.domain.model.enumration.LearningGoal
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
            // Tao moi - set tat ca cac field
            val userData = mapOf(
                "id" to uid,
                "name" to name,
                "account" to mapOf(
                    "email" to email,
                    "password" to ""
                ),
                "userProfile" to mapOf(
                    "learningGoal" to emptyList<String>(),
                    "initialLevel" to InitialLevel.B1.value,
                    "emailNotification" to false,
                    "dailyReminder" to true,
                    "spacedRepetition" to true
                ),
                "userSetting" to mapOf(
                    "dailyNewWordGoal" to 10,
                    "dailyReviewGoal" to 50
                ),
                "avatarIndex" to 0,
                "wordsLearned" to 0,
                "streak" to 0
            )
            userRef.setValue(userData).await()
        } else {
            // User da ton tai - migrate len schema moi
            migrateUserToNewSchema(userRef, snapshot, email)
        }
    }

    private suspend fun migrateUserToNewSchema(userRef: com.google.firebase.database.DatabaseReference, snapshot: DataSnapshot, email: String) {
        val updates = mutableMapOf<String, Any?>()
        
        // Lay gia tri hien tai
        val currentName = snapshot.child("name").getValue(String::class.java) ?: ""
        val currentEmail = snapshot.child("account/email").getValue(String::class.java) 
            ?: snapshot.child("email").getValue(String::class.java) 
            ?: email
        val currentAvatarIndex = snapshot.child("avatarIndex").getValue(Int::class.java) ?: 0
        val currentWordsLearned = snapshot.child("wordsLearned").getValue(Int::class.java) ?: 0
        val currentStreak = snapshot.child("streak").getValue(Int::class.java) ?: 0
        
        // UserProfile
        val currentLearningGoal = snapshot.child("userProfile/learningGoal").children.mapNotNull {
            it.getValue(String::class.java)
        }
        val currentInitialLevel = snapshot.child("userProfile/initialLevel").getValue(String::class.java) ?: InitialLevel.B1.value
        val currentEmailNotification = snapshot.child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false
        val currentDailyReminder = snapshot.child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: true
        val currentSpacedRepetition = snapshot.child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: true
        
        // UserSetting
        val currentDailyNewWordGoal = snapshot.child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 10
        val currentDailyReviewGoal = snapshot.child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 50
        
        // Cap nhat account
        updates["account"] = mapOf(
            "email" to currentEmail,
            "password" to ""
        )
        
        // Cap nhat userProfile
        updates["userProfile"] = mapOf(
            "learningGoal" to currentLearningGoal,
            "initialLevel" to currentInitialLevel,
            "emailNotification" to currentEmailNotification,
            "dailyReminder" to currentDailyReminder,
            "spacedRepetition" to currentSpacedRepetition
        )
        
        // Cap nhat userSetting
        updates["userSetting"] = mapOf(
            "dailyNewWordGoal" to currentDailyNewWordGoal,
            "dailyReviewGoal" to currentDailyReviewGoal
        )
        
        // Cap nhat cac field khac
        updates["name"] = currentName
        updates["avatarIndex"] = currentAvatarIndex
        updates["wordsLearned"] = currentWordsLearned
        updates["streak"] = currentStreak
        
        // Xoa cac field cu (nam o top-level)
        updates["email"] = null
        updates["createdAt"] = null
        
        // Update
        userRef.updateChildren(updates).await()
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

    suspend fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().await()
    }

    private fun DataSnapshot.toUser(): User? {
        return try {
            val learningGoalStrings = child("userProfile/learningGoal").children.mapNotNull {
                it.getValue(String::class.java)
            }
            val learningGoals = learningGoalStrings.mapNotNull { value ->
                LearningGoal.entries.find { it.value == value }
            }
            
            val initialLevelValue = child("userProfile/initialLevel").getValue(String::class.java) ?: "B1"
            val initialLevel = InitialLevel.entries.find { it.value == initialLevelValue } ?: InitialLevel.B1
            
            User(
                id = key ?: "",
                name = child("name").getValue(String::class.java) ?: "",
                account = Account(
                    email = child("account/email").getValue(String::class.java) ?: "",
                    password = child("account/password").getValue(String::class.java) ?: ""
                ),
                userProfile = UserProfile(
                    learningGoal = learningGoals,
                    initialLevel = initialLevel,
                    emailNotification = child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false,
                    dailyReminder = child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: true,
                    spacedRepetition = child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: true
                ),
                userSetting = UserSetting(
                    dailyNewWordGoal = child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 10,
                    dailyReviewGoal = child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 50
                ),
                avatarIndex = child("avatarIndex").getValue(Int::class.java) ?: 0,
                wordsLearned = child("wordsLearned").getValue(Int::class.java) ?: 0,
                streak = child("streak").getValue(Int::class.java) ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }
}
