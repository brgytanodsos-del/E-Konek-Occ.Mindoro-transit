package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.model.*
import com.example.data.repository.*
import com.example.ui.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.dao()
    
    private val scheduleRepository = ScheduleRepository(db, application)
    private val authRepository = AuthRepository(application)

    // Authentication
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _currentRole = MutableStateFlow<String?>(null)
    val currentRole = _currentRole.asStateFlow()

    // Data
    val ships = scheduleRepository.getAllShips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val trips = scheduleRepository.getAllTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val bookings = scheduleRepository.getAllBookings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _uiState = MutableStateFlow<UiState<Any>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)

    fun login(role: String, pin: String?) {
        viewModelScope.launch {
            val isValid = authRepository.validateLogin(role, pin)
            if (isValid) {
                _currentRole.value = role
                _isAuthenticated.value = true
                _toastMessage.value = "Welcome, ${role.replaceFirstChar { it.uppercase() }}!"
            } else {
                _toastMessage.value = "Invalid credentials"
            }
        }
    }

    // FIXED: Proper commission calculation
    fun calculateGrossAndCommission(booking: Booking): Pair<Double, Double> {
        val basePrice = when (booking.type) {
            "Ferry" -> when (booking.ticketType) {
                "Student" -> AppConstants.PRICE_FERRY_STUDENT
                else -> AppConstants.PRICE_FERRY_REGULAR
            }
            "Van", "Bus" -> when (booking.ticketType) {
                "Student" -> AppConstants.PRICE_VAN_STUDENT
                else -> AppConstants.PRICE_VAN_REGULAR
            }
            else -> 0.0
        }

        val gross = basePrice * (booking.seats ?: 1)
        val commissionRate = when (booking.type) {
            "Ferry" -> AppConstants.COMMISSION_FERRY
            else -> AppConstants.COMMISSION_VAN
        }

        return Pair(gross, gross * commissionRate)
    }

    fun confirmBooking(booking: Booking) {
        viewModelScope.launch {
            val (gross, commission) = calculateGrossAndCommission(booking)
            val updatedBooking = booking.copy(
                status = "Confirmed",
                amount = gross,
                commission = commission,
                isSyncing = true
            )
            scheduleRepository.insertBooking(updatedBooking)
            _toastMessage.value = "Booking confirmed - ₱$gross"
        }
    }
}
