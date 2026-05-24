package com.example.data.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow

class ShipRepository(private val dao: AppDao) {
    fun getAllShips(): Flow<List<Ship>> = dao.getAllShips()
    suspend fun insertShips(ships: List<Ship>) = dao.insertShips(ships)
    suspend fun updateShip(ship: Ship) = dao.updateShip(ship)
}

class TripRepository(private val dao: AppDao) {
    fun getAllTrips(): Flow<List<Trip>> = dao.getAllTrips()
    suspend fun insertTrips(trips: List<Trip>) = dao.insertTrips(trips)
    suspend fun updateTrip(trip: Trip) = dao.updateTrip(trip)
}

class BookingRepository(private val dao: AppDao) {
    fun getAllBookings(): Flow<List<Booking>> = dao.getAllBookings()
    suspend fun insertBooking(booking: Booking) = dao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = dao.updateBooking(booking)
}

class TransactionRepository(private val dao: AppDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()
    suspend fun insertTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun markAllAsPaid() = dao.markAllAsPaid()
}

class AnnouncementRepository(private val dao: AppDao) {
    fun getAllAnnouncements(): Flow<List<Announcement>> = dao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement) = dao.insertAnnouncement(announcement)
}

class AdminRepository(private val dao: AppDao) {
    fun getAllAuditLogs(): Flow<List<AuditLog>> = dao.getAllAuditLogs()
    suspend fun insertAuditLog(log: AuditLog) = dao.insertAuditLog(log)
    fun getAllPayouts(): Flow<List<Payout>> = dao.getAllPayouts()
    suspend fun insertPayout(payout: Payout) = dao.insertPayout(payout)
}
