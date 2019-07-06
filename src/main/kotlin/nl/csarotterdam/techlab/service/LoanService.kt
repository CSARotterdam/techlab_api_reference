package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.LoanDataSource
import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class LoanService(
        private val loanDataSource: LoanDataSource,

        private val contractService: ContractService,
        private val inventoryService: InventoryService
) {

    private fun Loan.convert(): LoanOutput = LoanOutput(
            id = id,
            contract = contractService.readById(contract_id),
            items = inventoryService.readAllMutationsByLoanId(id)
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

    fun readAllActiveLoans() = loanDataSource.readAllActiveLoans()
            .map { it.convert() }

    fun readById(id: String) = loanDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("loan with id '$id' not found")

    fun readByContractId(contractId: String) = loanDataSource.readByContractId(contractId)
            ?.convert() ?: throw NotFoundException("loan with contract id '$contractId' not found")

    fun readActiveLoansByUserId(userId: String) = loanDataSource.readActiveLoansByUserId(userId)
            .map { it.convert() }

    fun readByUserId(userId: String) = loanDataSource.readByUserId(userId)
            .map { it.convert() }

    fun create(l: LoanCreateInput, contractId: String): Loan {
        val loan = l.convert(contractId)
        if (loanDataSource.create(loan)) {
            return loan
        } else {
            throw BadRequestException("something went wrong while creating the loan")
        }
    }

    fun setReturned(id: String) = loanDataSource.setReturned(id)
}