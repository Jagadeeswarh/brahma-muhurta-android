package com.sattva.brahmamuhurta

import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat

class AlarmActivity : ComponentActivity() {
    private var ringtone: android.media.Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        try {
            ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            if (android.os.Build.VERSION.SDK_INT >= 28) ringtone?.isLooping = true
            ringtone?.play()
        } catch (_: Exception) { }
        setContent {
            val night = Color(0xFF112521); val gold = Color(0xFFE4B96D)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF203D35), night, Color(0xFF0A1916)))).padding(28.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("✦     ☾     ✦", color = gold, fontSize = 28.sp)
                    Spacer(Modifier.height(38.dp))
                    Text("BRAHMA MUHURTA", color = gold, fontSize = 12.sp, letterSpacing = 3.sp)
                    Spacer(Modifier.height(18.dp))
                    Text("The world is\nstill becoming.", color = Color(0xFFF8F1E3), fontSize = 35.sp, lineHeight = 43.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Light)
                    Spacer(Modifier.height(14.dp))
                    Text("A little silence. A little light. Begin gently.", color = Color(0xFFC4D0C7), fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(54.dp))
                    Button(onClick = { stopAlarm(); finish() }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = night)) { Text("I'M AWAKE · BEGIN", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { stopAlarm(); AlarmScheduler.test(this@AlarmActivity); finish() }) { Text("Snooze for 1 minute", color = Color(0xFFF8F1E3)) }
                }
            }
        }
    }
    private fun stopAlarm() { ringtone?.stop(); NotificationManagerCompat.from(this).cancel(501) }
    override fun onDestroy() { ringtone?.stop(); super.onDestroy() }
}
