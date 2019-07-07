package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import nl.csarotterdam.techlab.model.db.*
import nl.csarotterdam.techlab.model.inventory.InventoryInfo
import nl.csarotterdam.techlab.model.management.*
import nl.csarotterdam.techlab.model.misc.BadRequestException
import nl.csarotterdam.techlab.util.TimeUtils
import nl.csarotterdam.techlab.util.toSQLInstant
import org.springframework.stereotype.Component
import java.sql.Date
import java.time.Instant
import java.util.*

@Component
class LoanAndReservationManagementService(
        private val loanService: LoanService,
        private val reservationService: ReservationService,

        private val inventoryService: InventoryService,
        private val contractService: ContractService,
        private val authService: AuthService
) {

    private fun LoanCreateInput.convertToContract(): ContractInput = ContractInput(
            account_id = account_id,
            user_id = user_id
    )

    private fun ReservationInput.convert(): ContractInput = ContractInput(
            account_id = account_id,
            user_id = user_id
    )

    private fun ReservationInput.convert(contract: ContractInputWithId): Reservation = Reservation(
            id = UUID.randomUUID().toString(),
            contract_id = contract.id,
            from_date = from_date,
            to_date = to_date,
            activated_on = null,
            deleted_on = null
    )

    private fun improveLoan(l: LoanCreateInput) = l.run {
        LoanCreateInput(
                account_id = account_id,
                user_id = user_id,
                items = groupInventoryItems(items),
                return_date = return_date
        )
    }

    private fun improveReservation(r: ReservationInput) = r.run {
        ReservationInput(
                account_id = account_id,
                user_id = user_id,
                items = groupInventoryItems(items),
                from_date = from_date,
                to_date = to_date
        )
    }

    private fun groupInventoryItems(items: List<InventoryItemInput>): List<InventoryItemInput> = items
            .groupBy { it.inventory_id }
            .map { (inventory_id, groupedItems) ->
                val amount = groupedItems.sumBy { it.amount }
                InventoryItemInput(
                        inventory_id = inventory_id,
                        amount = amount
                )
            }

    private fun checkReturnDate(return_date: Date): ManagementItem? =
            if (TimeUtils.betweenNowInDays(return_date) >= 0) null else {
                ManagementItem(
                        info = InfoLoanReturnDate(),
                        inventory_id = null
                )
            }

    private fun checkLoanTime(
            loanTimeInDays: Int,
            items: List<InventoryItemInput>,
            inventories: List<InventoryOutput>
    ): List<ManagementItem> = items
            .map { item ->
                val inventory = requireNotNull(inventories.find { it.id == item.inventory_id })
                inventory
            }
            .filter { inventory -> loanTimeInDays > inventory.loan_time_in_days }
            .map { inventory ->
                ManagementItem(
                        info = InfoInventoryLoanTime(
                                given_loan_time = loanTimeInDays,
                                expected_loan_time = inventory.loan_time_in_days
                        ),
                        inventory_id = inventory.id
                )
            }

    private fun checkInventoryAvailability(
            items: List<InventoryItemInput>,
            inventoriesInfo: List<InventoryInfo>
    ): List<ManagementItem> = items
            .map { item ->
                val inventoryId = item.inventory_id
                val inventoryInfo = requireNotNull(inventoriesInfo.find { it.inventory_id == inventoryId })
                item to inventoryInfo
            }
            .filter { (item, inventoryInfo) ->
                item.amount > inventoryInfo.stock_amount
            }
            .map { (item, inventoryInfo) ->
                ManagementItem(
                        info = InfoInventoryNotAvailable(
                                given_amount = item.amount,
                                inventory_info = inventoryInfo
                        ),
                        inventory_id = item.inventory_id
                )
            }

    private fun checkReservationCollisions(
            token: String,
            items: List<InventoryItemInput>,
            inventoryIds: List<String>,
            inventoriesInfo: List<InventoryInfo>,
            fromDate: Instant,
            toDate: Instant
    ): List<ManagementItem> {
        // get applicable reservations
        val reservations = reservationService.listCurrent(token)
                .filter {
                    val reservationFrom = it.from_date
                    val reservationTo = it.to_date

                    val before = TimeUtils.betweenInDays(toDate, reservationFrom.toInstant()) > 0
                    val after = TimeUtils.betweenInDays(reservationTo.toInstant(), fromDate) > 0

                    !before && !after
                }

        // get all reservation items
        val reservationItems = reservations.map { reservation ->
            reservation.items.filter { item -> item.inventory.id in inventoryIds }
        }.flatten()

        // group all reservations
        return reservationItems
                .groupBy { it.inventory.id }
                .mapNotNull { (inventoryId, items) ->
                    val reservedAmount = items.sumBy { item -> item.amount }

                    val item = requireNotNull(items.find { it.inventory.id == inventoryId })
                    val inventoryInfo = requireNotNull(inventoriesInfo.find { it.inventory_id == inventoryId })

                    val stockAmountExcludingReserved = inventoryInfo.stock_amount - reservedAmount

                    if (item.amount > stockAmountExcludingReserved) {
                        ManagementItem(
                                info = InfoInventoryReserved(
                                        given_amount = item.amount,
                                        stock_amount = inventoryInfo.stock_amount,
                                        reserved_amount = reservedAmount,
                                        stock_amount_excluding_reserved = stockAmountExcludingReserved
                                ),
                                inventory_id = inventoryId
                        )
                    } else null
                }
    }

    fun verifyLoan(token: String, l: LoanCreateInput): List<ManagementItem> = authService.authenticate(token, AccountPrivilege.WRITE) {
        val loan = improveLoan(l)

        val currentDate = TimeUtils.currentDate()
        val returnDate = l.return_date
        val items = loan.items

        val daysCheck = checkReturnDate(returnDate)
        if (daysCheck != null) {
            listOf(daysCheck)
        } else {
            val loanTimeInDays = TimeUtils.betweenNowInDays(returnDate).toInt()
            val inventoryIds = items.map { it.inventory_id }
            val inventories = inventoryIds.map { inventoryService.readInventoryById(it) }
            val inventoriesInfo = inventoryIds.map { inventoryService.readInventoryInfoByInventoryId(token, it) }

            // check for amount of days for loaning
            val loanTime = checkLoanTime(loanTimeInDays, items, inventories)

            // check if items are available in stock of inventory
            val inventoryAvailability = checkInventoryAvailability(items, inventoriesInfo)

            // check if reservations collide with this loan
            val reservationCollisions = checkReservationCollisions(
                    token = token,
                    items = items,
                    inventoryIds = inventoryIds,
                    inventoriesInfo = inventoriesInfo,
                    fromDate = currentDate,
                    toDate = returnDate.toSQLInstant()
            )

            listOf(
                    loanTime,
                    inventoryAvailability,
                    reservationCollisions
            ).flatten()
        }
    }

    fun verifyReservation(token: String, r: ReservationInput): List<ManagementItem> = authService.authenticate(token, AccountPrivilege.WRITE) {
        val reservation = improveReservation(r)

        // TODO: perform inventory checks
        // TODO: perform inventory availability checks
        // TODO: perform reservation collision checks

        emptyList()
    }

    fun createLoan(token: String, l: LoanCreateInput): LoanOutput = authService.authenticate(token, AccountPrivilege.WRITE) {
        val loanInput = improveLoan(l)
        if (verifyLoan(token, loanInput).any { it.info.type == ManagementType.ERROR }) {
            throw BadRequestException("loan can't be created")
        }

        // create contact for loan
        val contract = contractService.create(token, loanInput.convertToContract())

        // setup the loan and create inventory mutations
        val loan = loanService.create(token, loanInput, contract.id)
        val successfullyCreatedMutations = inventoryService.createMutationsForLoan(token, loanInput, loan.id)
        if (!successfullyCreatedMutations) {
            throw BadRequestException("something went wrong while creating the mutations for the loan")
        }
        loanService.readById(token, loan.id)
    }

    fun createReservation(token: String, r: ReservationInput): Boolean = authService.authenticate(token, AccountPrivilege.WRITE) {
        val reservationInput = improveReservation(r)
        if (verifyReservation(token, reservationInput).any { it.info.type == ManagementType.ERROR }) {
            throw BadRequestException("loan can't be created")
        }
        val contract = contractService.create(token, reservationInput.convert())
        val reservation = reservationInput.convert(contract)
        reservationService.create(token, reservation)
    }
}