package nl.csarotterdam.techlab.data

import com.natpryce.konfig.Configuration
import mu.KotlinLogging
import nl.csarotterdam.techlab.config.server
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

@Component
class Database(
        private val config: Configuration
) {

    private val logger = KotlinLogging.logger {}

    private var connection = createConnection()

    private fun getConnection(
            retrying: Boolean = false
    ): Connection = connection.let {
        if (it.isClosed && !retrying) {
            connection = createConnection()
            getConnection(retrying = true)
        } else {
            it
        }
    }

    private fun createConnection(): Connection {
        // Check if JDBC Driver exists
        Class.forName("org.postgresql.Driver")

        val host = config[server.db.host]
        val port = config[server.db.port]
        val databaseName = config[server.db.name]
        val user = config[server.db.user]
        val password = config[server.db.pwd]
        val useSSL = config[server.db.ssl]

        val url = "jdbc:postgresql://$host:$port/$databaseName"
        val properties = Properties()
        properties.setProperty("user", user)
        properties.setProperty("password", password)

        logger.info { "Creating connection to PostgreSQL: url: '$url' and user: '$user'" }
        if (useSSL) {
            logger.info { "Using SSL for PostgreSQL" }
            properties.setProperty("ssl", "true")
            properties.setProperty("sslmode", "require")
        }
        return DriverManager.getConnection(url, properties)
    }

    fun execute(
            query: String,
            initPreparedStatement: (PreparedStatement) -> Unit
    ): Boolean {
        // Initialize connections
        val conn = getConnection()
        val ps = conn.prepareStatement(query)
        val res: Boolean

        fun close(ps: PreparedStatement) {
            ps.close()
        }

        try {
            // Set query
            initPreparedStatement(ps)
            res = ps.execute()
        } catch (e: Exception) {
            close(ps)
            throw e
        } finally {
            close(ps)
        }
        return res
    }

    private fun <T> executeQuery(
            query: String,
            initPreparedStatement: (PreparedStatement, Connection) -> Unit,
            convertEntry: (ResultSet, Connection) -> T
    ): List<T> {
        // Initialize connections
        val conn = getConnection()
        val ps = conn.prepareStatement(query)
        val list: MutableList<T> = mutableListOf()
        val rs: ResultSet

        fun close(ps: PreparedStatement) {
            ps.close()
        }

        try {
            // Set query
            initPreparedStatement(ps, conn)
            rs = ps.executeQuery()

            try {
                // Convert result
                while (rs.next()) {
                    list.add(convertEntry(rs, conn))
                }
                rs.close()
            } catch (e: Exception) {
                rs.close()
                throw e
            }
        } catch (e: Exception) {
            close(ps)
            throw e
        } finally {
            close(ps)
        }
        return list
    }

    fun <T> executeQuery(
            conn: Connection?,
            query: String,
            initPreparedStatement: (PreparedStatement, Connection) -> Unit,
            convertEntry: (ResultSet, Connection) -> T
    ): List<T> = conn?.run {
        // Initialize connections
        val ps = this.prepareStatement(query)

        // Set query
        initPreparedStatement(ps, this)
        val rs = ps.executeQuery()

        // Convert result
        val list: MutableList<T> = mutableListOf()
        while (rs.next()) {
            list.add(convertEntry(rs, this))
        }

        // Close connections
        rs.close()

        list
    } ?: executeQuery(query, initPreparedStatement, convertEntry)

    fun <T> assertOneResult(
            list: List<T>
    ): T? {
        return when {
            list.isEmpty() -> null
            list.size > 1 -> null
            else -> list.first()
        }
    }
}