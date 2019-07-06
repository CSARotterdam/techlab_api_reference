package nl.csarotterdam.techlab.model

import java.sql.Date
import java.sql.Timestamp

data class Inventory(
        val id: String,
        val name: String,
        val manufacturer: String,
        val category: InventoryCategory,
        val loan_time_in_days: Int
        // TODO: add extra information for books, etc.? => extra_info_id
) : Table("inventory")

data class InventoryMutation(
        val mutation_id: String,
        val inventory_id: String,
        val type: InventoryMutationType,
        val subtype: InventoryMutationSubtype,
        val loan_id: String?,
        val amount: Int,
        val time: Timestamp
) : Table("inventoryMutation")

data class User(
        val id: String,
        val code: String,
        val mail: String,
        val mobileNumber: String,
        val name: String
) : Table("user")

data class Contract(
        val id: String,
        val created_time: Timestamp,
        val account_id: String,
        val user_id: String,
        val signed_by_account_on: Timestamp?,
        val signed_by_user_on: Timestamp?
        // extra info for account and user??
)

data class Loan(
        val id: String,
        val user_id: String,
        val contract_id: String,
        val return_date: Date
) : Table("loan")

data class LoanArchive(
        val id: String,
        val user_id: String,
        val contract_id: String,
        val return_date: Date,
        val returned_on: Date
) : Table("loanArchive")

data class Account(
        val id: String,
        val username: String,
        val passwordHash: String,
        val salt: String,
        val role: AccountRole,
        val active: Boolean
) : Table("account")

data class Reservation(
        val id: String,
        val user_id: String,
        val contract_id: String,
        val from_date: Date,
        val to_date: Date
) : Table("reservation")

data class ReservationArchive(
        val id: String,
        val user_id: String,
        val contract_id: String,
        val from_date: Date,
        val to_date: Date,
        val activated_on: Date
) : Table("reservationArchive")

data class ReservationItem(
        val item_id: String,
        val reservation_id: String,
        val inventory_id: String,
        val amount: Int
) : Table("reservationItem")