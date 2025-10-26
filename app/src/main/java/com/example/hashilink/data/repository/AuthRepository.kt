package com.example.hashilink.data.repository

import com.example.hashilink.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    suspend fun signup(email: String, password: String, name: String, mobile: String, role: String): Result<User> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: throw Exception("User ID not found")
        val user = User(uid, name, email, mobile, role)
        db.child(uid).setValue(user).await()
        user
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: throw Exception("User ID not found")
        val snapshot = db.child(uid).get().await()
        snapshot.getValue(User::class.java) ?: throw Exception("User data not found")
    }

    suspend fun getUserProfile(uid: String): Result<User> = runCatching {
        val snapshot = db.child(uid).get().await()
        snapshot.getValue(User::class.java) ?: throw Exception("User data not found")
    }

    fun logout() = auth.signOut()
}