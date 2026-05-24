package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
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
    isOnline: Boolean
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
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
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
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadData(getLeafletHtml(center, markers), "text/html", "UTF-8")
            }
        },
        update = { webView ->
            webView.loadData(getLeafletHtml(center, markers), "text/html", "UTF-8")
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
