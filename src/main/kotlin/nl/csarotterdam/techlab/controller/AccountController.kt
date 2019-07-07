package nl.csarotterdam.techlab.controller

import io.swagger.annotations.ApiParam
import nl.csarotterdam.techlab.model.AccountInput
import nl.csarotterdam.techlab.model.auth.AccountRole
import nl.csarotterdam.techlab.service.AuthService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/account")
class AccountController(
        private val authService: AuthService
) {

    @GetMapping("/list/active")
    fun listActive(
            @RequestHeader("Authorization") token: String
    ) = authService.listActive(token)

    @GetMapping("/list")
    fun list(
            @RequestHeader("Authorization") token: String
    ) = authService.list(token)

    @GetMapping("/id/{id}")
    fun readById(
            @RequestHeader("Authorization") token: String,
            @PathVariable id: String
    ) = authService.readById(token, id)

    @PostMapping("/create")
    fun create(
            @RequestHeader("Authorization") token: String,
            @RequestBody account: AccountInput
    ) = authService.create(token, account)

    @PatchMapping("/update/username")
    fun setUsername(
            @RequestHeader("Authorization") token: String,
            @ApiParam(required = true) @RequestParam id: String,
            @ApiParam(required = true) @RequestParam username: String
    ) = authService.setUsername(token, id, username)

    @PatchMapping("/update/password")
    fun setPassword(
            @RequestHeader("Authorization") token: String,
            @ApiParam(required = true) @RequestParam id: String,
            @ApiParam(required = true) @RequestParam password: String
    ) = authService.setPassword(token, id, password)

    @PatchMapping("/update/role")
    fun setRole(
            @RequestHeader("Authorization") token: String,
            @ApiParam(required = true) @RequestParam id: String,
            @ApiParam(required = true) @RequestParam role: AccountRole
    ) = authService.setRole(token, id, role)

    @PatchMapping("/update/active")
    fun setRole(
            @RequestHeader("Authorization") token: String,
            @ApiParam(required = true) @RequestParam id: String,
            @ApiParam(required = true) @RequestParam active: Boolean
    ) = authService.setActive(token, id, active)
}