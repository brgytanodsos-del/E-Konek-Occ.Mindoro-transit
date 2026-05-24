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
import com.example.data.*
import com.example.ui.theme.Navy
import com.example.ui.theme.Orange
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortStaffPanel(viewModel: AppViewModel, isSuperAdmin: Boolean = false) {
    val ships by viewModel.ships.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val abraWeather by viewModel.abraWeather.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    
    var showScanner by remember { mutableStateOf(false) }
    val ferryBookings = bookings.filter { it.type == "Ferry" }

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
                                "Staff View",
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
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1.3f)) {
                        WeatherWidget(abraWeather, "Abra Port", isOnline, onSpeak = {
                            abraWeather?.let {
                                val (_, label) = getWeatherLabel(it.weatherCode)
                                viewModel.speak("Ang panahon sa Abra Port ay $label. Ang temperatura ay ${it.temperature.toInt()} degrees Celsius.")
                            }
                        })
                    }
                    Card(
                        modifier = Modifier.weight(1f).height(110.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                            Text("Tickets Sold", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${ferryBookings.size}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        }
                    }
                }
                Text("VESSEL SCHEDULE", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            
            items(ships) { ship ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    VesselCard(ship, onStatusChange = { viewModel.updateShipStatus(ship, it) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("RESERVATIONS", fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            
            val pending = ferryBookings.filter { it.status == "Pending" }
            val confirmed = ferryBookings.filter { it.status == "Confirmed" }

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
                        BookingRow(booking, onConfirm = { viewModel.confirmBooking(booking, "Port Admin") }, onCancel = { viewModel.cancelBooking(booking) })
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
                            onCancel = { viewModel.cancelBooking(booking) }, 
                            isConfirmed = true,
                            onIssueTicket = { showTicket = true }
                        )
                    }
                    if (showTicket) {
                        TicketDialog(booking, ships.find { it.id == booking.entityId }) { showTicket = false }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        if (showScanner) {
            ScannerDialog(onDismiss = { showScanner = false }) { ref ->
                val b = bookings.find { it.referenceId == ref }
                if (b != null) {
                    viewModel.showToast("✅ Valid: ${b.name} (${b.type})")
                    if (b.status == "Pending") {
                        viewModel.confirmBooking(b, "Port Admin")
                    }
                } else {
                    viewModel.showToast("❌ Invalid Reference ID")
                }
                showScanner = false
            }
        }
    }
}

@Composable
fun VesselCard(ship: Ship, onStatusChange: (String) -> Unit) {
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
                    Icon(Icons.Default.DirectionsBoat, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ship.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text(ship.route, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusDropdown(ship.status, onStatusChange)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = " Dep: ${formatPST(ship.depTime)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${ship.available}/${ship.capacity} SLOTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}


