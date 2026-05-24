package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusBar(isOnline: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isOnline) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
        animationSpec = androidx.compose.animation.core.tween(500)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isOnline) "🟢 Online — Live Data" else "🔴 Offline — Cached Mode",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun WeatherWidget(
    weather: CurrentWeather?,
    title: String,
    isOnline: Boolean,
    onSpeak: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                if (onSpeak != null) {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (weather != null) {
                val (emoji, label) = getWeatherLabel(weather.weatherCode)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("${weather.temperature.toInt()}°", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Air, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("  ${weather.windSpeed}km/h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (weather.windSpeed > 30) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "⚠️ HIGH WINDS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                Text("Loading...", fontSize = 12.sp, color = Color.Gray)
            }
            if (!isOnline) {
                Text("📵 Cached", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MapView(
    center: List<Double>,
    markers: List<MapMarker>,
    modifier: Modifier = Modifier
) {
    val html = remember(center, markers) { getLeafletHtml(center, markers) }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

data class MapMarker(val id: String, val pos: List<Double>, val popupText: String)

private fun getLeafletHtml(center: List<Double>, markers: List<MapMarker>): String {
    val markersJs = markers.joinToString("\n") { m ->
        """L.circle([${m.pos[0]}, ${m.pos[1]}], {
            color: 'white',
            fillColor: '#FF6B00',
            fillOpacity: 1,
            radius: 100
        }).addTo(map).bindPopup("${m.popupText}");"""
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                #map { height: 100vh; width: 100vw; margin: 0; padding: 0; }
            </style>
        </head>
        <body style="margin:0;">
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([${center[0]}, ${center[1]}], 13);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                $markersJs
            </script>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun StatusDropdown(current: String, onStatusChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("Scheduled", "Boarding", "Departed", "Delayed", "Cancelled", "Completed")
    
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(current, fontSize = 10.sp) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
            shape = RoundedCornerShape(8.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            statuses.forEach { s ->
                DropdownMenuItem(text = { Text(s, fontSize = 12.sp) }, onClick = { onStatusChange(s); expanded = false })
            }
        }
    }
}

@Composable
fun BookingRow(
    booking: Booking, 
    onConfirm: () -> Unit, 
    onCancel: () -> Unit, 
    isConfirmed: Boolean = false,
    onIssueTicket: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isConfirmed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isConfirmed) Icons.Filled.VerifiedUser else Icons.Filled.Person, 
                    null, 
                    tint = if (isConfirmed) Color(0xFF16A34A) else Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(booking.contact, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(booking.ticketType ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (!isConfirmed) {
                Row {
                    IconButton(onClick = onConfirm, modifier = Modifier.size(32.dp).background(Color(0xFFDCFCE7), CircleShape)) { 
                        Icon(Icons.Filled.Check, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp)) 
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp).background(Color(0xFFFEE2E2), CircleShape)) { 
                        Icon(Icons.Filled.Close, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) 
                    }
                }
            } else {
                Button(
                    onClick = onIssueTicket,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.ConfirmationNumber, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ticket", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun TicketDialog(booking: Booking, ship: Ship?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text("Print Ticket") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Boarding Pass", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ship?.name ?: "Vessel/Transit", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(ship?.route ?: "Transport Route", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 2.dp, modifier = Modifier.padding(horizontal = 10.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("PASSENGER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(booking.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TYPE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(booking.ticketType ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DEPARTURE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(formatPST(ship?.departureTime ?: ""), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                loadUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${booking.referenceId}")
                            }
                        },
                        modifier = Modifier.size(150.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(booking.referenceId, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
                }
            }
        }
    )
}
@Composable
fun AddVoyageDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("Abra Port → Batangas") }
    var dep by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("RORO") }
    var cap by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Voyage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ship Name") })
                OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Route") })
                OutlinedTextField(value = dep, onValueChange = { dep = it }, label = { Text("Dep Time (ISO)") }, placeholder = { Text("2026-05-24T20:00:00Z") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (RORO/Ferry)") })
                OutlinedTextField(value = cap, onValueChange = { cap = it }, label = { Text("Capacity") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, route, dep, type, cap.toIntOrNull() ?: 200) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTripDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, Int) -> Unit) {
    var route by remember { mutableStateOf("Mamburao → Abra Port") }
    var dep by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Van") }
    var driver by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("14") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Route") })
                OutlinedTextField(value = dep, onValueChange = { dep = it }, label = { Text("Dep Time (ISO)") }, placeholder = { Text("2026-05-24T20:00:00Z") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (Van/Bus)") })
                OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Driver Name") })
                OutlinedTextField(value = cap, onValueChange = { cap = it }, label = { Text("Capacity") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(route, dep, type, driver, cap.toIntOrNull() ?: 14) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddAnnouncementDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Announcement") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onAdd(text) }) { Text("Post") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScannerDialog(onDismiss: () -> Unit, onScan: (String) -> Unit) {
    var ref by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, null, tint = Navy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Boarding Scanner")
            }
        },
        text = {
            Column {
                Text("Align QR code within frame or enter Reference ID manually.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Mock Scan View
                    Box(modifier = Modifier.size(100.dp).border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)))
                    Text("📷 Camera Active", color = Color.White, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("Reference Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onScan(ref) }, enabled = ref.isNotEmpty()) { Text("Validate") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun Toast(message: String?) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
        label = "ToastBox"
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Surface(
                color = GreenSync,
                shape = CircleShape,
                tonalElevation = 8.dp
            ) {
                Text(
                    text = message ?: "",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
