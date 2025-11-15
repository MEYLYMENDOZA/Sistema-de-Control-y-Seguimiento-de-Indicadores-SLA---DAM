package com.example.proyecto1.data.model

import java.util.Date

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val role: Role,
    val status: String, // "Verified" or "Pending"
    val createdAt: Date = Date()
)
