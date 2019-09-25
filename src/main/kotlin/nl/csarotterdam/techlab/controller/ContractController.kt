package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.misc.AUTHORIZATION
import nl.csarotterdam.techlab.model.db.ContractInput
import nl.csarotterdam.techlab.service.ContractService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/contract")
class ContractController(
        private val contractService: ContractService
) {

    @GetMapping("/id/{id}")
    fun readById(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = contractService.readById(token, id)

    @GetMapping("/account/{accountId}")
    fun readByAccountId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable accountId: String
    ) = contractService.readByAccountId(token, accountId)

    @GetMapping("/user/{userId}")
    fun readByUserId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable userId: String
    ) = contractService.readByUserId(token, userId)

    @PatchMapping("/sign/account/{accountId}")
    fun signByAccount(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable accountId: String
    ) = contractService.signByAccount(token, accountId)

    @PatchMapping("/sign/user/{userId}")
    fun signByUser(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable userId: String
    ) = contractService.signByUser(token, userId)
}