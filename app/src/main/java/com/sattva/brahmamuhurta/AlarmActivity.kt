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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Manrope = FontFamily(Font(R.font.manrope, FontWeight.Normal))

class AlarmActivity : ComponentActivity() {
    private var ringtone: android.media.Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        try {
            ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            if (android.os.Build.VERSION.SDK_INT >= 28) ringtone?.isLooping = true
            ringtone?.play()
        } catch (_: Exception) { }

        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = Manrope)) {
                    val pulse = rememberInfiniteTransition(label = "alarm-sun")
                    val scale by pulse.animateFloat(
                        .94f, 1.07f,
                        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "sun-scale"
                    )
                    val glow by pulse.animateFloat(
                        .72f, 1f,
                        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "sun-glow"
                    )
                    val safeTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
                    val currentTime = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())) }

                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF7772B4), Color(0xFFB69BC7), Color(0xFFFFC8A9), Color(0xFF3D3B68))
                            )
                        )
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val w=size.width; val h=size.height
                            val sun=Offset(w*.50f,h*.59f)
                            val radius=size.minDimension*.205f*scale
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF7773B7),Color(0xFFB49BC9),Color(0xFFFFC9AC),Color(0xFFFFD5B6))))
                            // Soft dawn halo and fine concentric arcs.
                            drawCircle(Color(0x22FFF0D5).copy(alpha=glow),radius*2.15f,sun)
                            drawCircle(Color(0x20FFF0D5),radius*1.72f,sun)
                            drawCircle(Color(0x55FFF1D9),radius*1.38f,sun,style=androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFBE4),Color(0xFFFFE19B),Color(0xFFFFB36F)),sun,radius),radius,sun)
                            // Floating dawn clouds sit behind the sun.
                            val cloudY=h*.655f
                            for (i in 0..5) {
                                val x=w*(i/5f)
                                val cloud=Brush.horizontalGradient(listOf(Color(0xFFFFE7D9).copy(alpha=.0f),Color(0xFFFFE7D9).copy(alpha=.85f),Color(0xFFFFE7D9).copy(alpha=.0f)),x-70.dp.toPx(),x+70.dp.toPx())
                                drawOval(cloud,Offset(x-76.dp.toPx(),cloudY+(i%2)*12.dp.toPx()),androidx.compose.ui.geometry.Size(152.dp.toPx(),22.dp.toPx()))
                                drawCircle(Color(0xFFFFE8DC).copy(alpha=.8f),15.dp.toPx(),Offset(x-25.dp.toPx(),cloudY+2.dp.toPx()))
                                drawCircle(Color(0xFFFFE8DC).copy(alpha=.8f),19.dp.toPx(),Offset(x+3.dp.toPx(),cloudY-2.dp.toPx()))
                            }
                            val distant=Path().apply { moveTo(0f,h*.73f); cubicTo(w*.18f,h*.62f,w*.29f,h*.66f,w*.46f,h*.76f); cubicTo(w*.65f,h*.62f,w*.82f,h*.64f,w,h*.73f); lineTo(w,h); lineTo(0f,h); close() }
                            drawPath(distant,Brush.verticalGradient(listOf(Color(0xFFD2A8C4),Color(0xFF8E80A9))))
                            val middle=Path().apply { moveTo(0f,h*.82f); lineTo(w*.22f,h*.72f); cubicTo(w*.36f,h*.76f,w*.43f,h*.85f,w*.54f,h*.83f); lineTo(w*.78f,h*.70f); lineTo(w,h*.81f); lineTo(w,h); lineTo(0f,h); close() }
                            drawPath(middle,Brush.verticalGradient(listOf(Color(0xFF8176A4),Color(0xFF625B86))))
                            val foreground=Path().apply { moveTo(0f,h*.84f); lineTo(w*.22f,h*.75f); lineTo(w*.48f,h*.88f); lineTo(w*.76f,h*.72f); lineTo(w,h*.84f); lineTo(w,h); lineTo(0f,h); close() }
                            drawPath(foreground,Brush.verticalGradient(listOf(Color(0xFF514B78),Color(0xFF39365F))))
                            // Small foreground foliage for the illustrated postcard finish.
                            for (i in 0..8) {
                                val x=w*(.08f+i*.105f)
                                val y=h*(.91f+(i%3)*.018f)
                                drawLine(Color(0xFF38375B),Offset(x,y+18.dp.toPx()),Offset(x-7.dp.toPx(),y-7.dp.toPx()),2.dp.toPx())
                                drawOval(Color(0xFF4B496F),Offset(x-14.dp.toPx(),y-13.dp.toPx()),androidx.compose.ui.geometry.Size(12.dp.toPx(),7.dp.toPx()))
                                drawOval(Color(0xFF4B496F),Offset(x-2.dp.toPx(),y-20.dp.toPx()),androidx.compose.ui.geometry.Size(14.dp.toPx(),8.dp.toPx()))
                            }
                            for (i in 0..20) {
                                val x=(i*67 % w.toInt().coerceAtLeast(1)).toFloat()
                                val y=(i*43 % (h*.43f).toInt().coerceAtLeast(1)).toFloat()
                                drawCircle(Color(0x99FFF2E4),if(i%3==0) 2.dp.toPx() else 1.dp.toPx(),Offset(x,y))
                            }
                        }

                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 28.dp)
                                .padding(top = maxOf(safeTop, 24.dp) + 22.dp, bottom = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("♧", color = Color(0xFFFFF0D4), fontSize = 34.sp); Text("Brahma Muhurta", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold); Text("Time to wake, reflect and recharge", color = Color(0xFFFFF5EE), fontSize = 14.sp) }
                            Spacer(Modifier.weight(.62f))
                            Text(currentTime, color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                            Spacer(Modifier.height(10.dp))
                            Spacer(Modifier.weight(1.38f))
                            Button(
                                onClick = { stopAlarm(); finish() },
                                modifier = Modifier.fillMaxWidth().height(62.dp),
                                shape = RoundedCornerShape(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0CD), contentColor = Color(0xFF28233F))
                            ) {
                                Text("Dismiss", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { stopAlarm(); AlarmScheduler.test(this@AlarmActivity); finish() },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Snooze (10 min)")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        NotificationManagerCompat.from(this).cancel(501)
    }

    override fun onDestroy() {
        ringtone?.stop()
        super.onDestroy()
    }
}
