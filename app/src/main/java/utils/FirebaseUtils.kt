package com.example.telegrambotmanager.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserUid get() = auth.currentUser?.uid ?: ""

    fun userBotsCollection() = firestore.collection("users").document(currentUserUid).collection("bots")
}

