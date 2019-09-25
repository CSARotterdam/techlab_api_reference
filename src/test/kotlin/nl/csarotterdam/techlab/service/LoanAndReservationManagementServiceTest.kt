package nl.csarotterdam.techlab.service

import junit.framework.TestCase.assertEquals
import nl.csarotterdam.techlab.BaseTest
import nl.csarotterdam.techlab.model.auth.AccountRole
import nl.csarotterdam.techlab.model.db.*
import nl.csarotterdam.techlab.model.inventory.InventoryCategory
import nl.csarotterdam.techlab.model.inventory.InventoryCategoryOutput
import nl.csarotterdam.techlab.model.inventory.InventoryInfo
import nl.csarotterdam.techlab.model.management.*
import nl.csarotterdam.techlab.util.TimeUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.sql.Date
import java.sql.Timestamp
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class LoanAndReservationManagementServiceTest : BaseTest() {

    @Autowired
    private lateinit var managementService: LoanAndReservationManagementService

    @MockBean
    private lateinit var loanService: LoanService

    @MockBean
    private lateinit var reservationService: ReservationService

    @MockBean
    private lateinit var inventoryService: InventoryService

    @MockBean
    private lateinit var contractService: ContractService

    data class TestDataVerifyLoan(
            val message: String,
            val input: LoanCreateInput,
            val inventories: List<InventoryOutput>,
            val inventoriesInfo: List<InventoryInfo>,
            val activeLoans: List<LoanOutput>,
            val currentReservations: List<ReservationOutput>,
            val output: List<ManagementItem>
    )

    @ParameterizedTest
    @MethodSource("verifyLoanData")
    fun verifyLoan(data: TestDataVerifyLoan) {
        `when`(inventoryService.readInventoryById(anyString()))
                .thenAnswer {
                    val inventoryId = it.arguments[0] as String
                    requireNotNull(data.inventories.find { inv -> inv.id == inventoryId })
                }
        `when`(inventoryService.readInventoryInfoByInventoryId(anyString(), anyString()))
                .thenAnswer {
                    val inventoryId = it.arguments[1] as String
                    requireNotNull(data.inventoriesInfo.find { inv -> inv.inventory_id == inventoryId })
                }
        `when`(loanService.readAllActiveLoans(anyString()))
                .thenReturn(data.activeLoans)
        `when`(reservationService.listCurrent(anyString()))
                .thenReturn(data.currentReservations)

        val result = managementService.verifyLoan(token, data.input)
        assertEquals(data.message, data.output.toString(), result.toString())
    }

    @Test
    fun verifyReservation() {
    }

    companion object {

        private fun loan(items: List<InventoryItemInput>, return_date: Date) = LoanCreateInput(
                account_id = "account_id",
                user_id = "user_id",
                items = items,
                return_date = return_date
        )

        private fun inventory(id: String, loan_time_in_days: Int) = InventoryOutput(
                id = id,
                name = "${id}_name",
                manufacturer = "${id}_manufacturer",
                category = InventoryCategoryOutput(InventoryCategory.GAME),
                loan_time_in_days = loan_time_in_days
        )

        private fun validContract() = ContractOutput(
                id = "id",
                account = AccountOutput(
                        id = "id",
                        user = UserOutput(
                                id = "id",
                                code = "code",
                                mail = "mail",
                                mobile_number = "mobile_number",
                                name = "name"
                        ),
                        username = "username",
                        role = AccountRole.ADMIN,
                        active = true
                ),
                user = UserOutput(
                        id = "user_id",
                        code = "user_code",
                        mail = "user_mail",
                        mobile_number = "user_mobile_number",
                        name = "user_name"
                ),
                created_time = Timestamp.from(TimeUtils.currentDate()),
                signed_by_user_on = null,
                signed_by_account_on = null
        )

        @JvmStatic
        fun verifyLoanData(): Stream<TestDataVerifyLoan> = Stream.of(
                TestDataVerifyLoan(
                        message = "empty items list",
                        input = loan(
                                items = emptyList(),
                                return_date = Date(TimeUtils.currentDate().toEpochMilli())
                        ),
                        inventories = emptyList(),
                        inventoriesInfo = emptyList(),
                        activeLoans = emptyList(),
                        currentReservations = emptyList(),
                        output = listOf(
                                ManagementItem(
                                        info = InfoDefaultItemsEmpty(),
                                        inventory_id = null
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "id1 and id2 have invalid amounts, id3's amount is valid",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = -1
                                        ),
                                        InventoryItemInput(
                                                inventory_id = "id2",
                                                amount = 0
                                        ),
                                        InventoryItemInput(
                                                inventory_id = "id3",
                                                amount = 1
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 0),
                                inventory("id2", 0),
                                inventory("id3", 0)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 1, 0, 0),
                                InventoryInfo("id2", 1, 0, 0),
                                InventoryInfo("id3", 1, 0, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = emptyList(),
                        output = listOf(
                                ManagementItem(
                                        info = InfoDefaultItemNoAmount(),
                                        inventory_id = "id1"
                                ),
                                ManagementItem(
                                        info = InfoDefaultItemNoAmount(),
                                        inventory_id = "id2"
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "invalid return date",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 2
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().minus(2, ChronoUnit.DAYS).toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 0)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 1, 1, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = emptyList(),
                        output = listOf(
                                ManagementItem(
                                        info = InfoLoanReturnDate(),
                                        inventory_id = null
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "loan time warning",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 1
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().plus(2, ChronoUnit.DAYS).toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 1)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 1, 1, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = emptyList(),
                        output = listOf(
                                ManagementItem(
                                        info = InfoInventoryLoanTime(
                                                given_loan_time = 2,
                                                expected_loan_time = 1
                                        ),
                                        inventory_id = "id1"
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "inventory item not available",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 2
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 0)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 1, 0, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = emptyList(),
                        output = listOf(
                                ManagementItem(
                                        info = InfoInventoryNotAvailable(
                                                given_amount = 2,
                                                available_slots = emptyList()
                                        ),
                                        inventory_id = "id1"
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "loan valid, reservation is placed on next day",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 2
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 0)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 2, 0, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = listOf(
                                ReservationOutput(
                                        id = "r_id",
                                        contract = validContract(),
                                        items = listOf(
                                                ReservationItemOutput(
                                                        item_id = "item_id1",
                                                        inventory = inventory("id1", 0),
                                                        amount = 1
                                                )
                                        ),
                                        from_date = Date(TimeUtils.currentDate().plus(1, ChronoUnit.DAYS).toEpochMilli()),
                                        to_date = Date(TimeUtils.currentDate().plus(1, ChronoUnit.DAYS).toEpochMilli()),
                                        activated_on = null,
                                        deleted_on = null
                                )
                        ),
                        output = emptyList()
                ),
                TestDataVerifyLoan(
                        message = "loan invalid, reservation is placed on return date",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 2
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().plus(1, ChronoUnit.DAYS).toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 1)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 2, 0, 0)
                        ),
                        activeLoans = emptyList(),
                        currentReservations = listOf(
                                ReservationOutput(
                                        id = "r_id",
                                        contract = validContract(),
                                        items = listOf(
                                                ReservationItemOutput(
                                                        item_id = "item_id1",
                                                        inventory = inventory("id1", 1),
                                                        amount = 1
                                                )
                                        ),
                                        from_date = Date(TimeUtils.currentDate().plus(1, ChronoUnit.DAYS).toEpochMilli()),
                                        to_date = Date(TimeUtils.currentDate().plus(4, ChronoUnit.DAYS).toEpochMilli()),
                                        activated_on = null,
                                        deleted_on = null
                                )
                        ),
                        output = listOf(
                                ManagementItem(
                                        info = InfoInventoryReserved(
                                                given_amount = 2,
                                                available_slots = listOf(
                                                        InventoryNotAvailableSlot(
                                                                stock_amount = 2,
                                                                from_date = Date(TimeUtils.currentDate().toEpochMilli()),
                                                                to_date = Date(TimeUtils.currentDate().toEpochMilli())
                                                        )
                                                )
                                        ),
                                        inventory_id = "id1"
                                )
                        )
                ),
                TestDataVerifyLoan(
                        message = "inventory item available, because loan is delivered today",
                        input = loan(
                                items = listOf(
                                        InventoryItemInput(
                                                inventory_id = "id1",
                                                amount = 1
                                        )
                                ),
                                return_date = Date(TimeUtils.currentDate().plus(14, ChronoUnit.DAYS).toEpochMilli())
                        ),
                        inventories = listOf(
                                inventory("id1", 14)
                        ),
                        inventoriesInfo = listOf(
                                InventoryInfo("id1", 0, 1, 0)
                        ),
                        activeLoans = listOf(
                                LoanOutput(
                                        id = "id",
                                        contract = validContract(),
                                        items = listOf(
                                                LoanInventoryItem(
                                                        inventory = InventoryOutput(
                                                                id = "id1",
                                                                name = "name",
                                                                manufacturer = "manufacturer",
                                                                category = InventoryCategoryOutput(InventoryCategory.GAME),
                                                                loan_time_in_days = 14
                                                        ),
                                                        amount = 1
                                                )
                                        ),
                                        return_date = Date(TimeUtils.currentDate().plus(2, ChronoUnit.DAYS).toEpochMilli()),
                                        returned_on = null
                                )
                        ),
                        currentReservations = listOf(
                                ReservationOutput(
                                        id = "r_id",
                                        contract = validContract(),
                                        items = listOf(
                                                ReservationItemOutput(
                                                        item_id = "item_id1",
                                                        inventory = inventory("id1", 14),
                                                        amount = 1
                                                )
                                        ),
                                        from_date = Date(TimeUtils.currentDate().plus(5, ChronoUnit.DAYS).toEpochMilli()),
                                        to_date = Date(TimeUtils.currentDate().plus(10, ChronoUnit.DAYS).toEpochMilli()),
                                        activated_on = null,
                                        deleted_on = null
                                )
                        ),
                        output = listOf(
                                ManagementItem(
                                        info = InfoInventoryNotAvailable(
                                                given_amount = 1,
                                                available_slots = listOf(
                                                        InventoryNotAvailableSlot(
                                                                stock_amount = 1,
                                                                from_date = Date(TimeUtils.currentDate().plus(3, ChronoUnit.DAYS).toEpochMilli()),
                                                                to_date = Date(TimeUtils.currentDate().plus(14, ChronoUnit.DAYS).toEpochMilli())
                                                        )
                                                )
                                        ),
                                        inventory_id = "id1"
                                ),
                                ManagementItem(
                                        info = InfoInventoryReserved(
                                                given_amount = 1,
                                                available_slots = listOf(
                                                        InventoryNotAvailableSlot(
                                                                stock_amount = 1,
                                                                from_date = Date(TimeUtils.currentDate().plus(3, ChronoUnit.DAYS).toEpochMilli()),
                                                                to_date = Date(TimeUtils.currentDate().plus(4, ChronoUnit.DAYS).toEpochMilli())
                                                        ),
                                                        InventoryNotAvailableSlot(
                                                                stock_amount = 1,
                                                                from_date = Date(TimeUtils.currentDate().plus(11, ChronoUnit.DAYS).toEpochMilli()),
                                                                to_date = Date(TimeUtils.currentDate().plus(14, ChronoUnit.DAYS).toEpochMilli())
                                                        )
                                                )
                                        ),
                                        inventory_id = "id1"
                                )
                        )
                )
        )
    }
}