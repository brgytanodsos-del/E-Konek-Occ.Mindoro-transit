package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.repository.BookingRepository
import com.example.data.repository.ShipRepository
import com.example.data.repository.TripRepository
import com.example.utils.generateId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val bookingRepository = BookingRepository(db.dao())
    private val shipRepository = ShipRepository(db.dao())
    private val tripRepository = TripRepository(db.dao())

    val ships = shipRepository.getAllShips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trips = tripRepository.getAllTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookings = bookingRepository.getAllBookings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun bookFerry(ship: Ship, name: String, contact: String, ticketType: String, isOnline: Boolean) {
        val booking = Booking(
            id = generateId(),
            referenceId = "REF-${generateId().uppercase()}",
            entityId = ship.id,
            type = "Ferry",
            name = name,
            contact = contact,
            ticketType = ticketType,
            status = "Pending",
            timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
            isSyncing = !isOnline
        )
        viewModelScope.launch {
            bookingRepository.insertBooking(booking)
        }
    }

    fun bookVanBus(trip: Trip, name: String, contact: String, pickup: String, seats: Int, isOnline: Boolean) {
        val booking = Booking(
            id = generateId(),
            referenceId = "REF-${generateId().uppercase()}",
            entityId = trip.id,
            type = trip.type,
            name = name,
            contact = contact,
            ticketType = "$seats seats",
            seats = seats,
            pickup = pickup,
            status = "Pending",
            timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
            isSyncing = !isOnline
        )
        viewModelScope.launch {
            bookingRepository.insertBooking(booking)
        }
    }
}
