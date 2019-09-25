package nl.csarotterdam.techlab.model.db

import nl.csarotterdam.techlab.model.auth.AccountRole
import nl.csarotterdam.techlab.model.inventory.InventoryCategory
import nl.csarotterdam.techlab.model.inventory.InventoryCategoryOutput
import nl.csarotterdam.techlab.model.inventory.InventoryMutationSubtype
import nl.csarotterdam.techlab.model.inventory.InventoryMutationType
import java.sql.Date
import java.sql.Timestamp

data class InventoryInput(
        val name: String,
        val manufacturer: String,
        val category: InventoryCategory,
        val loan_time_in_days: Int,
        val initial_stock_size: Int
)

data class InventoryItemInput(
        val inventory_id: String,
        val amount: Int
)

data class InventoryOutput(
        val id: String,
        val name: String,
        val manufacturer: String,
        val category: InventoryCategoryOutput,
        val loan_time_in_days: Int
)

data class InventoryMutationInput(
        val inventory_id: String,
        val type: InventoryMutationType,
        val subtype: InventoryMutationSubtype,
        val loan_id: String?,
        val amount: Int
)

data class InventoryMutationOutput(
        val mutation_id: String,
        val inventory: InventoryOutput,
        val type: InventoryMutationType,
        val subtype: InventoryMutationSubtype,
        val loan_id: String?,
        val amount: Int,
        val time: Timestamp
)

data class UserInput(
        val code: String,
        val mail: String,
        val mobile_number: String,
        val name: String
)

data class UserOutput(
        val id: String,
        val code: String,
        val mail: String,
        val mobile_number: String,
        val name: String
)

data class ContractInput(
        val account_id: String,
        val user_id: String
)

data class ContractInputWithId(
        val id: String,
        val account_id: String,
        val user_id: String
)

data class ContractOutput(
        val id: String,
        val account: AccountOutput,
        val user: UserOutput,
        val created_time: Timestamp,
        val signed_by_account_on: Timestamp?,
        val signed_by_user_on: Timestamp?
)

data class LoanCreateInput(
        val account_id: String,
        val user_id: String,
        val items: List<InventoryItemInput>,
        val return_date: Date
)

data class LoanInventoryItem(
        val inventory: InventoryOutput,
        val amount: Int
)

data class LoanOutput(
        val id: String,
        val contract: ContractOutput,
        val items: List<LoanInventoryItem>,
        val return_date: Date,
        val returned_on: Timestamp?
)

data class AccountInput(
        val user_id: String,
        val username: String,
        val password: String,
        val role: AccountRole
)

data class AccountOutput(
        val id: String,
        val user: UserOutput,
        val username: String,
        val role: AccountRole,
        val active: Boolean
)

data class ReservationInput(
        val account_id: String,
        val user_id: String,
        val items: List<InventoryItemInput>,
        val from_date: Date,
        val to_date: Date
)

data class ReservationOutput(
        val id: String,
        val contract: ContractOutput,
        val items: List<ReservationItemOutput>,
        val from_date: Date,
        val to_date: Date,
        val activated_on: Timestamp?,
        val deleted_on: Timestamp?
)

data class ReservationItemOutput(
        val item_id: String,
        val inventory: InventoryOutput,
        val amount: Int
)

data class ReservationItemDatedOutput(
        val item_id: String,
        val inventory: InventoryOutput,
        val amount: Int,
        val from_date: Date,
        val to_date: Date
)