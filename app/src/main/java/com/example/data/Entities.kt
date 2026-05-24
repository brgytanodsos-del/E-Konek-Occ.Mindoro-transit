package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Entity(tableName = "ships")
data class Ship(
    @PrimaryKey val id: String,
    val name: String,
    val route: String,
    val depTime: String,
    val arrTime: String,
    val status: String, // Scheduled, Boarding, Departed, Delayed, Cancelled
    val capacity: Int,
    val available: Int,
    val type: String // RORO, Passenger Ferry
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val route: String,
    val depTime: String,
    val type: String, // Van, Bus
    val driver: String,
    val capacity: Int,
    val available: Int,
    val status: String // Scheduled, Boarding, Departed, Completed, Cancelled
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val referenceId: String,
    val entityId: String, // shipId or tripId
    val type: String, // Ferry, Van, Bus
    val name: String,
    val contact: String,
    val ticketType: String, // Regular, Student, Senior, PWD
    val seats: Int = 1,
    val pickup: String? = null,
    val status: String, // Pending, Confirmed, Cancelled
    val timestamp: String,
    val isSyncing: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val timestamp: String,
    val type: String, // Ferry, Van, Bus
    val bookingId: String,
    val passengerName: String,
    val route: String,
    val ticketType: String,
    val grossAmount: Double,
    val commissionAmount: Double,
    val confirmedBy: String,
    val status: String, // Completed, Refunded
    val paid: Boolean = false
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey val id: String,
    val text: String,
    val date: String,
    val author: String
)

@Entity(tableName = "audit_log")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val role: String,
    val action: String // login, logout
)

@Entity(tableName = "payout_history")
data class Payout(
    @PrimaryKey val id: String,
    val date: String,
    val totalAmount: Double,
    val transactionCount: Int
)
