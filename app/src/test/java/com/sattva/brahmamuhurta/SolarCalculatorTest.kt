package com.sattva.brahmamuhurta

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SolarCalculatorTest {
    @Test fun hyderabadTimesArePlausible() {
        val result = SolarCalculator.events(LocalDate.of(2026, 10, 29), 17.385, 78.4867, ZoneId.of("Asia/Kolkata"))
        assertNotNull(result.sunrise); assertNotNull(result.sunset)
        assertEquals(6, result.sunrise!!.hour)
        assertTrue(result.sunset!!.hour in 17..18)
    }
    @Test fun brahmaWindowIsFortyEightMinutes() {
        val sunrise = SolarCalculator.events(LocalDate.of(2026, 10, 29), 17.385, 78.4867, ZoneId.of("Asia/Kolkata")).sunrise!!
        assertEquals(48L, java.time.Duration.between(sunrise.minusMinutes(96), sunrise.minusMinutes(48)).toMinutes())
    }
}
