package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.data.model.Ship

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EpicShipCard(ship: Ship, fare: Double? = 450.0, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(animationSpec = tween(600, easing = FastOutSlowInEasing))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(12.dp, shape = MaterialTheme.shapes.extraLarge)
                .animateContentSize(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A2540), Color(0xFF1E3A5F))
                )
            )) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Animated Wave Icon
                    WaveAnimation()

                    Text(ship.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("${ship.route} • ${ship.departureTime}", color = MaterialTheme.colorScheme.tertiary)

                    LinearProgressIndicator(
                        progress = { (ship.capacity - ship.currentPassengers).toFloat() / ship.capacity },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                        Text("BOOK NOW • ₱${fare}")
                    }
                }
            }
        }
    }
}

@Composable
fun WaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "wave"
    )

    Icon(
        imageVector = Icons.Default.Waves,
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { rotationZ = waveOffset },
        tint = MaterialTheme.colorScheme.tertiary
    )
}
