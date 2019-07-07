package nl.csarotterdam.techlab.util

import java.time.Duration
import java.time.Instant

object TimeUtils {

    fun currentDate(): Instant = Instant.now()

    fun betweenNowInDays(date: Instant): Long = betweenInDays(currentDate(), date)

    fun betweenInDays(from: Instant, to: Instant): Long =
            Duration.between(from, to).toDays()
}