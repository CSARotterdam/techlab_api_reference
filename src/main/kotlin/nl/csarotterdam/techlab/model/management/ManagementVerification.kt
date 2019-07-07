package nl.csarotterdam.techlab.model.management

import nl.csarotterdam.techlab.model.inventory.InventoryInfo
import nl.csarotterdam.techlab.model.management.ManagementSubtype.*
import nl.csarotterdam.techlab.model.management.ManagementType.ERROR
import nl.csarotterdam.techlab.model.management.ManagementType.WARNING

data class ManagementItem(
        val info: ManagementInfo,
        val inventory_id: String?
)

enum class ManagementType {
    WARNING,
    ERROR
}

enum class ManagementSubtype {
    LOAN_RETURN_DATE,
    RESERVATION_FROM_DATE,
    RESERVATION_TO_DATE,
    INVENTORY_LOAN_TIME,
    INVENTORY_NOT_AVAILABLE,
    INVENTORY_RESERVED
}

abstract class ManagementInfo(
        val type: ManagementType,
        val subtype: ManagementSubtype
)

class InfoLoanReturnDate : ManagementInfo(ERROR, LOAN_RETURN_DATE)

class InfoReservationFromDate : ManagementInfo(ERROR, RESERVATION_FROM_DATE)
class InfoReservationToDate : ManagementInfo(ERROR, RESERVATION_TO_DATE)

class InfoInventoryLoanTime(
        val given_loan_time: Int,
        val expected_loan_time: Int
) : ManagementInfo(WARNING, INVENTORY_LOAN_TIME)

class InfoInventoryNotAvailable(
        val given_amount: Int,
        val inventory_info: InventoryInfo
) : ManagementInfo(ERROR, INVENTORY_NOT_AVAILABLE)

class InfoInventoryReserved(
        val given_amount: Int,
        val stock_amount: Int,
        val reserved_amount: Int,
        val stock_amount_excluding_reserved: Int
) : ManagementInfo(ERROR, INVENTORY_RESERVED)