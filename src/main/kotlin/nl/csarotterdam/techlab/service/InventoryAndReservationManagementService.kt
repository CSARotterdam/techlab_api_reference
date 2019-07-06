package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class InventoryAndReservationManagementService(
        private val inventoryService: InventoryService,
        private val loanService: LoanService,
        private val reservationService: ReservationService,
        private val contractService: ContractService
) {

    private fun LoanCreateInput.convertToContract(): ContractInput = ContractInput(
            account_id = account_id,
            user_id = user_id
    )

    private fun ReservationInput.convert(): ContractInput = ContractInput(
            account_id = account_id,
            user_id = user_id
    )

    private fun ReservationInput.convert(contract: ContractInputWithId): Reservation = Reservation(
            id = UUID.randomUUID().toString(),
            contract_id = contract.id,
            from_date = from_date,
            to_date = to_date,
            activated_on = null
    )

    fun createLoan(l: LoanCreateInput): LoanOutput {
        // TODO: perform inventory checks
        // TODO: perform inventory availability checks
        // TODO: perform reservation checks
        // create contact for loan
        val contract = contractService.create(l.convertToContract())

        // setup the loan and create inventory mutations
        val loan = loanService.create(l, contract.id)
        val successfullyCreatedMutations = inventoryService.createMutationsForLoan(l, loan.id)
        if (!successfullyCreatedMutations) {
            throw BadRequestException("something went wrong while creating the mutations for the loan")
        }
        return loanService.readById(loan.id)
    }

    fun createReservation(r: ReservationInput): Boolean {
        // TODO: perform inventory checks
        // TODO: perform inventory availability checks
        // TODO: perform reservation collision checks
        val contract = contractService.create(r.convert())
        val reservation = r.convert(contract)
        return reservationService.create(reservation)
    }
}