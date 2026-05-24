package com.example.data

import androidx.room.*
import com.example.data.model.Ship
import com.example.data.model.Trip
import com.example.data.model.Booking
import com.example.data.model.Transaction
import com.example.data.model.Announcement
import com.example.data.model.AuditLog
import com.example.data.model.Payout
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM ships")
    fun getAllShips(): Flow<List<Ship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShips(ships: List<Ship>)

    @Update
    suspend fun updateShip(ship: Ship)

    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<Trip>)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: String): Booking?

    @Query("SELECT * FROM ships WHERE id = :id")
    suspend fun getShipById(id: String): Ship?

    @Query("SELECT * FROM transactions WHERE bookingId = :bookingId")
    suspend fun getTransactionByBookingId(bookingId: String): Transaction?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT * FROM payout_history ORDER BY date DESC")
    fun getAllPayouts(): Flow<List<Payout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: Payout)

    @Query("UPDATE transactions SET paid = 1 WHERE status = 'Completed'")
    suspend fun markAllAsPaid()
}

@Database(
    entities = [
        Ship::class, Trip::class, Booking::class, Transaction::class,
        Announcement::class, AuditLog::class, Payout::class, User::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindoro_transit_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
