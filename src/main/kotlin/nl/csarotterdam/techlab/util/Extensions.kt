package nl.csarotterdam.techlab.util

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.util.*

fun <T> T.toResultOrNull(rs: ResultSet): T? = if (rs.wasNull()) null else this
fun <T> PreparedStatement.setValueCanBeNull(
        n: Int,
        type: Int,
        value: T?,
        setter: (Int, T) -> Unit
) = value?.let { v -> setter(n, v) } ?: this.setNull(n, type)

fun java.sql.Date.toSQLInstant(): Instant {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.toInstant()
}