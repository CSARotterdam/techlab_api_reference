package nl.csarotterdam.techlab.model.management

import nl.csarotterdam.techlab.model.management.ManagementSubtype.*
import nl.csarotterdam.techlab.model.management.ManagementType.ERROR
import nl.csarotterdam.techlab.model.management.ManagementType.WARNING
import java.sql.Date

data class ManagementItem(
        val info: ManagementInfo,
        val inventory_id: String?
)

enum class ManagementType {
    WARNING,
    ERROR
}

enum class ManagementSubtype {
    DEFAULT_ITEMS_EMPTY,
    DEFAULT_ITEM_NO_AMOUNT,

    LOAN_RETURN_DATE,

    RESERVATION_FROM_DATE,
    RESERVATION_TO_DATE,

    INVENTORY_LOAN_TIME,
    INVENTORY_NOT_AVAILABLE,
    INVENTORY_RESERVED
}

abstract class ManagementInfo(
        open val type: ManagementType,
        open val subtype: ManagementSubtype
)

data class InfoDefaultItemsEmpty(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = DEFAULT_ITEMS_EMPTY
) : ManagementInfo(type, subtype)

data class InfoDefaultItemNoAmount(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = DEFAULT_ITEM_NO_AMOUNT
) : ManagementInfo(type, subtype)

data class InfoLoanReturnDate(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = LOAN_RETURN_DATE
) : ManagementInfo(type, subtype)

data class InfoReservationFromDate(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = RESERVATION_FROM_DATE
) : ManagementInfo(type, subtype)

data class InfoReservationToDate(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = RESERVATION_TO_DATE
) : ManagementInfo(type, subtype)

data class InfoInventoryLoanTime(
        override val type: ManagementType = WARNING,
        override val subtype: ManagementSubtype = INVENTORY_LOAN_TIME,
        val given_loan_time: Int,
        val expected_loan_time: Int
) : ManagementInfo(type, subtype)

data class InfoInventoryNotAvailable(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = INVENTORY_NOT_AVAILABLE,
        val given_amount: Int,
        val available_slots: List<InventoryNotAvailableSlot>
) : ManagementInfo(type, subtype)

data class InfoInventoryReserved(
        override val type: ManagementType = ERROR,
        override val subtype: ManagementSubtype = INVENTORY_RESERVED,
        val given_amount: Int,
        val available_slots: List<InventoryNotAvailableSlot>
) : ManagementInfo(type, subtype)

data class InventoryNotAvailableSlot(
        val stock_amount: Int,
        val from_date: Date,
        val to_date: Date
)

data class InventoryNotAvailable(
        val is_available: Boolean,
        val available_slots: List<InventoryNotAvailableSlot>
)