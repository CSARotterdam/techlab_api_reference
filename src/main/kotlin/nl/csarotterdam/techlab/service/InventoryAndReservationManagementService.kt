package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.model.*
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import org.springframework.stereotype.Component
import java.util.*

@Component
class InventoryAndReservationManagementService(
        private val inventoryService: InventoryService,
        private val loanService: LoanService,
        private val reservationService: ReservationService,
        private val contractService: ContractService,
        private val authService: AuthService
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

    fun createLoan(token: String, l: LoanCreateInput): LoanOutput = authService.authenticate(token, AccountPrivilege.WRITE) {
        // TODO: perform inventory checks
        // TODO: perform inventory availability checks
        // TODO: perform reservation checks
        // create contact for loan
        val contract = contractService.create(token, l.convertToContract())

        // setup the loan and create inventory mutations
        val loan = loanService.create(token, l, contract.id)
        val successfullyCreatedMutations = inventoryService.createMutationsForLoan(token, l, loan.id)
        if (!successfullyCreatedMutations) {
            throw BadRequestException("something went wrong while creating the mutations for the loan")
        }
        loanService.readById(token, loan.id)
    }

    fun createReservation(token: String, r: ReservationInput): Boolean = authService.authenticate(token, AccountPrivilege.WRITE) {
        // TODO: perform inventory checks
        // TODO: perform inventory availability checks
        // TODO: perform reservation collision checks
        val contract = contractService.create(token, r.convert())
        val reservation = r.convert(contract)
        reservationService.create(token, reservation)
    }
}