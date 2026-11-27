package com.example.swarstudio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.swarstudio.audio.MicrophonePitchEngine
import com.example.swarstudio.audio.PitchReading
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { SingingPracticeScreen() } }
    }
}

@Composable
private fun SingingPracticeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var reading by remember { mutableStateOf<PitchReading?>(null) }
    var status by remember { mutableStateOf("অনুশীলনের জন্য প্রস্তুত") }
    var active by remember { mutableStateOf(false) }
    var engine by remember { mutableStateOf<MicrophonePitchEngine?>(null) }
    fun begin() {
        if (active) return
        active = true; status = "শুনছি…"
        val e = MicrophonePitchEngine({ reading = it }, { status = it })
        engine = e
        scope.launch { e.start(); active = false; status = "অনুশীলন বন্ধ" }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) begin() else status = "Microphone permission দেওয়া হয়নি"
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1020)) {
        Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SWAR STUDIO", color = Color(0xFFB9A7FF), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(30.dp))
            Text("Live Pitch Studio", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Real-time vocal pitch monitor", color = Color.LightGray)
            Spacer(Modifier.height(36.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171F35)), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DETECTED NOTE", color = Color(0xFFB9A7FF), fontSize = 12.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(reading?.note ?: "—", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                    Text(reading?.let { "%.1f Hz  •  %+.0f cents".format(it.frequencyHz, it.cents) } ?: "গান শুরু করলে নোট দেখা যাবে", color = Color.LightGray)
                    Spacer(Modifier.height(20.dp))
                    Text(status, color = Color(0xFFB9A7FF))
                }
            }
            Spacer(Modifier.height(26.dp))
            Button(onClick = {
                if (active) { engine?.stop(); active = false; status = "অনুশীলন বন্ধ" }
                else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) begin()
                else permission.launch(Manifest.permission.RECORD_AUDIO)
            }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp)) {
                Text(if (active) "STOP PRACTICE" else "START PRACTICE", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            Text("পরবর্তী মডিউল: Pitch history • Intonation score • Tanpura • Sargam", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
