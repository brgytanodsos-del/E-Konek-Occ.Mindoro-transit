package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    val abraWeather by viewModel.abraWeather.collectAsState()
    val mamburaoWeather by viewModel.mamburaoWeather.collectAsState()
    val announcement by viewModel.announcements.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    
    var showBookingForm by remember { mutableStateOf<String?>(null) } // "Ferry" or "Van"
    var selectedEntityId by remember { mutableStateOf<String?>(null) }
    
    var countdown by remember { mutableStateOf(30) }
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
                    Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.error).padding(8.dp)) {
                        Text("⚠️ WIND ADVISORY: High winds at Abra Port.", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Decorative circle as in design
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .offset(x = 100.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Mamburao Central Terminal",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).horizontalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.width(200.dp)) {
                        WeatherWidget(abraWeather, "Abra Port", isOnline)
                    }
                    Box(modifier = Modifier.width(200.dp)) {
                        WeatherWidget(mamburaoWeather, "Mamburao", isOnline)
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
                        }
                    } else {
                        val trip = trips.find { it.id == selectedEntityId }
                        if (trip != null) VanBookingFields(trip) { name, contact, pickup, seats ->
                            viewModel.bookVanBus(trip, name, contact, pickup, seats)
                            showBookingForm = null
                        }
                    }
                },
                confirmButton = {}
            )
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
