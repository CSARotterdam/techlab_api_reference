package nl.csarotterdam.techlab.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.util.*

@Configuration
class KonfigConfiguration {

    @Bean
    fun configuration() =
            systemProperties() overriding
                    EnvironmentVariables() overriding
                    ConfigurationProperties.fromOptionalFile(File("/etc/extra/server.properties")) overriding
                    ConfigurationProperties(properties = properties())

    private fun properties(): Properties {
        val properties = Properties()
        properties.load(this::class.java.classLoader.getResourceAsStream("system.properties"))
        return properties
    }
}

object server : PropertyGroup() {
    object db : PropertyGroup() {
        val host by stringType
        val port by intType
        val name by stringType
        val user by stringType
        val pwd by stringType
        val ssl by booleanType
    }
}