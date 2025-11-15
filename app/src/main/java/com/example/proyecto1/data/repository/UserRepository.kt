package com.example.proyecto1.data.repository

import com.example.proyecto1.data.model.User
import com.example.proyecto1.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class UserRepository {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: Flow<List<User>> = _users.asStateFlow()

    private var nextId = 1

    init {
        // Add some dummy data
        addUser(User(0, "admin", "Admin User", "admin@example.com", Role.ADMIN, "Verified"))
        addUser(User(0, "analyst", "Analyst User", "analyst@example.com", Role.ANALIST, "Pending"))
    }

    fun addUser(user: User) {
        _users.value = _users.value + user.copy(id = nextId++)
    }

    fun updateUser(user: User) {
        _users.value = _users.value.map { if (it.id == user.id) user else it }
    }

    fun deleteUser(userId: Int) {
        _users.value = _users.value.filterNot { it.id == userId }
    }

    fun login(username: String, password: String): User? {
        // In a real app, you would have password hashing and verification.
        // Here we just check for the username.
        return _users.value.find { it.username == username }
    }
}
