package com.sattva.brahmamuhurta

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.*

data class SolarEvents(val sunrise: LocalDateTime?, val sunset: LocalDateTime?)

object SolarCalculator {
    private const val ZENITH = 90.833
    fun events(date: LocalDate, latitude: Double, longitude: Double, zone: ZoneId): SolarEvents {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return SolarEvents(null, null)
        return SolarEvents(event(date, latitude, longitude, zone, true), event(date, latitude, longitude, zone, false))
    }
    private fun event(date: LocalDate, lat: Double, lon: Double, zone: ZoneId, rise: Boolean): LocalDateTime? {
        val utcHour = utcHour(date.dayOfYear, lat, lon, rise) ?: return null
        val millis = (utcHour * 3_600_000).roundToLong()
        for (offset in -1L..1L) {
            val instant = date.plusDays(offset).atStartOfDay(ZoneOffset.UTC).toInstant().plusMillis(millis)
            val local = LocalDateTime.ofInstant(instant, zone)
            if (local.toLocalDate() == date) return local
        }
        return null
    }
    private fun utcHour(day: Int, lat: Double, lon: Double, rise: Boolean): Double? {
        val lonHour = lon / 15.0
        val t = day + (((if (rise) 6.0 else 18.0) - lonHour) / 24.0)
        val m = 0.9856 * t - 3.289
        val l = norm(m + 1.916 * sin(rad(m)) + 0.020 * sin(2 * rad(m)) + 282.634)
        var ra = norm(deg(atan(0.91764 * tan(rad(l)))))
        ra += floor(l / 90) * 90 - floor(ra / 90) * 90
        ra /= 15
        val sinDec = 0.39782 * sin(rad(l))
        val cosDec = cos(asin(sinDec))
        val denom = cosDec * cos(rad(lat))
        if (denom == 0.0) return null
        val cosH = (cos(rad(ZENITH)) - sinDec * sin(rad(lat))) / denom
        if (cosH !in -1.0..1.0) return null
        val h = if (rise) (360 - deg(acos(cosH))) / 15 else deg(acos(cosH)) / 15
        return normHours(h + ra - 0.06571 * t - 6.622 - lonHour)
    }
    private fun rad(v: Double) = Math.toRadians(v)
    private fun deg(v: Double) = Math.toDegrees(v)
    private fun norm(v: Double) = ((v % 360) + 360) % 360
    private fun normHours(v: Double) = ((v % 24) + 24) % 24
    private fun Double.roundToLong() = kotlin.math.round(this).toLong()
}
