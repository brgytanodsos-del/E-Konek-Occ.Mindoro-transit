package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.Navy
import com.example.ui.theme.Orange
import com.example.viewmodel.AppViewModel

import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalStaffPanel(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel = viewModel(),
    isSuperAdmin: Boolean = false
) {
    val trips by appViewModel.trips.collectAsState()
    val bookings by appViewModel.bookings.collectAsState()
    val mamburaoWeather by appViewModel.mamburaoWeather.collectAsState()
    val isOnline by appViewModel.isOnline.collectAsState()
    val gpsIndices by appViewModel.gpsIndices.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showScanner by remember { mutableStateOf(false) }
    val terminalBookings = bookings.filter { it.type == "Van" || it.type == "Bus" }

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
                                "Terminal View",
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
                        IconButton(onClick = { showScanner = true }, modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                            IconButton(onClick = { authViewModel.logout() }) {
                                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Trips", fontSize = 11.sp, fontWeight = if(selectedTab==0) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Bookings", fontSize = 11.sp, fontWeight = if(selectedTab==1) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Map", fontSize = 11.sp, fontWeight = if(selectedTab==2) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Ships", fontSize = 11.sp, fontWeight = if(selectedTab==3) FontWeight.Bold else FontWeight.Normal) })
            }
            
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1.3f)) {
                                        WeatherWidget(mamburaoWeather, "Mamburao Term.", isOnline, onSpeak = {
                                        mamburaoWeather?.let { 
                                            val (_, label) = getWeatherLabel(it.weatherCode)
                                            appViewModel.speak("Ang panahon sa Mamburao Terminal ay $label. Ang temperatura ay ${it.temperature.toInt()} degrees Celsius.")
                                        }
                                    })
                                    }
                                    Card(
                                        modifier = Modifier.weight(1f).height(110.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                                            Text("Active Trips", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${trips.size}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                                Text("ACTIVE TRIPS", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            }

                            items(trips) { trip ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    TripCard(trip, onStatusChange = { appViewModel.updateTripStatus(trip, it) })
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            item {
                Text("BOOKINGS", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            
            val pending = terminalBookings.filter { it.status == "Pending" }
            val confirmed = terminalBookings.filter { it.status == "Confirmed" }

            if (pending.isEmpty() && confirmed.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No bookings found", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            if (pending.isNotEmpty()) {
                item { Text("PENDING", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp), color = Orange) }
                items(pending) { booking ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BookingRow(booking, onConfirm = { appViewModel.confirmBooking(booking, "Terminal Admin") }, onCancel = { appViewModel.cancelBooking(booking) })
                    }
                }
            }

            if (confirmed.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Text("CONFIRMED", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp), color = Color(0xFF16A34A)) }
                items(confirmed) { booking ->
                    var showTicket by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BookingRow(
                            booking, 
                            onConfirm = {}, 
                            onCancel = { appViewModel.cancelBooking(booking) }, 
                            isConfirmed = true,
                            onIssueTicket = { showTicket = true }
                        )
                    }
                    if (showTicket) {
                        TicketDialog(booking, null) { showTicket = false }
                    }
                }
            }
                        }
                    }
                    2 -> {
                        val activeTrips = trips.filter { it.status == "Boarding" || it.status == "Departed" }
                        val markers = activeTrips.map { trip ->
                            val route = AppConstants.GPS_ROUTES[trip.route] ?: AppConstants.GPS_ROUTES["default"]!!
                            val index = gpsIndices[trip.id] ?: 0
                            MapMarker(trip.id, route[index % route.size], "${trip.driver} (${trip.type}) - ${trip.route}")
                        }
                        MapView(center = listOf(13.2167, 120.5833), markers = markers, modifier = Modifier.fillMaxSize())
                    }
                    3 -> {
                        val ships by appViewModel.ships.collectAsState()
                        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("FERRY SYNC MONITOR", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LIVE", color = Color(0xFF16A34A), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(ships) { ship ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DirectionsBoat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(ship.name, fontWeight = FontWeight.Bold)
                                                Text(ship.route, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Surface(
                                                color = when(ship.status) {
                                                    "Boarding" -> Color(0xFFDCFCE7)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    ship.status.uppercase(),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = when(ship.status) {
                                                        "Boarding" -> Color(0xFF16A34A)
                                                        else -> MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showScanner) {
            ScannerDialog(onDismiss = { showScanner = false }) { ref ->
                val b = bookings.find { it.referenceId == ref }
                if (b != null) {
                    appViewModel.showToast("✅ Valid: ${b.name} (${b.type})")
                    if (b.status == "Pending") {
                        appViewModel.confirmBooking(b, "Terminal Admin")
                    }
                } else {
                    appViewModel.showToast("❌ Invalid Reference ID")
                }
                showScanner = false
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, onStatusChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (trip.type == "Van") Icons.Default.AirportShuttle else Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(trip.route, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text(trip.driver, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusDropdown(trip.status, onStatusChange)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = " Dep: ${formatPST(trip.depTime)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (trip.available > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${trip.available}/${trip.capacity} SEATS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (trip.available > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
