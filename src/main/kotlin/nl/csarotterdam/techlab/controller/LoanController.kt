package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.AUTHORIZATION
import nl.csarotterdam.techlab.service.LoanService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/loan")
class LoanController(
        private val loanService: LoanService
) {

    @GetMapping("/active")
    fun readAllActiveLoans(
            @RequestHeader(AUTHORIZATION) token: String
    ) = loanService.readAllActiveLoans(token)

    @GetMapping("/id/{id}")
    fun readById(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = loanService.readById(token, id)

    @GetMapping("/contract/{contractId}")
    fun readByContractId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable contractId: String
    ) = loanService.readByContractId(token, contractId)

    @GetMapping("/user/{userId}/active")
    fun readActiveLoansByUserId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable userId: String
    ) = loanService.readActiveLoansByUserId(token, userId)

    @GetMapping("/user/{userId}")
    fun readByUserId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable userId: String
    ) = loanService.readByUserId(token, userId)

    @PatchMapping("/return/id/{id}")
    fun setReturned(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable id: String
    ) = loanService.setReturned(token, id)
}