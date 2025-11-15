package com.example.proyecto1.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.model.User
import com.example.proyecto1.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UserAdministrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    val users: StateFlow<List<User>> = userRepository.users
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addUser(user: User) {
        userRepository.addUser(user)
    }

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }

    fun deleteUser(userId: Int) {
        userRepository.deleteUser(userId)
    }
}
