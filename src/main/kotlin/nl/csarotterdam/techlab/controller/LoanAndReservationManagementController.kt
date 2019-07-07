package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.db.LoanCreateInput
import nl.csarotterdam.techlab.model.db.ReservationInput
import nl.csarotterdam.techlab.model.misc.AUTHORIZATION
import nl.csarotterdam.techlab.service.LoanAndReservationManagementService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/management")
class LoanAndReservationManagementController(
        private val loanAndReservationManagementService: LoanAndReservationManagementService
) {

    @PostMapping("/verify/loan")
    fun verifyLoan(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody loan: LoanCreateInput
    ) = loanAndReservationManagementService.verifyLoan(token, loan)

    @PostMapping("/create/loan")
    fun createLoan(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody loan: LoanCreateInput
    ) = loanAndReservationManagementService.createLoan(token, loan)

    @PostMapping("/verify/reservation")
    fun verifyReservation(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody reservation: ReservationInput
    ) = loanAndReservationManagementService.verifyReservation(token, reservation)

    @PostMapping("/create/reservation")
    fun createReservation(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody reservation: ReservationInput
    ) = loanAndReservationManagementService.createReservation(token, reservation)
}