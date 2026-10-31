package com.chessfloat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF60A5FA),
                background = Color(0xFF0B1220), surface = Color(0xFF101827))) {
                ChessFloatHome(
                    overlayGranted = Settings.canDrawOverlays(this),
                    onGrantOverlay = { startActivity(Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"))) },
                    onStartOverlay = {
                        if (Settings.canDrawOverlays(this))
                            startService(Intent(this, FloatingAssistantService::class.java))
                    },
                    onStopOverlay = { stopService(Intent(this, FloatingAssistantService::class.java)) },
                    onStartCapture = { resultCode, data ->
                        ScreenCaptureService.start(this, resultCode, data)
                    },
                    onStopCapture = {
                        stopService(Intent(this, ScreenCaptureService::class.java))
                    },
                    engineFile = File(applicationInfo.nativeLibraryDir, "libstockfish.so")
                )
            }
        }
    }
}

@Composable
private fun ChessFloatHome(
    overlayGranted: Boolean,
    onGrantOverlay: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onStartCapture: (Int, Intent) -> Unit,
    onStopCapture: () -> Unit,
    engineFile: File
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var authorized by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("Study / analysis") }
    var fen by remember { mutableStateOf(START_FEN) }
    var depth by remember { mutableIntStateOf(20) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Stockfish engine status will be checked from the packaged native binary.") }
    var candidates by remember { mutableStateOf<List<CandidateMove>>(emptyList()) }
    val modes = listOf("Puzzle", "Lesson", "Study / analysis",
        "Completed-game review", "Live rated game")
    val enabled = authorized && mode != "Live rated game"
    val projectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    val captureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            onStartCapture(result.resultCode, result.data!!)
            status = "Screen capture active. Frames remain on this device."
        } else status = "Screen capture permission was not granted."
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1220)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("CHESSFLOAT", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Floating Stockfish companion · bottom-player orientation",
                color = Color(0xFF94A3B8))

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SESSION CONTEXT", color = Color(0xFF93C5FD))
                    modes.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = mode == item, onClick = {
                                mode = item
                                if (item == "Live rated game") authorized = false
                            })
                            Text(item, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = authorized && mode != "Live rated game",
                            onCheckedChange = { authorized = it && mode != "Live rated game" })
                        Text("I confirm this is an authorized study/review session",
                            color = Color.White)
                    }
                    Text(if (enabled) "Study engine analysis enabled"
                        else if (mode == "Live rated game") "Engine analysis disabled for live rated games"
                        else "Confirm the study/review context to enable analysis",
                        color = if (enabled) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("FLOATING WINDOW", color = Color(0xFF93C5FD))
                    Text(if (overlayGranted) "Overlay permission granted" else "Overlay permission required",
                        color = Color.White)
                    Button(onClick = if (overlayGranted) onStartOverlay else onGrantOverlay,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (overlayGranted) "Launch draggable floating panel" else "Grant overlay permission")
                    }
                    OutlinedButton(onClick = onStopOverlay, modifier = Modifier.fillMaxWidth()) {
                        Text("Stop floating panel")
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("SCREEN CAPTURE", color = Color(0xFF93C5FD))
                    Text("Android will show its own screen-sharing consent prompt. Stop capture from its persistent notification or below.",
                        color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { captureLauncher.launch(projectionManager.createScreenCaptureIntent()) },
                        modifier = Modifier.fillMaxWidth()) { Text("Authorize and start screen capture") }
                    OutlinedButton(onClick = onStopCapture, modifier = Modifier.fillMaxWidth()) {
                        Text("Stop screen capture")
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("LOCAL STOCKFISH ENGINE", color = Color(0xFF93C5FD))
                    Text(if (engineFile.exists()) "Stockfish 19 ARM64 bundled in this APK" else "Bundled engine missing for this device ABI",
                        color = if (engineFile.exists()) Color(0xFF4ADE80) else Color(0xFFFBBF24))
                    Text("Official Stockfish 19 universal ARM64 executable is packaged with this build. GPLv3 notices and the corresponding source link are included in app assets.",
                        color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101827))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("POSITION INPUT", color = Color(0xFF93C5FD))
                    OutlinedTextField(value = fen, onValueChange = { fen = it },
                        modifier = Modifier.fillMaxWidth(), label = { Text("FEN") },
                        minLines = 2, textStyle = MaterialTheme.typography.bodySmall)
                    Text("Depth: $depth", color = Color.White)
                    Slider(value = depth.toFloat(), onValueChange = { depth = it.toInt() },
                        valueRange = 10f..30f, steps = 19)
                    Button(enabled = enabled && !busy && engineFile.exists(),
                        onClick = {
                            val error = FenValidator.validate(fen)
                            if (error != null) {
                                status = error
                            } else {
                                busy = true
                                status = "Stockfish is analyzing…"
                                scope.launch {
                                    try {
                                        candidates = StockfishUciRunner(engineFile).analyze(fen, depth)
                                        status = if (candidates.isEmpty())
                                            "No candidate PV returned (terminal position or engine issue)."
                                        else "Analysis complete · scores are from side-to-move perspective."
                                    } catch (e: Exception) {
                                        status = "Analysis failed: ${e.message}"
                                    } finally { busy = false }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (busy) "Analyzing…" else "Analyze top 3 moves")
                    }
                    Text(status, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                    candidates.sortedBy { it.rank }.forEach { move ->
                        val color = when (move.rank) {
                            1 -> Color(0xFF4ADE80)
                            2 -> Color(0xFF60A5FA)
                            else -> Color(0xFFFBBF24)
                        }
                        Text("#${move.rank}  ${move.uci}   ${move.scoreCp?.let { " ${it} cp" } ?: move.mateIn?.let { " mate $it" } ?: "score n/a"}   depth ${move.depth}",
                            color = color)
                    }
                }
            }
            Text("Three-arrow overlay geometry and TFLite model adapter are present as integration layers. Automatic board localization, trained piece model, and board-to-FEN recognition are not yet complete; this build never invents moves from pixels.",
                color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
        }
    }
}
