package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MindoroTransitTheme
import com.example.ui.theme.Navy
import com.example.ui.theme.Orange
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindoroTransitTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent(viewModel: AppViewModel = viewModel()) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    
    var selectedRoleForPin by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { StatusBar(isOnline) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                !isAuthenticated -> {
                    if (selectedRoleForPin != null) {
                        PinEntryScreen(
                            role = selectedRoleForPin!!,
                            onPinEntered = { viewModel.login(selectedRoleForPin!!, it) },
                            onBack = { selectedRoleForPin = null }
                        )
                    } else {
                        LoginScreen(onRoleSelected = { role ->
                            if (role == "passenger") {
                                viewModel.login("passenger", null)
                            } else {
                                selectedRoleForPin = role
                            }
                        })
                    }
                }
                currentRole == "superadmin" -> {
                    var adminTab by remember { mutableIntStateOf(0) }
                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    selected = adminTab == 0,
                                    onClick = { adminTab = 0 },
                                    icon = { Icon(Icons.Default.DirectionsBoat, null) },
                                    label = { Text("Port", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = adminTab == 1,
                                    onClick = { adminTab = 1 },
                                    icon = { Icon(Icons.Default.DirectionsBus, null) },
                                    label = { Text("Terminal", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = adminTab == 2,
                                    onClick = { adminTab = 2 },
                                    icon = { Icon(Icons.Default.Person, null) },
                                    label = { Text("Pax", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                NavigationBarItem(
                                    selected = adminTab == 3,
                                    onClick = { adminTab = 3 },
                                    icon = { Icon(Icons.Default.AdminPanelSettings, null) },
                                    label = { Text("Admin", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    ) { adminPadding ->
                        Box(modifier = Modifier.padding(adminPadding).fillMaxSize()) {
                            when (adminTab) {
                                0 -> PortStaffPanel(viewModel, isSuperAdmin = true)
                                1 -> TerminalStaffPanel(viewModel, isSuperAdmin = true)
                                2 -> PassengerPanel(viewModel, isSuperAdmin = true)
                                3 -> SuperAdminPanel(viewModel)
                            }
                        }
                    }
                }
                currentRole == "port" -> PortStaffPanel(viewModel)
                currentRole == "terminal" -> TerminalStaffPanel(viewModel)
                currentRole == "passenger" -> PassengerPanel(viewModel)
            }
            
            Toast(toastMessage)
        }
    }
}
