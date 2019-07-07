package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.AUTHORIZATION
import nl.csarotterdam.techlab.model.LoanCreateInput
import nl.csarotterdam.techlab.model.ReservationInput
import nl.csarotterdam.techlab.service.InventoryAndReservationManagementService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/management")
class InventoryAndReservationManagementController(
        private val inventoryAndReservationManagementService: InventoryAndReservationManagementService
) {

    @PostMapping("/create/loan")
    fun createLoan(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody loan: LoanCreateInput
    ) = inventoryAndReservationManagementService.createLoan(token, loan)

    @GetMapping("/create/reservation")
    fun createReservation(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody reservation: ReservationInput
    ) = inventoryAndReservationManagementService.createReservation(token, reservation)
}