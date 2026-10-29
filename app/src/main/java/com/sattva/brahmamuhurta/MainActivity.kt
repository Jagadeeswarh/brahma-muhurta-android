package com.sattva.brahmamuhurta

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Cream = Color(0xFFF6F1E7)
private val Ink = Color(0xFF172D29)
private val Gold = Color(0xFFD9A45B)
private val Muted = Color(0xFF74786D)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Ink, secondary = Gold, background = Cream, surface = Color.White)) { BrahmaApp() } }
    }
    @Composable private fun BrahmaApp() {
        val prefs = remember { getSharedPreferences(AlarmScheduler.PREFS, MODE_PRIVATE) }
        var selected by remember { mutableStateOf(LocalDate.now()) }
        var tab by remember { mutableStateOf("Today") }
        var latitude by remember { mutableStateOf(prefs.getFloat(AlarmScheduler.LAT, 17.385f).toString()) }
        var longitude by remember { mutableStateOf(prefs.getFloat(AlarmScheduler.LON, 78.4867f).toString()) }
        var enabled by remember { mutableStateOf(prefs.getBoolean(AlarmScheduler.ENABLED, false)) }
        var offset by remember { mutableIntStateOf(prefs.getInt(AlarmScheduler.OFFSET, 96)) }
        val zone = ZoneId.systemDefault()
        val lat = latitude.toDoubleOrNull()?.coerceIn(-90.0, 90.0) ?: 17.385
        val lon = longitude.toDoubleOrNull()?.coerceIn(-180.0, 180.0) ?: 78.4867
        val events = remember(selected, lat, lon, zone) { SolarCalculator.events(selected, lat, lon, zone) }
        val start = events.sunrise?.minusMinutes(96)
        val end = events.sunrise?.minusMinutes(48)
        LazyColumn(Modifier.fillMaxSize().background(Cream).padding(horizontal = 20.dp)) {
            item {
                Spacer(Modifier.height(22.dp))
                Text("☼  SATTVA · DAILY RITUAL", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(10.dp))
                Text("Brahma Muhurta", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("A quieter beginning to your day.", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Today", "Calendar", "Settings").forEach { label ->
                        Surface(color = if (tab == label) Ink else Color.White, shape = RoundedCornerShape(30.dp), modifier = Modifier.clickable { tab = label }.padding(0.dp)) {
                            Text(label, color = if (tab == label) Cream else Ink, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            if (tab == "Calendar") {
                item {
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Text(selected.format(DateTimeFormatter.ofPattern("MMMM yyyy")), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { selected = selected.minusMonths(1) }) { Text("‹ Month") }
                                OutlinedButton(onClick = { selected = LocalDate.now() }) { Text("Today") }
                                OutlinedButton(onClick = { selected = selected.plusMonths(1) }) { Text("Month ›") }
                            }
                            Spacer(Modifier.height(12.dp))
                            val first = selected.withDayOfMonth(1)
                            val days = first.lengthOfMonth()
                            val offsetStart = first.dayOfWeek.value % 7
                            (0 until ((offsetStart + days + 6) / 7)).forEach { week ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    (0..6).forEach { col ->
                                        val day = week * 7 + col - offsetStart + 1
                                        Box(Modifier.size(38.dp).background(if (day == selected.dayOfMonth) Gold else Color.Transparent, RoundedCornerShape(12.dp)).clickable(enabled = day in 1..days) { if (day in 1..days) selected = first.withDayOfMonth(day) }, contentAlignment = Alignment.Center) {
                                            Text(if (day in 1..days) day.toString() else "", color = Ink, fontSize = 13.sp, fontWeight = if (day == selected.dayOfMonth) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                            Text("Select any date to calculate its local solar times.", color = Muted, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Ink)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Text(selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())).uppercase(), color = Color(0xFFC4D0C7), fontSize = 10.sp, letterSpacing = 1.2.sp)
                        Spacer(Modifier.height(14.dp))
                        Text("Your sacred window", color = Cream, fontSize = 17.sp)
                        Spacer(Modifier.height(7.dp))
                        Text(if (start != null && end != null) "${time(start)} – ${time(end)}" else "No sunrise available", color = Gold, fontSize = 27.sp, fontWeight = FontWeight.Light)
                        Text("48 minutes · ending 48 minutes before sunrise", color = Color(0xFFC4D0C7), fontSize = 11.sp)
                        Spacer(Modifier.height(20.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("SUNRISE", color = Color(0xFFC4D0C7), fontSize = 10.sp); Text(events.sunrise?.let(::time) ?: "—", color = Cream, fontSize = 18.sp) }
                            Column { Text("SUNSET", color = Color(0xFFC4D0C7), fontSize = 10.sp); Text(events.sunset?.let(::time) ?: "—", color = Cream, fontSize = 18.sp) }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
            if (tab == "Settings") {
                item {
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(18.dp)) {
                            Text("YOUR LOCATION", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Enter coordinates for accurate local times.", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") }, singleLine = true)
                            OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") }, singleLine = true)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                prefs.edit().putFloat(AlarmScheduler.LAT, lat.toFloat()).putFloat(AlarmScheduler.LON, lon.toFloat()).apply()
                                if (enabled) AlarmScheduler.scheduleNext(this@MainActivity)
                            }, colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Save location") }
                            Text("Hyderabad default: 17.3850, 78.4867", color = Muted, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("DAILY WAKE-UP ALARM", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(if (enabled) "Alarm enabled" else "Wake gently at your chosen time", color = Muted, fontSize = 11.sp)
                            }
                            Switch(checked = enabled, onCheckedChange = { checked ->
                                enabled = checked
                                prefs.edit().putBoolean(AlarmScheduler.ENABLED, checked).putInt(AlarmScheduler.OFFSET, offset).putFloat(AlarmScheduler.LAT, lat.toFloat()).putFloat(AlarmScheduler.LON, lon.toFloat()).apply()
                                if (checked) {
                                    if (!AlarmScheduler.canSchedule(this@MainActivity) && Build.VERSION.SDK_INT >= 31) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                                    else AlarmScheduler.scheduleNext(this@MainActivity)
                                } else AlarmScheduler.cancel(this@MainActivity)
                            })
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("ALARM TIME", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                        listOf(96, 72, 48).forEach { minutes ->
                            Row(Modifier.fillMaxWidth().clickable { offset = minutes; prefs.edit().putInt(AlarmScheduler.OFFSET, minutes).apply(); if (enabled) AlarmScheduler.scheduleNext(this@MainActivity) }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = offset == minutes, onClick = { offset = minutes; prefs.edit().putInt(AlarmScheduler.OFFSET, minutes).apply(); if (enabled) AlarmScheduler.scheduleNext(this@MainActivity) })
                                Column(Modifier.padding(start = 6.dp)) {
                                    Text(when (minutes) { 96 -> "Beginning"; 72 -> "Midpoint"; else -> "End of window" }, color = Ink, fontSize = 13.sp)
                                    Text("$minutes minutes before sunrise", color = Muted, fontSize = 11.sp)
                                }
                            }
                        }
                        OutlinedButton(onClick = { if (!AlarmScheduler.canSchedule(this@MainActivity) && Build.VERSION.SDK_INT >= 31) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) else AlarmScheduler.test(this@MainActivity) }, modifier = Modifier.fillMaxWidth()) { Text("Test alarm in 1 minute") }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE5D8))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("ABOUT BRAHMA MUHURTA", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("This app uses the 48-minute convention: the window begins 96 minutes before sunrise and ends 48 minutes before sunrise. Sunrise and sunset are estimated offline from your date, coordinates, and device time zone.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("Times are approximate and may differ from local almanacs or observed horizons.", color = Muted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
    private fun time(value: java.time.LocalDateTime) = value.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
}
