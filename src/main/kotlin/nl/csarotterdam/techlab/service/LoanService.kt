package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.LoanDataSource
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import nl.csarotterdam.techlab.model.db.*
import nl.csarotterdam.techlab.model.inventory.InventoryMutationSubtype
import nl.csarotterdam.techlab.model.inventory.InventoryMutationType
import nl.csarotterdam.techlab.model.misc.NotFoundException
import org.springframework.stereotype.Component
import java.util.*

@Component
class LoanService(
        private val loanDataSource: LoanDataSource,

        private val contractService: ContractService,
        private val inventoryService: InventoryService,
        private val authService: AuthService
) {

    private fun Loan.convert(token: String): LoanOutput = LoanOutput(
            id = id,
            contract = contractService.readById(token, contract_id),
            items = inventoryService.readAllMutationsByLoanId(token, id)
                    .filter { it.type == InventoryMutationType.ADD && it.subtype == InventoryMutationSubtype.LOAN }
                    .map { it.convert() },
            return_date = return_date,
            returned_on = returned_on
    )

    private fun LoanCreateInput.convert(contractId: String): Loan = Loan(
            id = UUID.randomUUID().toString(),
            contract_id = contractId,
            return_date = return_date,
            returned_on = null
    )

    private fun InventoryMutationOutput.convert(): LoanInventoryItem = LoanInventoryItem(
            inventory = inventory,
            amount = amount
    )

    fun readAllActiveLoans(token: String) = authService.authenticate(token, AccountPrivilege.READ) {
        loanDataSource.readAllActiveLoans().map { it.convert(token) }
    }

    fun readById(token: String, id: String) = authService.authenticate(token, AccountPrivilege.READ) {
        loanDataSource.readById(id)?.convert(token)
                ?: throw NotFoundException("loan with id '$id' not found")
    }

    fun readByContractId(token: String, contractId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        loanDataSource.readByContractId(contractId)?.convert(token)
                ?: throw NotFoundException("loan with contract id '$contractId' not found")
    }

    fun readActiveLoansByUserId(token: String, userId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        loanDataSource.readActiveLoansByUserId(userId).map { it.convert(token) }
    }

    fun readByUserId(token: String, userId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        loanDataSource.readByUserId(userId).map { it.convert(token) }
    }

    fun create(token: String, l: LoanCreateInput, contractId: String): Loan = authService.authenticate(token, AccountPrivilege.WRITE) {
        val loan = l.convert(contractId)
        loanDataSource.create(loan)
        loan
    }

    fun setReturned(token: String, id: String) = authService.authenticate(token, AccountPrivilege.WRITE) {
        loanDataSource.setReturned(id)
    }
}