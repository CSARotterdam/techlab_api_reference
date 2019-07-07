package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.InventoryDataSource
import nl.csarotterdam.techlab.data.InventoryMutationDataSource
import nl.csarotterdam.techlab.model.*
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import org.springframework.stereotype.Component
import java.util.*

@Component
class InventoryService(
        private val inventoryDataSource: InventoryDataSource,
        private val inventoryMutationDataSource: InventoryMutationDataSource,

        private val authService: AuthService
) {

    object Mutation {
        fun getLoanMutations(loan: LoanCreateInput, loanId: String): List<InventoryMutationInput> = loan.items.map {
            InventoryMutationInput(
                    inventory_id = it.inventory_id,
                    type = InventoryMutationType.ADD,
                    subtype = InventoryMutationSubtype.LOAN,
                    loan_id = loanId,
                    amount = it.amount
            )
        }
    }

    private fun Inventory.convert(): InventoryOutput = InventoryOutput(
            id = id,
            name = name,
            manufacturer = manufacturer,
            category = category,
            loan_time_in_days = loan_time_in_days
    )

    private fun InventoryMutation.convert(): InventoryMutationOutput = InventoryMutationOutput(
            mutation_id = mutation_id,
            inventory = readInventoryById(inventory_id),
            type = type,
            subtype = subtype,
            loan_id = loan_id,
            amount = amount,
            time = time
    )

    private fun InventoryInput.convert(): Pair<Inventory, InventoryMutationInput> {
        val inventory = Inventory(
                id = UUID.randomUUID().toString(),
                name = name,
                manufacturer = manufacturer,
                category = category,
                loan_time_in_days = loan_time_in_days
        )
        val mutation = InventoryMutationInput(
                inventory_id = inventory.id,
                type = InventoryMutationType.ADD,
                subtype = InventoryMutationSubtype.STOCK,
                loan_id = null,
                amount = this.initial_stock_size
        )
        return inventory to mutation
    }

    fun listInventory() = inventoryDataSource.list()
            .map { it.convert() }

    fun listMutations(token: String) = authService.authenticate(token, AccountPrivilege.READ) {
        inventoryMutationDataSource.list().map { it.convert() }
    }

    fun readInventoryById(id: String) = inventoryDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("inventory with id '$id' not found")

    fun searchInventoryByName(name: String) = inventoryDataSource.searchByName(name)
            .map { it.convert() }

    fun searchInventoryByCategory(category: InventoryCategory) = inventoryDataSource.searchByCategory(category)
            .map { it.convert() }

    fun getInventoryCategories() = InventoryCategory.values()

    fun readMutationById(token: String, mutationId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        inventoryMutationDataSource.readById(mutationId)?.convert()
                ?: throw NotFoundException("inventory mutation with id '$mutationId' not found")
    }

    fun readAllMutationsByInventoryId(token: String, inventoryId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        inventoryMutationDataSource
                .readAllByInventoryId(inventoryId)
                .map { it.convert() }
    }

    fun readAllMutationsByLoanId(token: String, loanId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        inventoryMutationDataSource.readAllByLoanId(loanId)
                .map { it.convert() }
    }

    fun createInventory(token: String, i: InventoryInput): Boolean = authService.authenticate(token, AccountPrivilege.ADMIN) {
        if (i.initial_stock_size < 0) {
            throw BadRequestException("initial stock size must be 0 or higher")
        }
        val (inventory, mutation) = i.convert()
        inventoryDataSource.create(inventory) && createMutation(token, mutation)
    }

    private fun createMutation(token: String, im: InventoryMutationInput) = authService.authenticate(token, AccountPrivilege.WRITE) {
        inventoryMutationDataSource.create(im)
    }

    fun createMutationsForLoan(token: String, l: LoanCreateInput, id: String): Boolean = authService.authenticate(token, AccountPrivilege.WRITE) {
        var successful = true
        val inventoryMutations = Mutation.getLoanMutations(l, id)
        inventoryMutations.forEach { mutation ->
            successful = successful && createMutation(token, mutation)
        }
        successful
    }

    fun updateInventory(token: String, i: Inventory) = authService.authenticate(token, AccountPrivilege.ADMIN) {
        inventoryDataSource.update(i)
    }
}