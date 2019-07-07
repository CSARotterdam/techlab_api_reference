package nl.csarotterdam.techlab.model

import nl.csarotterdam.techlab.model.auth.AccountRole
import java.sql.Date
import java.sql.Timestamp

data class Inventory(
        val id: String,
        val name: String,
        val manufacturer: String,
        val category: InventoryCategory,
        val loan_time_in_days: Int
        // TODO: add extra information for books, etc.? => extra_info_id
)

data class InventoryMutation(
        val mutation_id: String,
        val inventory_id: String,
        val type: InventoryMutationType,
        val subtype: InventoryMutationSubtype,
        val loan_id: String?,
        val amount: Int,
        val time: Timestamp
)

data class User(
        val id: String,
        val code: String,
        val mail: String,
        val mobile_number: String,
        val name: String,
        val salt: String
)

data class Contract(
        val id: String,
        val account_id: String,
        val user_id: String,
        val created_time: Timestamp,
        val signed_by_account_on: Timestamp?,
        val signed_by_user_on: Timestamp?
        // extra info for account and user??
)

data class Loan(
        val id: String,
        val contract_id: String,
        val return_date: Date,
        val returned_on: Date?
)

data class Account(
        val id: String,
        val user_id: String,
        val username: String,
        val password_hash: String,
        val salt: String,
        val role: AccountRole,
        val active: Boolean
)

data class Reservation(
        val id: String,
        val contract_id: String,
        val from_date: Date,
        val to_date: Date,
        val activated_on: Timestamp?,
        val deleted_on: Timestamp?
)

data class ReservationItem(
        val item_id: String,
        val reservation_id: String,
        val inventory_id: String,
        val amount: Int
)