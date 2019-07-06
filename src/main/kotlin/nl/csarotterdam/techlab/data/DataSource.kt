package nl.csarotterdam.techlab.data

import com.natpryce.konfig.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
abstract class DataSource<T> {

    @Autowired
    protected lateinit var database: Database

    @Autowired
    protected lateinit var config: Configuration

    protected abstract fun read(rs: ResultSet): T
}