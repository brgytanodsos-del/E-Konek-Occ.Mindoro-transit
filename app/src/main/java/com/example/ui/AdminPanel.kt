package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminPanel(viewModel: AppViewModel) {
    val txs by viewModel.transactions.collectAsState()
    val payouts by viewModel.payouts.collectAsState()
    val logs by viewModel.auditLogs.collectAsState()
    
    val totalCommissions = txs.filter { it.status == "Completed" }.sumOf { it.commissionAmount }
    val totalGross = txs.filter { it.status == "Completed" }.sumOf { it.grossAmount }
    val pendingPayout = txs.filter { it.status == "Completed" && !it.paid }.sumOf { it.commissionAmount }
    
    val ferryComm = txs.filter { it.status == "Completed" && it.type == "Ferry" }.sumOf { it.commissionAmount }
    val vanComm = txs.filter { it.status == "Completed" && it.type == "Van" }.sumOf { it.commissionAmount }
    val busComm = txs.filter { it.status == "Completed" && it.type == "Bus" }.sumOf { it.commissionAmount }

    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }
    var filterPeriod by remember { mutableStateOf("All") }
    var filterStatus by remember { mutableStateOf("All") }

    var showAddVoyage by remember { mutableStateOf(false) }
    var showAddTrip by remember { mutableStateOf(false) }
    var showAddAnnouncement by remember { mutableStateOf(false) }

    val filteredTxs = txs.filter { tx ->
        val matchesSearch = tx.passengerName.contains(searchQuery, ignoreCase = true) || tx.id.contains(searchQuery, ignoreCase = true)
        val matchesType = filterType == "All" || tx.type == filterType
        val matchesStatus = filterStatus == "All" || tx.status == filterStatus
        // Period filter could be implemented based on tx.timestamp
        matchesSearch && matchesType && matchesStatus
    }.sortedByDescending { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Panel", fontWeight = FontWeight.Black) },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(4.dp)).background(Orange).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("ADMIN MODE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    var showLogoutConfirm by remember { mutableStateOf(false) }
                    if (showLogoutConfirm) {
                        TextButton(onClick = { viewModel.logout() }) { Text("Confirm Logout", color = Color.Red, fontSize = 12.sp) }
                        IconButton(onClick = { showLogoutConfirm = false }) { Icon(Icons.Default.Close, contentDescription = null) }
                    } else {
                        IconButton(onClick = { showLogoutConfirm = true }) { Icon(Icons.Default.Logout, contentDescription = "Logout") }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Earnings", "₱${"%.0f".format(totalCommissions)}", Icons.Default.Payments, MaterialTheme.colorScheme.primary)
                    StatCard("Revenue", "₱${"%.0f".format(totalGross)}", Icons.Default.TrendingUp, GreenSync)
                    StatCard("Pending", "₱${"%.0f".format(pendingPayout)}", Icons.Default.Schedule, Orange)
                    StatCard("Volume", "${txs.size}", Icons.Default.Analytics, Navy)
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("SYSTEM MANAGEMENT", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showAddVoyage = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            Icon(Icons.Default.DirectionsBoat, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Ship", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showAddTrip = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) {
                            Icon(Icons.Default.DirectionsBus, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Trip", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showAddAnnouncement = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Announcement, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Announce", fontSize = 12.sp)
                        }
                    }
                }
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("COMMISSION BREAKDOWN", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    SleekBarChart(ferryComm, vanComm, busComm)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("FILTERS & SEARCH", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search passenger or ref ID", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        FilterChip(selected = filterType == "All", onClick = { filterType = "All" }, label = { Text("All Types") })
                        FilterChip(selected = filterType == "Ferry", onClick = { filterType = "Ferry" }, label = { Text("Ferry") })
                        FilterChip(selected = filterType == "Van", onClick = { filterType = "Van" }, label = { Text("Van") })
                        FilterChip(selected = filterType == "Bus", onClick = { filterType = "Bus" }, label = { Text("Bus") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        FilterChip(selected = filterStatus == "All", onClick = { filterStatus = "All" }, label = { Text("All Status") })
                        FilterChip(selected = filterStatus == "Completed", onClick = { filterStatus = "Completed" }, label = { Text("Completed") })
                        FilterChip(selected = filterStatus == "Refunded", onClick = { filterStatus = "Refunded" }, label = { Text("Refunded") })
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("TRANSACTION LOG", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            val csv = "Timestamp,Ref,Passenger,Route,Type,Gross,Commission,Status\n" + 
                                filteredTxs.joinToString("\n") { "${it.timestamp},${it.id},${it.passengerName},${it.route},${it.type},${it.grossAmount},${it.commissionAmount},${it.status}" }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csv)
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export CSV"))
                        }) {
                            Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { viewModel.markAllAsPaid() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(filteredTxs) { tx ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TransactionRow(tx, onRefund = { viewModel.refundTransaction(tx) })
                }
            }

            item {
                if (filteredTxs.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Filtered Total:", fontWeight = FontWeight.Bold)
                            Text("₱${"%.2f".format(filteredTxs.sumOf { it.commissionAmount })}", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PayoutHistorySection(payouts)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    AuditLogSection(logs)
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        if (showAddVoyage) {
            AddVoyageDialog(
                onDismiss = { showAddVoyage = false },
                onAdd = { name, route, dep, type, cap ->
                    viewModel.addShip(Ship(generateId(), name, route, dep, dep, "Scheduled", cap, cap, type))
                    showAddVoyage = false
                }
            )
        }

        if (showAddTrip) {
            AddTripDialog(
                onDismiss = { showAddTrip = false },
                onAdd = { route, dep, type, driver, cap ->
                    viewModel.addTrip(Trip(generateId(), route, dep, type, driver, cap, cap, "Scheduled"))
                    showAddTrip = false
                }
            )
        }

        if (showAddAnnouncement) {
            AddAnnouncementDialog(
                onDismiss = { showAddAnnouncement = false },
                onAdd = { text ->
                    viewModel.addAnnouncement(text, "Super Admin")
                    showAddAnnouncement = false
                }
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(150.dp).height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(12.dp), tint = color)
                Spacer(modifier = Modifier.width(4.dp))
                Text(title.uppercase(), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun SleekBarChart(ferry: Double, van: Double, bus: Double) {
    val maxComm = maxOf(ferry, van, bus).coerceAtLeast(1.0)
    
    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        VerticalBar("Ferry", ferry, ferry / maxComm, Navy)
        VerticalBar("Van", van, van / maxComm, Orange)
        VerticalBar("Bus", bus, bus / maxComm, MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun VerticalBar(label: String, value: Double, heightFactor: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
        Text("₱${"%.0f".format(value)}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight((heightFactor * 0.8).toFloat().coerceIn(0.05f, 1f))
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PayoutHistorySection(payouts: List<Payout>) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
            Text("PAYOUT HISTORY", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = MaterialTheme.colorScheme.primary)
        }
        if (expanded) {
            payouts.take(10).forEach { p ->
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payout on ${formatPST(p.date)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${p.transactionCount} transactions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("₱${"%.2f".format(p.totalAmount)}", fontWeight = FontWeight.Black, color = GreenSync)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction, onRefund: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
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
                    if (tx.type == "Ferry") Icons.Default.DirectionsBoat else Icons.Default.DirectionsBus,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${tx.passengerName} • ${tx.type}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(tx.route, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatPST(tx.timestamp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₱${"%.2f".format(tx.commissionAmount)}", fontWeight = FontWeight.Bold, color = if (tx.status == "Refunded") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Text("Gross: ₱${"%.0f".format(tx.grossAmount)}", fontSize = 9.sp, color = Color.Gray)
                if (tx.status == "Refunded") {
                    Surface(color = ErrorContainerSleek, shape = RoundedCornerShape(4.dp)) {
                        Text("REFUNDED", color = OnErrorContainerSleek, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                } else {
                    TextButton(onClick = onRefund, contentPadding = PaddingValues(0.dp)) {
                        Text("Refund", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogSection(logs: List<AuditLog>) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
            Text("Role Audit Log", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        }
        if (expanded) {
            logs.take(20).forEach { log ->
                Text("${formatPST(log.timestamp)}: ${log.role.uppercase()} ${log.action}", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}
