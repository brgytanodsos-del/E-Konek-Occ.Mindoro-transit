package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import com.example.data.repository.*
import com.example.utils.VoiceAssistant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.dao()
    
    private val shipRepository = ShipRepository(dao)
    private val tripRepository = TripRepository(dao)
    private val bookingRepository = BookingRepository(dao)
    private val transactionRepository = TransactionRepository(dao)
    private val announcementRepository = AnnouncementRepository(dao)
    private val adminRepository = AdminRepository(dao)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val weatherService = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(WeatherService::class.java)

    // Global State
    val ships = shipRepository.getAllShips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trips = tripRepository.getAllTrips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookings = bookingRepository.getAllBookings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = transactionRepository.getAllTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val announcements = announcementRepository.getAllAnnouncements().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = adminRepository.getAllAuditLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val payouts = adminRepository.getAllPayouts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _abraWeather = MutableStateFlow<CurrentWeather?>(null)
    val abraWeather = _abraWeather.asStateFlow()

    private val _mamburaoWeather = MutableStateFlow<CurrentWeather?>(null)
    val mamburaoWeather = _mamburaoWeather.asStateFlow()

    private val _gpsIndices = MutableStateFlow<Map<String, Int>>(emptyMap())
    val gpsIndices = _gpsIndices.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val voiceAssistant = VoiceAssistant(application)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        setupConnectivityObserver(application)
        seedData()
        startGpsUpdates()
        startWeatherUpdates()
    }

    fun speak(text: String) {
        voiceAssistant.speak(text)
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.stop()
        networkCallback?.let {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }
    }

    private fun setupConnectivityObserver(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        _isOnline.value = isInitiallyOnline(connectivityManager)

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                viewModelScope.launch {
                    processOfflineQueue()
                    fetchWeather()
                    showToast("✅ Bookings synced successfully")
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        }
        
        networkCallback?.let {
            connectivityManager.registerNetworkCallback(networkRequest, it)
        }
    }

    private fun isInitiallyOnline(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun seedData() {
        viewModelScope.launch {
            try {
                // Wait a bit for Room to initialize
                delay(500) 
                
                val currentShips = dao.getAllShips().first()
                if (currentShips.isEmpty()) {
                    val now = ZonedDateTime.now()
                    val seedShips = listOf(
                        Ship("s1", "MV Maria Olive", "Abra Port → Batangas", now.plusHours(2).toInstant().toString(), now.plusHours(4).plusMinutes(30).toInstant().toString(), "Boarding", 300, 120, "RORO"),
                        Ship("s2", "MV Reina Genoveva", "Abra Port → Puerto Galera", now.plusHours(5).toInstant().toString(), now.plusHours(6).plusMinutes(30).toInstant().toString(), "Scheduled", 250, 250, "Passenger Ferry"),
                        Ship("s3", "MV Montenegro Star", "Batangas → Abra Port", now.plusHours(8).toInstant().toString(), now.plusHours(10).plusMinutes(30).toInstant().toString(), "Scheduled", 200, 200, "RORO")
                    )
                    dao.insertShips(seedShips)
                }

                val currentTrips = dao.getAllTrips().first()
                if (currentTrips.isEmpty()) {
                    val now = ZonedDateTime.now()
                    val seedTrips = listOf(
                        Trip("t1", "Mamburao → Abra Port", now.plusMinutes(30).toInstant().toString(), "Van", "Kuya Jun Dela Rosa", 14, 6, "Boarding"),
                        Trip("t2", "Abra Port → Mamburao", now.plusHours(1).toInstant().toString(), "Van", "Ate Lorna Bautista", 14, 14, "Scheduled"),
                        Trip("t3", "Mamburao → San Jose", now.plusHours(2).toInstant().toString(), "Bus", "Mang Cardo Villanueva", 45, 30, "Scheduled"),
                        Trip("t4", "San Jose → Mamburao", now.plusHours(3).toInstant().toString(), "Bus", "Dodong Reyes", 45, 45, "Scheduled"),
                        Trip("t5", "Mamburao → Calintaan", now.plusMinutes(45).toInstant().toString(), "Van", "Kuya Romy Santos", 10, 3, "Departed"),
                        Trip("t6", "Calintaan → Mamburao", now.plusHours(4).toInstant().toString(), "Van", "Nanding Cruz", 10, 10, "Scheduled")
                    )
                    dao.insertTrips(seedTrips)
                }

                val currentAnnouncements = dao.getAllAnnouncements().first()
                if (currentAnnouncements.isEmpty()) {
                    dao.insertAnnouncement(Announcement(generateId(), "All trips are on schedule. Please arrive 30 mins early.", ZonedDateTime.now().toInstant().toString(), "System"))
                }
            } catch (e: Exception) {
                // Silently fail seed if already seeded or error
            }
        }
    }

    private fun startGpsUpdates() {
        viewModelScope.launch {
            while (isActive) {
                val currentTrips = trips.value
                val nextIndices = _gpsIndices.value.toMutableMap()
                currentTrips.forEach { trip ->
                    if (trip.status == "Boarding" || trip.status == "Departed") {
                        val route = AppConstants.GPS_ROUTES[trip.route] ?: AppConstants.GPS_ROUTES["default"]!!
                        val currentIndex = nextIndices[trip.id] ?: 0
                        nextIndices[trip.id] = (currentIndex + 1) % route.size
                    }
                }
                _gpsIndices.value = nextIndices
                delay(3000)
            }
        }
    }

    private fun startWeatherUpdates() {
        viewModelScope.launch {
            while (isActive) {
                fetchWeather()
                delay(5 * 60 * 1000) // 5 minutes
            }
        }
    }

    private suspend fun fetchWeather() {
        if (!_isOnline.value) return
        try {
            val abra = weatherService.getWeather(13.45, 120.63)
            val mamburao = weatherService.getWeather(13.2167, 120.5833)
            _abraWeather.value = abra.current
            _mamburaoWeather.value = mamburao.current
        } catch (e: Exception) {
            // Last cached values remain
        }
    }

    private suspend fun processOfflineQueue() {
        val pendingBookings = bookings.value.filter { it.isSyncing }
        pendingBookings.forEach { booking ->
            dao.updateBooking(booking.copy(isSyncing = false))
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(3000)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    fun bookFerry(ship: Ship, name: String, contact: String, ticketType: String) {
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
            isSyncing = !_isOnline.value
        )
        viewModelScope.launch {
            bookingRepository.insertBooking(booking)
            showToast("Booking Submitted!")
        }
    }

    fun bookVanBus(trip: Trip, name: String, contact: String, pickup: String, seats: Int) {
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
            isSyncing = !_isOnline.value
        )
        viewModelScope.launch {
            bookingRepository.insertBooking(booking)
            showToast("Booking Submitted!")
        }
    }

    fun confirmBooking(booking: Booking, confirmedBy: String) {
        viewModelScope.launch {
            val updatedBooking = booking.copy(status = "Confirmed")
            dao.updateBooking(updatedBooking)
            
            val gross = getGrossAmount(booking.type, booking.ticketType, booking.seats)
            val commission = getCommission(booking.type, booking.ticketType, booking.seats)
            
            val route = when (booking.type) {
                "Ferry" -> ships.value.find { it.id == booking.entityId }?.route ?: "Unknown"
                else -> trips.value.find { it.id == booking.entityId }?.route ?: "Unknown"
            }

            val tx = Transaction(
                id = "TX-${generateId().uppercase()}",
                timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                type = booking.type,
                bookingId = booking.id,
                passengerName = booking.name,
                route = route,
                ticketType = booking.ticketType,
                grossAmount = gross,
                commissionAmount = commission,
                confirmedBy = confirmedBy,
                status = "Completed",
                paid = false
            )
            transactionRepository.insertTransaction(tx)
        }
    }

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            bookingRepository.updateBooking(booking.copy(status = "Cancelled"))
        }
    }

    fun refundTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction.copy(status = "Refunded"))
        }
    }

    fun markAllAsPaid() {
        viewModelScope.launch {
            val completed = transactions.value.filter { it.status == "Completed" && !it.paid }
            if (completed.isNotEmpty()) {
                val totalAmount = completed.sumOf { it.commissionAmount }
                val count = completed.size
                adminRepository.insertPayout(Payout(
                    id = generateId(),
                    date = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                    totalAmount = totalAmount,
                    transactionCount = count
                    ))
                transactionRepository.markAllAsPaid()
                showToast("Payout recorded and cleared.")
            }
        }
    }

    fun addShip(ship: Ship) { viewModelScope.launch { shipRepository.insertShips(listOf(ship)) } }
    fun updateShipStatus(ship: Ship, status: String) { viewModelScope.launch { shipRepository.updateShip(ship.copy(status = status)) } }
    
    fun addTrip(trip: Trip) { viewModelScope.launch { tripRepository.insertTrips(listOf(trip)) } }
    fun updateTripStatus(trip: Trip, status: String) { viewModelScope.launch { tripRepository.updateTrip(trip.copy(status = status)) } }

    fun addAnnouncement(text: String, author: String) {
        viewModelScope.launch {
            announcementRepository.insertAnnouncement(Announcement(
                id = generateId(),
                text = text,
                date = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                author = author
            ))
        }
    }

    fun getTripLocation(tripId: String, routeStr: String): List<Double> {
        val route = AppConstants.GPS_ROUTES[routeStr] ?: AppConstants.GPS_ROUTES["default"]!!
        val index = gpsIndices.value[tripId] ?: 0
        return route[index % route.size]
    }
}
