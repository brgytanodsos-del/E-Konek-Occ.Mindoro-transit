package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.util.NetworkUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository(
    private val database: AppDatabase,
    private val context: Context
) {

    private val dao = database.dao()

    // ==================== READ OPERATIONS ====================

    fun getAllShips(): Flow<List<Ship>> = dao.getAllShips()
    fun getAllTrips(): Flow<List<Trip>> = dao.getAllTrips()
    fun getAllBookings(): Flow<List<Booking>> = dao.getAllBookings()
    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()

    fun getPendingSyncBookings(): Flow<List<Booking>> = 
        dao.getAllBookings().map { bookings ->
            bookings.filter { it.isSyncing == true }
        }

    // ==================== WRITE OPERATIONS ====================

    suspend fun insertBooking(booking: Booking) {
        database.withTransaction {
            dao.insertBooking(booking)
            // Also create transaction record
            val transaction = Transaction(
                id = "tx_${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis().toString(),
                type = booking.type,
                bookingId = booking.id,
                passengerName = booking.name,
                route = booking.route, // Assuming booking now has route
                ticketType = booking.ticketType ?: "Regular",
                grossAmount = booking.amount ?: 0.0,
                commissionAmount = booking.commission ?: 0.0,
                confirmedBy = "System",
                status = "Completed",
                paid = false
            )
            dao.insertTransaction(transaction)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        dao.getBookingById(bookingId)?.let { booking ->
            val updated = booking.copy(
                status = newStatus,
                lastUpdated = System.currentTimeMillis()
            )
            dao.insertBooking(updated)
        }
    }

    suspend fun updateShipStatus(shipId: String, newStatus: String) {
        dao.getShipById(shipId)?.let { ship ->
            val updated = ship.copy(
                status = newStatus,
                lastUpdated = System.currentTimeMillis()
            )
            dao.insertShips(listOf(updated))
        }
    }

    // ==================== OFFLINE SYNC (FIXED) ====================

    suspend fun processOfflineQueue() = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) return@withContext

        val pendingBookings = dao.getPendingSyncBookings()

        pendingBookings.forEach { booking ->
            try {
                // TODO: Replace with real API call when backend is ready
                val success = syncBookingToBackend(booking)

                if (success) {
                    val syncedBooking = booking.copy(
                        isSyncing = false,
                        syncedAt = System.currentTimeMillis()
                    )
                    dao.insertBooking(syncedBooking)

                    // Mark transaction as paid
                    dao.getTransactionByBookingId(booking.id)?.let { tx ->
                        dao.insertTransaction(tx.copy(paid = true))
                    }
                }
            } catch (e: Exception) {
                // Keep in queue for retry
                e.printStackTrace()
            }
        }
    }

    private suspend fun syncBookingToBackend(booking: Booking): Boolean {
        // Simulate network delay
        kotlinx.coroutines.delay(800)
        return true // For now, always succeed in demo
    }

    // ==================== SEED DATA ====================

    suspend fun seedInitialData() {
        if (dao.getAllShips().firstOrNull().isNullOrEmpty()) {
            database.withTransaction {
                // Ships (Ferries)
                dao.insertShips(listOf(
                    Ship(id = "s1", name = "MV Maria Gloria", route = "Mamburao ↔ Batangas", departureTime = "06:00", arrivalTime = "11:00", status = "Scheduled", capacity = 520, currentPassengers = 145, type = "Ferry"),
                    Ship(id = "s2", name = "MV Don Anselmo", route = "San Jose ↔ Batangas", departureTime = "08:30", arrivalTime = "14:30", status = "Boarding", capacity = 420, currentPassengers = 380, type = "Ferry")
                ))

                // Trips (Land Transport)
                dao.insertTrips(listOf(
                    Trip(id = "t1", route = "Mamburao to San Jose", departureTime = "07:15", vehicleType = "Van", driver = "Mang Rudy", capacity = 14, booked = 9, status = "On Time")
                ))
            }
        }
    }
}
