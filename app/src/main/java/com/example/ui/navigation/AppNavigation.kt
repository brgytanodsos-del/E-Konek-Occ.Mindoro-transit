package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    appViewModel: AppViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentRole by authViewModel.currentRole.collectAsState()
    val isOnline by appViewModel.isOnline.collectAsState()
    val toastMessage by authViewModel.toastMessage.collectAsState()

    var selectedRoleForPin by remember { mutableStateOf<String?>(null) }
    var showBookingFlow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { StatusBar(isOnline) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                !isAuthenticated -> {
                    AuthFlow(
                        selectedRoleForPin = selectedRoleForPin,
                        onRoleSelected = { role ->
                            if (role == "passenger") {
                                authViewModel.login("passenger", null)
                            } else {
                                selectedRoleForPin = role
                            }
                        },
                        onPinEntered = { pin ->
                            selectedRoleForPin?.let { role ->
                                authViewModel.login(role, pin)
                            }
                        },
                        onBack = { selectedRoleForPin = null }
                    )
                }
                showBookingFlow -> {
                    BookingFlowScreen(
                        onFinish = { showBookingFlow = false },
                        isOnline = isOnline
                    )
                }
                currentRole == "superadmin" -> {
                    SuperAdminTabContainer(appViewModel, authViewModel)
                }
                else -> {
                    when (currentRole) {
                        "port" -> PortStaffPanel(appViewModel, authViewModel)
                        "terminal" -> TerminalStaffPanel(appViewModel, authViewModel)
                        "passenger" -> PassengerPanel(appViewModel, authViewModel, onStartBooking = { showBookingFlow = true })
                    }
                }
            }

            toastMessage?.let { Toast(it) }
        }
    }
}

@Composable
fun AuthFlow(
    selectedRoleForPin: String?,
    onRoleSelected: (String) -> Unit,
    onPinEntered: (String) -> Unit,
    onBack: () -> Unit
) {
    if (selectedRoleForPin != null) {
        PinEntryScreen(
            role = selectedRoleForPin,
            onPinEntered = onPinEntered,
            onBack = onBack
        )
    } else {
        LoginScreen(onRoleSelected = onRoleSelected)
    }
}

@Composable
fun SuperAdminTabContainer(appViewModel: AppViewModel, authViewModel: AuthViewModel) {
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
                0 -> PortStaffPanel(appViewModel, authViewModel, isSuperAdmin = true)
                1 -> TerminalStaffPanel(appViewModel, authViewModel, isSuperAdmin = true)
                2 -> PassengerPanel(appViewModel, authViewModel, isSuperAdmin = true, onStartBooking = {})
                3 -> SuperAdminPanel(appViewModel, adminViewModel = viewModel(), authViewModel = authViewModel)
            }
        }
    }
}
