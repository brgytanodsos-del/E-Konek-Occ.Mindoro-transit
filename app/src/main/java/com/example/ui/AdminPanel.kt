package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val vanBusComm = txs.filter { it.status == "Completed" && (it.type == "Van" || it.type == "Bus") }.sumOf { it.commissionAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Panel") },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(4.dp)).background(Orange).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("ADMIN MODE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    var showLogoutConfirm by remember { mutableStateOf(false) }
                    if (showLogoutConfirm) {
                        TextButton(onClick = { viewModel.logout() }) { Text("Confirm Logout", color = Color.Red) }
                        IconButton(onClick = { showLogoutConfirm = false }) { Icon(Icons.Default.Close, contentDescription = null) }
                    } else {
                        IconButton(onClick = { showLogoutConfirm = true }) { Icon(Icons.Default.Logout, contentDescription = "Logout") }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    StatCard("Total Commissions", "PHP ${"%.2f".format(totalCommissions)}")
                    StatCard("Gross Revenue", "PHP ${"%.2f".format(totalGross)}")
                    StatCard("Pending Payout", "PHP ${"%.2f".format(pendingPayout)}", color = Orange)
                    StatCard("Transactions", "${txs.size}")
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Commission Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                SimpleBarChart(ferryComm, vanBusComm)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Transaction Log", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.markAllAsPaid() }, colors = ButtonDefaults.buttonColors(containerColor = GreenSync)) {
                        Text("Mark All Paid")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(txs) { tx ->
                TransactionRow(tx, onRefund = { viewModel.refundTransaction(tx) })
            }
            
            item {
                Spacer(modifier = Modifier.height(48.dp))
                AuditLogSection(logs)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color = PrimarySleek) {
    Card(
        modifier = Modifier.width(160.dp).padding(end = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun SimpleBarChart(ferry: Double, other: Double) {
    val total = (ferry + other).coerceAtLeast(1.0)
    val ferryP = (ferry / total).toFloat().coerceAtLeast(0.05f)
    val otherP = (other / total).toFloat().coerceAtLeast(0.05f)
    
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            Box(modifier = Modifier.weight(ferryP).fillMaxHeight().background(Navy).padding(4.dp)) {
                Text("Ferry", color = Color.White, fontSize = 10.sp)
            }
            Box(modifier = Modifier.weight(otherP).fillMaxHeight().background(Orange).padding(4.dp)) {
                Text("Van/Bus", color = Color.White, fontSize = 10.sp)
            }
        }
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text("PHP ${"%.0f".format(ferry)}", modifier = Modifier.weight(ferryP), fontSize = 12.sp)
            Text("PHP ${"%.0f".format(other)}", modifier = Modifier.weight(otherP).wrapContentWidth(Alignment.End), fontSize = 12.sp)
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
