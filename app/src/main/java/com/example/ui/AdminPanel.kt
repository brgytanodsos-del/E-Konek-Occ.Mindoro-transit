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
                    Text("COMMISSION BREAKDOWN", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    SleekBarChart(ferryComm, vanComm, busComm)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("TRANSACTION LOG", fontWeight = FontWeight.Black, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            val csv = "Timestamp,Ref,Passenger,Route,Type,Gross,Commission,Status\n" + 
                                txs.joinToString("\n") { "${it.timestamp},${it.id},${it.passengerName},${it.route},${it.type},${it.grossAmount},${it.commissionAmount},${it.status}" }
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
            
            items(txs.sortedByDescending { it.timestamp }) { tx ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TransactionRow(tx, onRefund = { viewModel.refundTransaction(tx) })
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
    val total = (ferry + van + bus).coerceAtLeast(1.0)
    val fP = (ferry / total).toFloat().coerceAtLeast(0.02f)
    val vP = (van / total).toFloat().coerceAtLeast(0.02f)
    val bP = (bus / total).toFloat().coerceAtLeast(0.02f)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.weight(fP).fillMaxHeight().background(Navy))
            Box(modifier = Modifier.weight(vP).fillMaxHeight().background(Orange))
            Box(modifier = Modifier.weight(bP).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartLegend("Ferry", ferry, Navy)
            ChartLegend("Van", van, Orange)
            ChartLegend("Bus", bus, MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ChartLegend(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₱${"%.0f".format(value)}", fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
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
