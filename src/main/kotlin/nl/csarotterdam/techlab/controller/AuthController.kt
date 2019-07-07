package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.auth.Credentials
import nl.csarotterdam.techlab.service.AuthService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/auth")
class AuthController(
        private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
            @RequestBody credentials: Credentials
    ) = authService.login(credentials)
}