package nl.csarotterdam.techlab.util

import java.sql.Date
import java.time.Duration
import java.time.Instant

object TimeUtils {

    fun currentDate(): Instant = Instant.now()

    fun betweenNowInDays(date: Date): Long = betweenInDays(currentDate(), date.toSQLInstant())

    fun betweenInDays(from: Date, to: Date): Long = betweenInDays(from.toSQLInstant(), to.toSQLInstant())

    fun betweenInDays(from: Instant, to: Instant): Long =
            Duration.between(from, to).toDays()
}