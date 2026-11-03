package com.sattva.brahmamuhurta

import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AlarmActivity : ComponentActivity() {
    private var ringtone: android.media.Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        try { ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); if (android.os.Build.VERSION.SDK_INT >= 28) ringtone?.isLooping = true; ringtone?.play() } catch (_: Exception) { }
        setContent {
            val pulse = rememberInfiniteTransition(label = "alarm-sun")
            val scale by pulse.animateFloat(.94f, 1.07f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sun-scale")
            val glow by pulse.animateFloat(.72f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sun-glow")
            val safeTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF7772B4), Color(0xFFB69BC7), Color(0xFFFFC8A9), Color(0xFF3D3B68))))) {
                Canvas(Modifier.fillMaxSize()) {
                    val c = Offset(size.width*.5f, size.height*.60f)
                    val r = size.minDimension*.22f*scale
                    drawCircle(Color(0x22FFF1D4), r*2.1f, c)
                    drawCircle(Color(0x33FFF1D4), r*1.65f, c)
                    drawCircle(Color(0x66FFF1D4), r*1.28f, c, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF8D9), Color(0xFFFFD18C), Color(0xFFFFA76D)), c, r), r, c)
                    val horizon = size.height*.69f
                    val hills = Path().apply { moveTo(0f,horizon+40.dp.toPx()); lineTo(size.width*.22f,horizon-42.dp.toPx()); lineTo(size.width*.48f,horizon+22.dp.toPx()); lineTo(size.width*.76f,horizon-58.dp.toPx()); lineTo(size.width,horizon+20.dp.toPx()); lineTo(size.width,size.height); lineTo(0f,size.height); close() }
                    drawPath(hills, Brush.verticalGradient(listOf(Color(0xFF716B9A), Color(0xFF39375F))))
                    for (i in 0..18) drawCircle(Color(0x88FFF2E4)*glow, (i%3+1).dp.toPx(), Offset((i*67%size.width.toInt()).toFloat(), (i*43%size.height.toInt()).toFloat()))
                }
                Column(Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = maxOf(safeTop, 24.dp)+22.dp, bottom = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✧  BRAHMA MUHURTA  ✧", color = Color(0xFFFFF0D4), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                    Spacer(Modifier.weight(1f))
                    Text("4:26 AM", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Time to wake, reflect and recharge", color = Color(0xFFFFF5EE), fontSize = 15.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { stopAlarm(); finish() }, modifier = Modifier.fillMaxWidth().height(62.dp), shape = RoundedCornerShape(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0CD), contentColor = Color(0xFF28233F))) {
                        Text("Dismiss", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(onClick = { stopAlarm(); AlarmScheduler.test(this@AlarmActivity); finish() }, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Snooze (10 min)") }
                }
            }
        }
    }
    private fun stopAlarm() { ringtone?.stop(); NotificationManagerCompat.from(this).cancel(501) }
    override fun onDestroy() { ringtone?.stop(); super.onDestroy() }
}
