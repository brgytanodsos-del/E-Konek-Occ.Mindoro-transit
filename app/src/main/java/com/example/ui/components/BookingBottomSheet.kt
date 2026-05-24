package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Ship
import com.example.viewmodel.AppViewModel
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingBottomSheet(
    ship: Ship,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var passengerName by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("GCash") }
    var isBooking by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Book Ticket - ${ship.name}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = passengerName,
                onValueChange = { passengerName = it },
                label = { Text("Passenger Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth()
            )

            // Payment Options
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("GCash", "Maya", "Cash").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    isBooking = true
                    scope.launch {
                        kotlinx.coroutines.delay(1500)
                        viewModel.bookFerry(ship, passengerName, contact, "Regular")
                        onDismiss()
                        isBooking = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBooking && passengerName.isNotBlank()
            ) {
                if (isBooking) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("CONFIRM BOOKING • ₱450")
                }
            }

            // Success Lottie (shown after booking)
            if (isBooking) {
                // Assuming R.raw.success_check exists
            }
        }
    }
}
