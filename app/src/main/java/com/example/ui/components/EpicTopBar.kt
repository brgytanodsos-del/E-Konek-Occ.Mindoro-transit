package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EpicTopBar(
    title: String,
    roleName: String,
    isOnline: Boolean,
    onLogout: () -> Unit
) {
    val alpha by animateFloatAsState(if (isOnline) 1f else 0.6f, label = "alpha")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2540).copy(alpha = 0.95f),
                        Color(0xFF05101F)
                    )
                )
            )
            .blur(8.dp) 
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF8C00)
                )
                Text(
                    roleName,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isOnline) Color.Green else Color.Red,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Spacer(Modifier.width(6.dp))
                Text(if (isOnline) "LIVE" else "OFFLINE", color = Color.White.copy(alpha = alpha))
            }

            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, "Logout", tint = Color.White)
            }
        }
    }
}
