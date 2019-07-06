package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.InventoryDataSource
import nl.csarotterdam.techlab.data.InventoryMutationDataSource
import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class InventoryService(
        private val inventoryDataSource: InventoryDataSource,
        private val inventoryMutationDataSource: InventoryMutationDataSource
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

    fun listMutations() = inventoryMutationDataSource.list()
            .map { it.convert() }

    fun readInventoryById(id: String) = inventoryDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("inventory with id '$id' not found")

    fun searchInventoryByName(name: String) = inventoryDataSource.searchByName(name)
            .map { it.convert() }

    fun searchInventoryByCategory(category: InventoryCategory) = inventoryDataSource.searchByCategory(category)
            .map { it.convert() }

    fun getInventoryCategories() = InventoryCategory.values()

    fun readMutationById(mutationId: String) = inventoryMutationDataSource.readById(mutationId)
            ?.convert() ?: throw NotFoundException("inventory mutation with id '$mutationId' not found")

    fun readAllMutationsByInventoryId(inventoryId: String) = inventoryMutationDataSource
            .readAllByInventoryId(inventoryId)
            .map { it.convert() }

    fun readAllMutationsByLoanId(loanId: String) = inventoryMutationDataSource.readAllByLoanId(loanId)
            .map { it.convert() }

    fun createInventory(i: InventoryInput): Boolean {
        if (i.initial_stock_size < 0) {
            throw BadRequestException("initial stock size must be 0 or higher")
        }
        val (inventory, mutation) = i.convert()
        return inventoryDataSource.create(inventory) && createMutation(mutation)
    }

    fun createMutation(im: InventoryMutationInput) = inventoryMutationDataSource.create(im)

    fun createMutationsForLoan(l: LoanCreateInput, id: String): Boolean {
        var successful = true
        val inventoryMutations = Mutation.getLoanMutations(l, id)
        inventoryMutations.forEach { mutation ->
            successful = successful && createMutation(mutation)
        }
        return successful
    }

    fun updateInventory(i: Inventory) = inventoryDataSource.update(i)
}