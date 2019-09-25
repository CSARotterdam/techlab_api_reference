package nl.csarotterdam.techlab

import nl.csarotterdam.techlab.model.auth.Credentials
import nl.csarotterdam.techlab.service.AuthService
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ContextConfiguration
@ExtendWith(SpringExtension::class)
abstract class BaseTest {

    @Autowired
    lateinit var authService: AuthService

    val token by lazy {
        authService.login(Credentials("techlab", "password")).token
    }
}