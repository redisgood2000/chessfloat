package com.chessfloat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF60A5FA),
                background = Color(0xFF0B1220), surface = Color(0xFF101827))) {
                ChessFloatHome(Settings.canDrawOverlays(this),
                    onGrantOverlay = { startActivity(Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))) },
                    onStartOverlay = {
                        if (Settings.canDrawOverlays(this))
                            startService(Intent(this, FloatingAssistantService::class.java))
                    },
                    onStopOverlay = { stopService(Intent(this, FloatingAssistantService::class.java)) })
            }
        }
    }
}

@Composable
private fun ChessFloatHome(overlayGranted: Boolean, onGrantOverlay: () -> Unit,
    onStartOverlay: () -> Unit, onStopOverlay: () -> Unit) {
    var authorized by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("Study / analysis") }
    val modes = listOf("Puzzle", "Lesson", "Study / analysis",
        "Completed-game review", "Live rated game")
    val enabled = authorized && mode != "Live rated game"
    Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1220)) {
        Column(Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("CHESSFLOAT", style = MaterialTheme.typography.headlineMedium,
                color = Color.White)
            Text("Floating Stockfish companion", color = Color(0xFF94A3B8))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SESSION TYPE", color = Color(0xFF93C5FD))
                    modes.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(mode == item, onClick = { mode = item })
                            Text(item, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(authorized, { authorized = it })
                        Text("I confirm this is an authorized study/review session",
                            color = Color.White)
                    }
                    Text(if (enabled) "Engine recommendations enabled"
                        else if (mode == "Live rated game") "Engine recommendations disabled in live rated games"
                        else "Confirm the study/review session to enable analysis",
                        color = if (enabled) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("FLOATING WINDOW", color = Color(0xFF93C5FD))
                    Text(if (overlayGranted) "Overlay permission granted"
                        else "Overlay permission required", color = Color.White)
                    Button(onClick = if (overlayGranted) onStartOverlay else onGrantOverlay,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (overlayGranted) "Launch floating panel" else "Grant overlay permission")
                    }
                    OutlinedButton(onClick = onStopOverlay, modifier = Modifier.fillMaxWidth()) {
                        Text("Stop floating panel")
                    }
                }
            }
            Text("Three candidate arrows · bottom-player perspective · MultiPV 3",
                color = Color(0xFFCBD5E1))
            Text("Board recognition and bundled engine are integration milestones; no moves are fabricated.",
                color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
        }
    }
}
