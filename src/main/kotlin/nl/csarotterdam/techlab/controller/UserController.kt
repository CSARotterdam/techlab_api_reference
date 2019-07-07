package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.misc.AUTHORIZATION
import nl.csarotterdam.techlab.model.db.User
import nl.csarotterdam.techlab.model.db.UserInput
import nl.csarotterdam.techlab.service.AuthService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/user")
class UserController(
        private val authService: AuthService
) {

    @GetMapping("/id/{id}")
    fun readUserById(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = authService.readUserById(token, id)

    @GetMapping("/code/{code}")
    fun readUserByCode(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable code: String
    ) = authService.readUserByCode(token, code)

    @PostMapping("/create")
    fun createUser(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody user: UserInput
    ) = authService.createUser(token, user)

    @PutMapping("/update")
    fun updateUser(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody user: User
    ) = authService.updateUser(token, user)
}