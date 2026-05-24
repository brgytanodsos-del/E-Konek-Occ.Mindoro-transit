package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppConstants
import com.example.data.AuditLog
import com.example.data.AppDatabase
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val adminRepository = AdminRepository(db.dao())

    private val _currentRole = MutableStateFlow<String?>(null)
    val currentRole = _currentRole.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun login(role: String, pin: String?) {
        val correctPin = AppConstants.ROLE_PINS[role]
        if (pin == correctPin || role == "passenger") {
            _currentRole.value = role
            _isAuthenticated.value = true
            viewModelScope.launch {
                adminRepository.insertAuditLog(AuditLog(timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT), role = role, action = "login"))
            }
        } else {
            showToast("Incorrect PIN. Try again.")
        }
    }

    fun logout() {
        val role = _currentRole.value ?: return
        viewModelScope.launch {
            adminRepository.insertAuditLog(AuditLog(timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT), role = role, action = "logout"))
            _currentRole.value = null
            _isAuthenticated.value = false
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }
    
    fun clearToast() {
        _toastMessage.value = null
    }
}
