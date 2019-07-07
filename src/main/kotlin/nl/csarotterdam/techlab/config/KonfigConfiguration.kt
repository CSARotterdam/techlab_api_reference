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
    val secret by stringType

    object db : PropertyGroup() {
        val host by stringType
        val port by intType
        val name by stringType
        val user by stringType
        val pwd by stringType
        val ssl by booleanType
    }
}

object inventory : PropertyGroup() {
    val readAll by stringType
    val readById by stringType
    val searchByName by stringType
    val searchByCategory by stringType

    val create by stringType
    val update by stringType
}

object inventoryMutation : PropertyGroup() {
    val readAll by stringType
    val readById by stringType
    val readAllByInventoryId by stringType
    val readAllByLoanId by stringType

    val create by stringType
}

object user : PropertyGroup() {
    val readById by stringType
    val readByCode by stringType

    val create by stringType
    val update by stringType
}

object contract : PropertyGroup() {
    val readById by stringType
    val readByAccountId by stringType
    val readByUserId by stringType

    val create by stringType
    val signByAccount by stringType
    val signByUser by stringType
}

object loan : PropertyGroup() {
    val readAllActiveLoans by stringType
    val readById by stringType
    val readByContractId by stringType
    val readActiveLoansByUserId by stringType
    val readByUserId by stringType

    val create by stringType
    val setReturned by stringType
}

object account : PropertyGroup() {
    val readAllActive by stringType
    val readAll by stringType
    val readById by stringType
    val readByUsername by stringType

    val create by stringType
    val setUsername by stringType
    val setPassword by stringType
    val setRole by stringType
    val setActive by stringType
}

object reservation : PropertyGroup() {
    val readAllCurrent by stringType
    val readAll by stringType
    val readById by stringType
    val readByUserId by stringType

    val create by stringType
    val setActivated by stringType
    val setDeleted by stringType
}

object reservationItem : PropertyGroup() {
    val readById by stringType
    val readByReservationId by stringType

    val create by stringType
}