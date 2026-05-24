package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.model.*
import com.example.data.repository.*
import com.example.utils.generateId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.dao()
    private val shipRepository = ShipRepository(dao)
    private val tripRepository = TripRepository(dao)
    private val announcementRepository = AnnouncementRepository(dao)

    val ships = shipRepository.getAllShips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trips = tripRepository.getAllTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val announcements = announcementRepository.getAllAnnouncements().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateShipStatus(ship: Ship, status: String) {
        viewModelScope.launch {
            shipRepository.updateShip(ship.copy(status = status))
        }
    }

    fun updateTripStatus(trip: Trip, status: String) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip.copy(status = status))
        }
    }

    fun addAnnouncement(text: String, author: String) {
        viewModelScope.launch {
            announcementRepository.insertAnnouncement(Announcement(
                id = generateId(),
                message = text,
                author = author
            ))
        }
    }
}
