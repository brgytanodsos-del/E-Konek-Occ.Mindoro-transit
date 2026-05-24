package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Booking
import com.example.data.Ship
import com.example.data.Trip
import com.example.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    onFinish: () -> Unit,
    isOnline: Boolean,
    viewModel: BookingViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var passengerName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Ferry") }
    var selectedEntityId by remember { mutableStateOf<String?>(null) }
    var selectedTicketType by remember { mutableStateOf("Regular") }
    var selectedSeats by remember { mutableIntStateOf(1) }
    var pickupPoint by remember { mutableStateOf("") }

    val ships by viewModel.ships.collectAsState()
    val trips by viewModel.trips.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create New Booking", style = MaterialTheme.typography.headlineSmall)
        Text("Step $currentStep of 3", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)

        Spacer(Modifier.height(24.dp))

        when (currentStep) {
            1 -> {
                Column {
                    OutlinedTextField(
                        value = passengerName,
                        onValueChange = { passengerName = it },
                        label = { Text("Passenger Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contactInfo,
                        onValueChange = { contactInfo = it },
                        label = { Text("Contact Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = passengerName.isNotBlank() && contactInfo.isNotBlank()
                    ) {
                        Text("Next")
                    }
                }
            }

            2 -> {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == "Ferry",
                            onClick = { selectedType = "Ferry"; selectedEntityId = null },
                            label = { Text("Ferry") }
                        )
                        FilterChip(
                            selected = selectedType == "Van",
                            onClick = { selectedType = "Van"; selectedEntityId = null },
                            label = { Text("Van / Bus") }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Select Schedule", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        if (selectedType == "Ferry") {
                            items(ships.filter { it.status == "Scheduled" || it.status == "Boarding" }) { ship ->
                                Card(
                                    onClick = { selectedEntityId = ship.id },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedEntityId == ship.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(ship.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(ship.route, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            items(trips.filter { it.status == "Scheduled" || it.status == "Boarding" }) { trip ->
                                Card(
                                    onClick = { selectedEntityId = trip.id },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedEntityId == trip.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("${trip.type}: ${trip.driver}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(trip.route, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 1 }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = { currentStep = 3 },
                            modifier = Modifier.weight(1f),
                            enabled = selectedEntityId != null
                        ) {
                            Text("Next")
                        }
                    }
                }
            }

            3 -> {
                Column {
                    if (selectedType == "Ferry") {
                        Text("Ticket Type")
                        val options = listOf("Regular", "Student", "Senior", "PWD")
                        options.forEach { option ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = selectedTicketType == option, onClick = { selectedTicketType = option })
                                Text(option)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = pickupPoint,
                            onValueChange = { pickupPoint = it },
                            label = { Text("Pickup Point") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Number of Seats")
                        Slider(
                            value = selectedSeats.toFloat(),
                            onValueChange = { selectedSeats = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 9
                        )
                        Text("$selectedSeats seats")
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 2 }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = {
                                if (selectedType == "Ferry") {
                                    val ship = ships.find { it.id == selectedEntityId }!!
                                    viewModel.bookFerry(ship, passengerName, contactInfo, selectedTicketType, isOnline)
                                } else {
                                    val trip = trips.find { it.id == selectedEntityId }!!
                                    viewModel.bookVanBus(trip, passengerName, contactInfo, pickupPoint, selectedSeats, isOnline)
                                }
                                onFinish()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Booking")
                        }
                    }
                }
            }
        }
    }
}
