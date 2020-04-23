package com.anoop.userapp.model

import com.google.firebase.firestore.DocumentId


data class RegUser(
    @DocumentId
    val docId : String = "",
    val name: String = "",
    val address: String = "",
    val email: String = "",
    val verified: Boolean = false,
    val admin: Boolean = false
)