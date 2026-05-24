package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerPanel(viewModel: AppViewModel, isSuperAdmin: Boolean = false) {
    val ships by viewModel.ships.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val abraWeather by viewModel.abraWeather.collectAsState()
    val mamburaoWeather by viewModel.mamburaoWeather.collectAsState()
    val announcement by viewModel.announcements.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    
    var showBookingForm by remember { mutableStateOf<String?>(null) } // "Ferry" or "Van"
    var selectedEntityId by remember { mutableStateOf<String?>(null) }
    
    var countdown by remember { mutableStateOf(30) }
    var trackingTripId by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf<Booking?>(null) }
    var showMyBookings by remember { mutableStateOf(false) }

    val gpsIndices by viewModel.gpsIndices.collectAsState()

    val pulseScale by animateFloatAsState(
        targetValue = if (countdown <= 3) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (trackingTripId != null) {
        val trip = trips.find { it.id == trackingTripId }
        val route = trip?.route ?: "default"
        val location = viewModel.getTripLocation(trackingTripId!!, route)
        TrackRideScreen(trip, location) { trackingTripId = null }
        return
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            countdown--
            if (countdown == 0) countdown = 30
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            "E-KONEK TRANSIT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Occi.Min",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (isSuperAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "ADMIN",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("JD", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { showMyBookings = !showMyBookings }, modifier = Modifier.size(40.dp).background(if (showMyBookings) Navy else Color.Transparent, CircleShape)) {
                            Icon(Icons.Default.ConfirmationNumber, null, tint = if (showMyBookings) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        if (!isSuperAdmin) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            item {
                if (abraWeather?.windSpeed ?: 0.0 > 30.0) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Red).padding(8.dp)) {
                        Text("⚠️ WIND ADVISORY: High winds at Abra Port. Schedules may be delayed.", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Navy)
                ) {
                    // Decorative patterns
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = 150.dp, y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("MindoroTransit", color = Orange, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                        Text("Live Travel\nCompanion", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, lineHeight = 36.sp, letterSpacing = (-1).sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Montenegro Shipping & Mamburao Terminal",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).horizontalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.width(200.dp)) {
                        WeatherWidget(abraWeather, "Abra Port", isOnline, onSpeak = {
                            abraWeather?.let {
                                val (_, label) = getWeatherLabel(it.weatherCode)
                                viewModel.speak("Weather update for Abra Port: it's $label with a temperature of ${it.temperature.toInt()} degrees Celsius.")
                            }
                        })
                    }
                    Box(modifier = Modifier.width(200.dp)) {
                        WeatherWidget(mamburaoWeather, "Mamburao", isOnline, onSpeak = {
                            mamburaoWeather?.let {
                                val (_, label) = getWeatherLabel(it.weatherCode)
                                viewModel.speak("Weather update for Mamburao: it's $label with a temperature of ${it.temperature.toInt()} degrees Celsius.")
                            }
                        })
                    }
                }
                
                announcement.firstOrNull()?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(it.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (showMyBookings) {
                    Text("MY ACTIVE BOOKINGS", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    val myBookings = bookings.filter { it.status != "Cancelled" }
                    if (myBookings.isEmpty()) {
                        Text("No active bookings found.", fontSize = 12.sp, modifier = Modifier.padding(20.dp), color = Color.Gray)
                    }
                    myBookings.forEach { b ->
                        BookingCard(b) {
                            if (b.type != "Ferry") {
                                trackingTripId = b.entityId
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AVAILABLE ROUTES", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                    Text("Live Schedules", fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.5).sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Refreshing in ${countdown}s", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Text("🚢 FERRY ROUTES", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            
            items(ships.filter { it.status != "Cancelled" && it.status != "Departed" }) { ship ->
                ScheduleRow(ship.name, ship.route, ship.status, ship.available, ship.capacity) {
                    selectedEntityId = ship.id
                    showBookingForm = "Ferry"
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("🚐 LAND TRANSIT", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            
            items(trips.filter { it.status != "Cancelled" && it.status != "Completed" }) { trip ->
                ScheduleRow(trip.driver, trip.route, trip.status, trip.available, trip.capacity) {
                    selectedEntityId = trip.id
                    showBookingForm = "Van"
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
        
        if (showBookingForm != null) {
            AlertDialog(
                onDismissRequest = { showBookingForm = null },
                title = { Text("Book ${showBookingForm}") },
                text = {
                    if (showBookingForm == "Ferry") {
                        val ship = ships.find { it.id == selectedEntityId }
                        if (ship != null) FerryBookingFields(ship) { name, contact, type ->
                            viewModel.bookFerry(ship, name, contact, type)
                            showBookingForm = null
                            // We can't easily get the last inserted booking, so mock a confirmation for UI
                            showConfirmation = Booking(generateId(), "REF-${generateId().uppercase()}", ship.id, "Ferry", name, contact, type, status = "Pending", timestamp = "")
                        }
                    } else {
                        val trip = trips.find { it.id == selectedEntityId }
                        if (trip != null) VanBookingFields(trip) { name, contact, pickup, seats ->
                            viewModel.bookVanBus(trip, name, contact, pickup, seats)
                            showBookingForm = null
                            showConfirmation = Booking(generateId(), "REF-${generateId().uppercase()}", trip.id, trip.type, name, contact, "$seats seats", status = "Pending", timestamp = "")
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showConfirmation = null },
                confirmButton = {
                    Button(onClick = { showConfirmation = null }) { Text("Close") }
                },
                title = { Text("Booking Submitted!") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenSync, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reference Number", fontSize = 12.sp, color = Color.Gray)
                        Text(showConfirmation?.referenceId ?: "", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        if (showConfirmation?.type != "Ferry") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { 
                                trackingTripId = showConfirmation?.entityId
                                showConfirmation = null
                            }) {
                                Icon(Icons.Default.Map, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Track Ride")
                            }
                        }
                    }
                }
            )
        }

        // Floating Offline Sync Badge
        val unsyncedCount = bookings.filter { it.isSyncing }.size
        if (unsyncedCount > 0 && !isOnline) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(bottom = if (isSuperAdmin) 60.dp else 0.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📥 $unsyncedCount bookings pending sync", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackRideScreen(trip: Trip?, location: List<Double>, onBack: () -> Unit) {
    var etaSeconds by remember { mutableStateOf(15 * 60) }
    LaunchedEffect(Unit) {
        while (etaSeconds > 0) {
            delay(3000)
            etaSeconds -= 5
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapView(center = location, markers = listOf(MapMarker(trip?.id ?: "current", location, "Your Ride: ${trip?.driver}")), modifier = Modifier.fillMaxSize())
        
        Column(modifier = Modifier.align(Alignment.TopStart).padding(20.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trip?.route ?: "Route", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(trip?.driver ?: "Driver", fontSize = 14.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ETA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        val mins = etaSeconds / 60
                        val secs = etaSeconds % 60
                        Text("%02d:%02dm".format(mins, secs), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(b: Booking, onTrack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (b.type == "Ferry") Icons.Default.DirectionsBoat else Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(b.referenceId, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("${b.ticketType} • ${b.type}", fontSize = 11.sp, color = Color.Gray)
                }
                Surface(color = if (b.status == "Confirmed") SuccessContainerSleek else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Text(b.status.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (b.status == "Confirmed") OnSuccessContainerSleek else MaterialTheme.colorScheme.primary)
                }
            }
            if (b.status == "Confirmed" && b.type != "Ferry") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onTrack, modifier = Modifier.fillMaxWidth().height(36.dp), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Track My Ride", fontSize = 11.sp)
                }
            } else if (b.status == "Confirmed") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Image(
                        painter = coil.compose.rememberAsyncImagePainter("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${b.referenceId}"),
                        contentDescription = "QR",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleRow(name: String, route: String, status: String, avail: Int, cap: Int, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(enabled = avail > 0) { onBook() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (status == "Boarding") Icons.Default.DirectionsBoat else Icons.Default.Schedule,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(route, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                val badgeColor = when (status) {
                    "Boarding" -> SuccessContainerSleek
                    "Delayed", "Cancelled" -> ErrorContainerSleek
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = when (status) {
                    "Boarding" -> OnSuccessContainerSleek
                    "Delayed", "Cancelled" -> OnErrorContainerSleek
                    else -> MaterialTheme.colorScheme.primary
                }
                Surface(color = badgeColor, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        status.uppercase(),
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (avail == 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = ErrorContainerSleek, shape = RoundedCornerShape(6.dp)) {
                        Text("FULL", color = OnErrorContainerSleek, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Text("₱${if (name.contains("MV")) "250" else "480"}.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
fun FerryBookingFields(ship: Ship, onComplete: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Regular") }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ship: ${ship.name}")
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") })
        Text("Ticket Type:")
        Row {
            listOf("Regular", "Student", "Senior", "PWD").forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Button(onClick = { onComplete(name, contact, type) }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotEmpty() && contact.isNotEmpty()) {
            Text("Book Now")
        }
    }
}

@Composable
fun VanBookingFields(trip: Trip, onComplete: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pickup by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf(1) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Trip: ${trip.route}")
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") })
        OutlinedTextField(value = pickup, onValueChange = { pickup = it }, label = { Text("Pickup Point") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Seats:")
            IconButton(onClick = { if (seats > 1) seats-- }) { Icon(Icons.Default.Remove, null) }
            Text("$seats")
            IconButton(onClick = { if (seats < trip.available) seats++ }) { Icon(Icons.Default.Add, null) }
        }
        Button(onClick = { onComplete(name, contact, pickup, seats) }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotEmpty() && contact.isNotEmpty()) {
            Text("Book Now")
        }
    }
}
