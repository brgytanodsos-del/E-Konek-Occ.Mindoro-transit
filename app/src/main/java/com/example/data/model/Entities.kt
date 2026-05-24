package com.example.data.model

import androidx.room.*
import java.time.LocalDateTime

@Entity(tableName = "ships")
data class Ship(
    @PrimaryKey val id: String,
    val name: String,
    val route: String,           // e.g., "Mamburao - Batangas"
    val departureTime: String,
    val arrivalTime: String,
    val status: String,          // Boarding, Departed, Arrived, Delayed, Cancelled
    val capacity: Int,
    val currentPassengers: Int,
    val type: String,            // RORO, Fastcraft, Passenger
    val weatherImpact: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val route: String,
    val departureTime: String,
    val type: String,            // Van, Bus, Truck
    val driver: String,
    val capacity: Int,
    val booked: Int,
    val status: String,
    val shipId: String? = null   // For sea-land combined trips
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val referenceId: String,
    val entityId: String, // shipId or tripId
    val type: String, // Ferry, Van, Bus
    val name: String,
    val contact: String,
    val ticketType: String?, // Regular, Student, Senior, PWD
    val seats: Int = 1,
    val pickup: String? = null,
    val status: String,          // Confirmed, Pending, Cancelled
    val timestamp: String,
    val amount: Double = 0.0,
    val commission: Double = 0.0,
    val paymentMethod: String = "Cash", // GCash, Cash, Maya
    val isSyncing: Boolean = false,
    val syncedAt: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
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
    val title: String = "",
    val message: String,
    val priority: String = "Normal", // High, Normal, Low
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val author: String = "System"
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userRole: String,
    val action: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val pin: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "payout_history")
data class Payout(
    @PrimaryKey val id: String,
    val date: String,
    val totalAmount: Double,
    val transactionCount: Int
)
