package nl.csarotterdam.techlab.util

import java.sql.Date
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object TimeUtils {

    fun currentDate(): Instant = Instant.now()

    fun betweenNowInDays(date: Instant): Long = betweenInDays(currentDate(), date)

    fun betweenInDays(from: Instant, to: Instant): Long {
        val zoneId = ZoneId.of("Europe/Amsterdam")
        var f = LocalDateTime.ofInstant(from, zoneId)
        val t = LocalDateTime.ofInstant(to, zoneId)

        if (
                f.year == t.year &&
                f.monthValue == t.monthValue &&
                f.dayOfMonth == t.dayOfMonth
        ) {
            return 0
        } else {
            var days = 0L
            while (!(f.year == t.year &&
                            f.monthValue == t.monthValue &&
                            f.dayOfMonth == t.dayOfMonth)) {
                if (from.isBefore(to)) {
                    f = f.plusDays(1)
                    days++
                } else {
                    f = f.minusDays(1)
                    days--
                }
            }
            return days
        }
    }

    fun getAllDaysInclusive(from: Instant, to: Instant): List<Instant> {
        val zoneOffset = ZoneOffset.of("+02:00")
        val zoneId = ZoneId.of("Europe/Amsterdam")
        var f = LocalDateTime.ofInstant(from, zoneId)
        val t = LocalDateTime.ofInstant(to, zoneId)

        if (
                f.year == t.year &&
                f.monthValue == t.monthValue &&
                f.dayOfMonth == t.dayOfMonth
        ) {
            return listOf(from)
        } else {
            val days = mutableListOf<Instant>()
            days.add(from)
            while (!(f.year == t.year &&
                            f.monthValue == t.monthValue &&
                            f.dayOfMonth == t.dayOfMonth)) {
                if (from.isBefore(to)) {
                    f = f.plusDays(1)
                    days.add(f.toInstant(zoneOffset))
                } else {
                    return days
                }
            }
            return days
        }
    }

    fun first(dates: List<Date>): Date =
            requireNotNull(dates.minBy { it.toSQLInstant().toEpochMilli() })

    fun last(dates: List<Date>): Date =
            requireNotNull(dates.maxBy { it.toSQLInstant().toEpochMilli() })
}