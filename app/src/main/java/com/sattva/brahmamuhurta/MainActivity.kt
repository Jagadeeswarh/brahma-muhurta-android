package com.sattva.brahmamuhurta

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Cream = Color(0xFFFFF8F2)
private val Ink = Color(0xFF17171B)
private val Gold = Color(0xFFFFC978)
private val Muted = Color(0xFF6F6870)
private val Peach = Color(0xFFFFE1D0)
private val Lavender = Color(0xFFE8DFFF)
private val Purple = Color(0xFF51449A)
private val Sage = Color(0xFFE5EDE2)
private val Manrope = FontFamily(Font(R.font.manrope, FontWeight.Normal))

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Purple, secondary = Gold, background = Cream, surface = Cream)) { CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = Manrope)) { BrahmaApp() } } }
    }

    @Composable private fun BrahmaApp() {
        val prefs = remember { getSharedPreferences(AlarmScheduler.PREFS, MODE_PRIVATE) }
        var selected by remember { mutableStateOf(LocalDate.now()) }
        var tab by remember { mutableStateOf("Today") }
        var latitude by remember { mutableStateOf(prefs.getFloat(AlarmScheduler.LAT, 17.385f).toString()) }
        var longitude by remember { mutableStateOf(prefs.getFloat(AlarmScheduler.LON, 78.4867f).toString()) }
        var enabled by remember { mutableStateOf(prefs.getBoolean(AlarmScheduler.ENABLED, false)) }
        var offset by remember { mutableIntStateOf(prefs.getInt(AlarmScheduler.OFFSET, 96)) }
        var testMessage by remember { mutableStateOf("") }
        val zone = ZoneId.systemDefault()
        val lat = latitude.toDoubleOrNull()?.takeIf { it in -90.0..90.0 } ?: 17.385
        val lon = longitude.toDoubleOrNull()?.takeIf { it in -180.0..180.0 } ?: 78.4867
        val events = remember(selected, lat, lon, zone) { SolarCalculator.events(selected, lat, lon, zone) }
        val start = events.sunrise?.minusMinutes(96)
        val end = events.sunrise?.minusMinutes(48)

        Scaffold(containerColor = Cream, bottomBar = {
            NavigationBar(containerColor = Cream, tonalElevation = 0.dp) {
                NavigationBarItem(selected = tab == "Today", onClick = { tab = "Today" }, icon = { Icon(Icons.Rounded.Home, null) }, label = { Text("Today") })
                NavigationBarItem(selected = tab == "Calendar", onClick = { tab = "Calendar" }, icon = { Icon(Icons.Rounded.CalendarMonth, null) }, label = { Text("Calendar") })
                NavigationBarItem(selected = tab == "Settings", onClick = { tab = "Settings" }, icon = { Icon(Icons.Rounded.Settings, null) }, label = { Text("Settings") })
            }
        }) { padding ->
            AnimatedContent(targetState = tab, label = "main-tab", transitionSpec = {
                (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 18 }) togetherWith
                    (fadeOut(tween(140)) + slideOutVertically(tween(180)) { -it / 24 })
            }) { page ->
                LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), contentPadding = PaddingValues(top = maxOf(WindowInsets.displayCutout.asPaddingValues().calculateTopPadding(), 24.dp) + 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (page) {
                        "Today" -> {
                            item { TodayHeader() }
                            item { ScenicHomeIllustration() }
                            item { MuhurtaSummary(start?.let(::time) ?: "—", end?.let(::time) ?: "—") }
                            item { SunriseCompact(events.sunrise?.let(::time) ?: "—") }
                            item { RitualNote() }
                        }
                        "Calendar" -> {
                            item { PageHeading("Your calendar", "Explore the sacred hours on any date.") }
                            item { CalendarCard(selected, onSelect = { selected = it }, onMonth = { selected = selected.plusMonths(it) }) }
                            item { SelectedDayCard(selected, start?.let(::time) ?: "No sunrise", end?.let(::time) ?: "available", events.sunrise?.let(::time) ?: "—", events.sunset?.let(::time) ?: "—") }
                        }
                        else -> {
                            item { PageHeading("Settings", "Make the morning ritual yours.") }
                            item {
                                LocationCard(latitude, longitude, onLatitude = { latitude = it }, onLongitude = { longitude = it }, onSave = {
                                    prefs.edit().putFloat(AlarmScheduler.LAT, lat.toFloat()).putFloat(AlarmScheduler.LON, lon.toFloat()).apply()
                                    testMessage = "Location saved. Solar times will use these coordinates."
                                    if (enabled) AlarmScheduler.scheduleNext(this@MainActivity)
                                })
                            }
                            item {
                                AlarmSettingsCard(
                                    enabled = enabled, offset = offset, message = testMessage,
                                    onEnabled = { checked ->
                                        enabled = checked
                                        prefs.edit().putBoolean(AlarmScheduler.ENABLED, checked).putInt(AlarmScheduler.OFFSET, offset).putFloat(AlarmScheduler.LAT, lat.toFloat()).putFloat(AlarmScheduler.LON, lon.toFloat()).apply()
                                        if (checked) {
                                            if (!AlarmScheduler.canSchedule(this@MainActivity) && Build.VERSION.SDK_INT >= 31) startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                                            else AlarmScheduler.scheduleNext(this@MainActivity)
                                        } else AlarmScheduler.cancel(this@MainActivity)
                                    },
                                    onOffset = { value -> offset = value; prefs.edit().putInt(AlarmScheduler.OFFSET, value).apply(); if (enabled) AlarmScheduler.scheduleNext(this@MainActivity) },
                                    onTest = {
                                        testMessage = ""
                                        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            testMessage = "Allow notifications, then tap the test button again."
                                        } else if (!AlarmScheduler.canSchedule(this@MainActivity) && Build.VERSION.SDK_INT >= 31) {
                                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                                            testMessage = "Allow Alarms & reminders, return here, then test again."
                                        } else {
                                            if (Build.VERSION.SDK_INT >= 34 && !getSystemService(android.app.NotificationManager::class.java).canUseFullScreenIntent()) {
                                                startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")))
                                                testMessage = "Allow full-screen alarms, return here, then test again. A notification will still appear if full-screen access is off."
                                            } else {
                                                AlarmScheduler.test(this@MainActivity)
                                                testMessage = "Test scheduled for 10 seconds from now. Keep notifications enabled; tap the alarm notification if Android doesn't open it automatically."
                                            }
                                        }
                                    }
                                )
                            }
                            item { AboutCard() }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun TodayHeader() {
    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())), color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(4.dp))
    Text("Good Morning", color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
    Text("A sacred time to focus, reflect\\nand set your intention.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable private fun ScenicHomeIllustration() {
    val anim = rememberInfiniteTransition(label = "home-sun")
    val sunPulse by anim.animateFloat(.98f, 1.02f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "sun")
    Box(Modifier.fillMaxWidth().height(238.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width; val h=size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFFFFE7D5),Color(0xFFFFD9C6),Color(0xFFE5C7D8))))
            val sun=Offset(w*.50f,h*.61f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF6D8),Color(0xFFFFD58F),Color.Transparent),sun,h*.40f),h*.40f,sun)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF7D9),Color(0xFFFFD27D)),sun,h*.105f*sunPulse),h*.105f*sunPulse,sun)
            val far=Path().apply { moveTo(0f,h*.68f); cubicTo(w*.18f,h*.45f,w*.28f,h*.58f,w*.43f,h*.70f); cubicTo(w*.61f,h*.48f,w*.77f,h*.48f,w,h*.66f); lineTo(w,h); lineTo(0f,h); close() }
            drawPath(far,Brush.verticalGradient(listOf(Color(0xFFD2AFCB),Color(0xFF9B8DB8))))
            val mid=Path().apply { moveTo(0f,h*.79f); cubicTo(w*.20f,h*.58f,w*.32f,h*.73f,w*.48f,h*.82f); cubicTo(w*.69f,h*.58f,w*.82f,h*.67f,w,h*.76f); lineTo(w,h); lineTo(0f,h); close() }
            drawPath(mid,Brush.verticalGradient(listOf(Color(0xFF9386B2),Color(0xFF71658F))))
            val near=Path().apply { moveTo(0f,h*.91f); cubicTo(w*.24f,h*.72f,w*.33f,h*.84f,w*.52f,h*.91f); cubicTo(w*.72f,h*.75f,w*.84f,h*.78f,w,h*.88f); lineTo(w,h); lineTo(0f,h); close() }
            drawPath(near,Color(0xFF51486F))
            val tx=w*.88f; val ty=h*.59f
            drawRect(Color(0xFF51486F),Offset(tx-8.dp.toPx(),ty+15.dp.toPx()),androidx.compose.ui.geometry.Size(16.dp.toPx(),24.dp.toPx()))
            val roof=Path().apply { moveTo(tx-13.dp.toPx(),ty+15.dp.toPx()); lineTo(tx,ty); lineTo(tx+13.dp.toPx(),ty+15.dp.toPx()); close() }
            drawPath(roof,Color(0xFF51486F))
            drawRect(Color(0xFF51486F),Offset(tx-2.dp.toPx(),ty+5.dp.toPx()),androidx.compose.ui.geometry.Size(4.dp.toPx(),10.dp.toPx()))
            val bx=w*.10f; val by=h*.77f
            drawLine(Color(0xFF494365),Offset(bx,by+28.dp.toPx()),Offset(bx+2.dp.toPx(),by-23.dp.toPx()),4.dp.toPx())
            listOf(Offset(bx-14.dp.toPx(),by-17.dp.toPx()),Offset(bx+1.dp.toPx(),by-31.dp.toPx()),Offset(bx+16.dp.toPx(),by-15.dp.toPx())).forEach { drawCircle(Color(0xFF494365),13.dp.toPx(),it) }
        }
    }
}

@Composable private fun MuhurtaSummary(start:String,end:String) {
    Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF9F5)),elevation=CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Lavender,CircleShape),contentAlignment=Alignment.Center) { Text("✧",color=Purple,fontSize=27.sp) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Brahma Muhurta",color=Ink,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
                Text("$start – $end",color=Ink,fontSize=23.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.8).sp)
                Text("Duration · 48 minutes",color=Muted,fontSize=12.sp)
            }
        }
    }
}

@Composable private fun SunriseCompact(sunrise:String) {
    Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF9F5))) {
        Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=15.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("☼",color=Color(0xFFB98A4B),fontSize=24.sp)
            Spacer(Modifier.width(12.dp))
            Text("Sunrise",color=Ink,fontSize=15.sp,fontWeight=FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(sunrise,color=Ink,fontSize=17.sp,fontWeight=FontWeight.Bold)
        }
    }
}

@Composable private fun PageHeading(title: String, subtitle: String) {
    Text(title, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Light)
    Text(subtitle, color = Muted, fontSize = 14.sp)
}

@Composable private fun SunriseHero(start: String, end: String, date: LocalDate) {
    val transition = rememberInfiniteTransition(label = "sunrise-pulse")
    val pulse by transition.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sun-pulse")
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Ink), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(282.dp).background(Brush.verticalGradient(listOf(Color(0xFF29483E), Color(0xFF18201F))))) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * .52f, size.height * .43f)
                val radius = size.minDimension * .27f * pulse
                drawCircle(Brush.radialGradient(listOf(Color(0x99FFD18A), Color(0x55FFB86F), Color.Transparent), center, radius * 1.9f), radius * 1.9f, center)
                drawCircle(Brush.verticalGradient(listOf(Color(0xFFFFF1B8), Color(0xFFFFA95F))), radius * .70f, center)
                val ridge = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * .67f)
                    lineTo(size.width * .25f, size.height * .55f)
                    lineTo(size.width * .45f, size.height * .66f)
                    lineTo(size.width * .70f, size.height * .52f)
                    lineTo(size.width, size.height * .65f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(ridge, Brush.verticalGradient(listOf(Color(0xFF80749F), Color(0xFF51466F))))
                for (i in 0..18) {
                    val x = (i * 47 % size.width.toInt().coerceAtLeast(1)).toFloat()
                    val y = (i * 31 % (size.height * .48f).toInt().coerceAtLeast(1)).toFloat()
                    drawCircle(Color(0x66F6E8C8), if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx(), Offset(x, y))
                }
            }
            Text(date.format(DateTimeFormatter.ofPattern("EEEE  ·  d MMMM", Locale.getDefault())), color = Color(0xFFE7E0D9), fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart).padding(22.dp))
            Column(
                Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD17171B))))
                    .padding(start = 22.dp, end = 22.dp, top = 34.dp, bottom = 20.dp)
            ) {
                Text("BRAHMA MUHURTA", color = Color(0xFFFFD18A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(5.dp))
                Text("$start  –  $end", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("48 minutes · before sunrise", color = Color(0xFFE8E0DA), fontSize = 12.sp)
            }
        }
    }
}

@Composable private fun SolarDetails(sunrise: String, sunset: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SolarTile("☼", "SUNRISE", sunrise, Modifier.weight(1f))
        SolarTile("◒", "SUNSET", sunset, Modifier.weight(1f))
    }
}
@Composable private fun SolarTile(symbol: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp)) {
            Text(symbol, color = Gold, fontSize = 24.sp)
            Text(label, color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Text(value, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable private fun RitualNote() {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Sage)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("✧", color = Gold, fontSize = 32.sp, modifier = Modifier.padding(end = 14.dp))
            Column {
                Text("The stillness before dawn", color = Ink, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text("A traditional time for meditation, prayer, study and quiet reflection.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable private fun CalendarCard(selected: LocalDate, onSelect: (LocalDate) -> Unit, onMonth: (Long) -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selected.format(DateTimeFormatter.ofPattern("MMMM yyyy")), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Row {
                    TextButton(onClick = { onMonth(-1) }) { Text("‹", color = Ink, fontSize = 22.sp) }
                    TextButton(onClick = { onMonth(1) }) { Text("›", color = Ink, fontSize = 22.sp) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("S","M","T","W","T","F","S").forEach { Text(it, color = Muted, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
            Spacer(Modifier.height(8.dp))
            val first = selected.withDayOfMonth(1)
            val days = first.lengthOfMonth()
            val leading = first.dayOfWeek.value % 7
            repeat((leading + days + 6) / 7) { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) { col ->
                        val day = week * 7 + col - leading + 1
                        val valid = day in 1..days
                        val date = if (valid) first.withDayOfMonth(day) else first
                        val active = valid && date == selected
                        Box(Modifier.size(36.dp).background(if (active) Ink else Color.Transparent, CircleShape).clickable(enabled = valid) { onSelect(date) }, contentAlignment = Alignment.Center) {
                            Text(if (valid) "$day" else "", color = if (active) Cream else Ink, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
            TextButton(onClick = { onSelect(LocalDate.now()) }) { Text("Jump to today", color = Ink) }
        }
    }
}

@Composable private fun SelectedDayCard(date: LocalDate, start: String, end: String, sunrise: String, sunset: String) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Ink)) {
        Column(Modifier.padding(20.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")), color = Color(0xFFC4D0C7), fontSize = 12.sp)
            Text("$start – $end", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sunrise  $sunrise", color = Cream, fontSize = 13.sp)
                Text("Sunset  $sunset", color = Cream, fontSize = 13.sp)
            }
        }
    }
}

@Composable private fun LocationCard(latitude: String, longitude: String, onLatitude: (String) -> Unit, onLongitude: (String) -> Unit, onSave: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp)) {
            Text("YOUR LOCATION", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
            Text("Solar times are calculated locally from these coordinates.", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(latitude, onLatitude, label = { Text("Latitude") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(longitude, onLongitude, label = { Text("Longitude") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Save location") }
            Text("Hyderabad default · 17.3850, 78.4867", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable private fun AlarmSettingsCard(enabled: Boolean, offset: Int, message: String, onEnabled: (Boolean) -> Unit, onOffset: (Int) -> Unit, onTest: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DAILY WAKE-UP ALARM", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(if (enabled) "Your sunrise ritual is scheduled" else "A gentle reminder before sunrise", color = Muted, fontSize = 11.sp)
                }
                Switch(checked = enabled, onCheckedChange = onEnabled)
            }
            AnimatedVisibility(enabled) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Text("CHOOSE YOUR MOMENT", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    listOf(96, 72, 48).forEach { value ->
                        Row(Modifier.fillMaxWidth().clickable { onOffset(value) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = offset == value, onClick = { onOffset(value) })
                            Column(Modifier.padding(start = 6.dp)) {
                                Text(when (value) { 96 -> "Beginning of Brahma Muhurta"; 72 -> "Midpoint"; else -> "End of the window" }, color = Ink, fontSize = 13.sp)
                                Text("$value minutes before sunrise", color = Muted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Cream)
            Text("ALARM CHECK", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Test the notification and full-screen wake-up experience. The test rings in about 10 seconds.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onTest, modifier = Modifier.fillMaxWidth()) { Text("Test alarm (10 seconds)") }
            AnimatedVisibility(message.isNotBlank()) {
                Text(message, color = Ink, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 10.dp).animateContentSize())
            }
        }
    }
}

@Composable private fun AboutCard() {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE5D8))) {
        Column(Modifier.padding(18.dp)) {
            Text("ABOUT BRAHMA MUHURTA", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text("This app follows the 48-minute convention: the window begins 96 minutes before sunrise and ends 48 minutes before sunrise. Solar times are estimates and may differ from local almanacs.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

private fun time(value: java.time.LocalDateTime) = value.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
