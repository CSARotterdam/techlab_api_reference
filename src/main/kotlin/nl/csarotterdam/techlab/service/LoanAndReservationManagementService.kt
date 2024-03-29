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

    /**
     * Improves the [LoanCreateInput] by grouping inventory items by their [InventoryItemInput.inventory_id]
     */
    private fun improveLoan(l: LoanCreateInput) = l.run {
        LoanCreateInput(
                account_id = account_id,
                user_id = user_id,
                items = groupInventoryItems(items),
                return_date = return_date
        )
    }

    /**
     * Improves the [ReservationInput] by grouping inventory items by their [InventoryItemInput.inventory_id]
     */
    private fun improveReservation(r: ReservationInput) = r.run {
        ReservationInput(
                account_id = account_id,
                user_id = user_id,
                items = groupInventoryItems(items),
                from_date = from_date,
                to_date = to_date
        )
    }

    /**
     * Correct inventory info by adding inventory items to stock if reservation ended
     */
    private fun improveInventoryInfo(
            date: Instant,
            inventoryInfo: InventoryInfo,
            loans: List<LoanOutput>
    ): InventoryInfo {
        var stockAmount = inventoryInfo.stock_amount
        var loanedAmount = inventoryInfo.loaned_amount

        loans.forEach { loan ->
            val returnedBefore = TimeUtils.betweenInDays(loan.return_date.toSQLInstant(), date) > 0
            if (returnedBefore) {
                stockAmount++
                loanedAmount--
            }
        }

        return InventoryInfo(
                inventory_id = inventoryInfo.inventory_id,
                stock_amount = stockAmount,
                loaned_amount = loanedAmount,
                broken_amount = inventoryInfo.broken_amount
        )
    }

    /**
     * Groups the given inventory [items]
     */
    private fun groupInventoryItems(items: List<InventoryItemInput>): List<InventoryItemInput> = items
            .groupBy { it.inventory_id }
            .map { (inventory_id, groupedItems) ->
                val amount = groupedItems.sumBy { it.amount }
                InventoryItemInput(
                        inventory_id = inventory_id,
                        amount = amount
                )
            }

    /**
     * Checks the [return_date] for at least being somewhere in the future
     */
    private fun checkReturnDate(return_date: Instant): ManagementItem? =
            if (TimeUtils.betweenNowInDays(return_date) >= 0) null else {
                ManagementItem(
                        info = InfoLoanReturnDate(),
                        inventory_id = null
                )
            }

    private fun checkReservationFromDate(fromDate: Instant): ManagementItem? =
            if (TimeUtils.betweenNowInDays(fromDate) >= 0) null else {
                ManagementItem(
                        info = InfoReservationFromDate(),
                        inventory_id = null
                )
            }

    private fun checkReservationToDate(fromDate: Instant, toDate: Instant): ManagementItem? =
            if (TimeUtils.betweenInDays(fromDate, toDate) >= 0) null else {
                ManagementItem(
                        info = InfoReservationToDate(),
                        inventory_id = null
                )
            }

    /**
     * Checks the loan time
     *
     * @param loanTimeInDays
     * @param items of the loan or reservation
     * @param inventories all inventory items that are requested for loan or reservation
     */
    private fun checkLoanTime(
            loanTimeInDays: Int,
            items: List<InventoryItemInput>,
            inventories: List<InventoryOutput>
    ): List<ManagementItem> = items
            .map { item ->
                // map the requested items to inventory representations
                val inventory = requireNotNull(inventories.find { it.id == item.inventory_id })
                inventory
            }
            // which inventory items can't be loaned this long
            .filter { inventory -> loanTimeInDays > inventory.loan_time_in_days }
            .map { inventory ->
                // create error
                ManagementItem(
                        info = InfoInventoryLoanTime(
                                given_loan_time = loanTimeInDays,
                                expected_loan_time = inventory.loan_time_in_days
                        ),
                        inventory_id = inventory.id
                )
            }

    /**
     * Returns the available time slots for the inventory to be loaned
     */
    private fun getInventoryNotAvailableSlots(
            item: InventoryItemInput,
            inventoryInfo: InventoryInfo,
            fromDate: Instant,
            toDate: Instant,
            reservedItems: List<ReservationItemDatedOutput>,
            loans: List<LoanOutput>
    ): InventoryNotAvailable {
        // setup
        val days = TimeUtils.getAllDaysInclusive(fromDate, toDate)
        val availableDays = days.toMutableList()
        val availableStock = mutableListOf<Int>()

        // check for every day from fromDate to and including toDate
        for (day in days) {
            // get all reserved items that are reserved on the current day
            val reservedItemsForDay = reservedItems
                    .filter {
                        val reservationFrom = it.from_date
                        val reservationTo = it.to_date

                        val beforeEnd = TimeUtils.betweenInDays(day, reservationTo.toSQLInstant()) >= 0
                        val afterStart = TimeUtils.betweenInDays(reservationFrom.toSQLInstant(), day) >= 0

                        beforeEnd && afterStart
                    }

            // get total reserved amount
            val reservedAmount = reservedItemsForDay.sumBy { it.amount }

            // improve for returned reservations in the future
            val inventoryInfoAfterLoans = improveInventoryInfo(day, inventoryInfo, loans)

            // get the indicative stock amount of the current day
            val stockAmountExcludingReserved = inventoryInfoAfterLoans.stock_amount - reservedAmount
            availableStock.add(stockAmountExcludingReserved)

            // if amount is greater, inventory item can't be loaned on this day
            if (item.amount > stockAmountExcludingReserved) {
                availableDays.remove(day)
            }
        }

        val slots = mutableListOf<InventoryNotAvailableSlot>()

        if (days != availableDays) {
            fun add(dates: MutableList<Date>) {
                val from = dates.first()
                val to = dates.last()

                // get the minimum stock amount for a given period
                val stockAmount = requireNotNull(days.withIndex()
                        .filter { (_, day) -> day in availableDays }
                        .map { availableStock[it.index] }
                        .min())
                slots.add(InventoryNotAvailableSlot(
                        stock_amount = stockAmount,
                        from_date = from,
                        to_date = to
                ))

                dates.clear()
            }

            // group by date ranges
            val dates = mutableListOf<Date>()
            for (day in days) {
                val date = Date(day.toEpochMilli())
                if (day in availableDays) {
                    dates.add(date)
                } else if (dates.isNotEmpty()) {
                    add(dates)
                }
            }

            if (dates.isNotEmpty()) {
                add(dates)
            }
        }

        return InventoryNotAvailable(
                is_available = availableDays.isNotEmpty(),
                available_slots = slots.toList()
        )
    }

    /**
     * Checks if an inventory item is available
     *
     * @param fromDate
     * @param toDate
     * @param items given items
     * @param inventoriesInfo inventory items info, indicating amount in stock, loaned and broken
     */
    private fun checkInventoryAvailability(
            fromDate: Instant,
            toDate: Instant,
            items: List<InventoryItemInput>,
            inventoriesInfo: List<InventoryInfo>,
            loans: List<LoanOutput>
    ): List<ManagementItem> = items
            .map { item ->
                // map the requested item to it's inventory info
                val inventoryId = item.inventory_id
                val inventoryInfo = requireNotNull(inventoriesInfo.find { it.inventory_id == inventoryId })
                item to inventoryInfo
            }
            .mapNotNull { (item, inventoryInfo) ->
                val inventoryAvailability = getInventoryNotAvailableSlots(
                        item = item,
                        inventoryInfo = inventoryInfo,
                        fromDate = fromDate,
                        toDate = toDate,
                        reservedItems = emptyList(),
                        loans = loans
                )

                // return inventory availability
                if (inventoryAvailability.available_slots.isNotEmpty() || !inventoryAvailability.is_available) {
                    ManagementItem(
                            info = InfoInventoryNotAvailable(
                                    given_amount = item.amount,
                                    available_slots = inventoryAvailability.available_slots
                            ),
                            inventory_id = item.inventory_id
                    )
                } else null
            }

    /**
     * Checks for collisions between reservations
     */
    private fun checkReservationCollisions(
            token: String,
            items: List<InventoryItemInput>,
            inventoryIds: List<String>,
            inventoriesInfo: List<InventoryInfo>,
            fromDate: Instant,
            toDate: Instant,
            loans: List<LoanOutput>
    ): List<ManagementItem> {
        // get applicable reservations
        val reservations = reservationService.listCurrent(token)
                .filter {
                    val reservationFrom = it.from_date
                    val reservationTo = it.to_date

                    val before = TimeUtils.betweenInDays(reservationTo.toSQLInstant(), fromDate) > 0
                    val after = TimeUtils.betweenInDays(toDate, reservationFrom.toSQLInstant()) > 0

                    // if not before and not after, it collides with the loan, so it's applicable
                    !before && !after
                }

        // get all reservation items
        val reservationItems = reservations.map { reservation ->
            reservation.items
                    // get only the items of a reservation applicable to the request
                    .filter { item -> item.inventory.id in inventoryIds }
                    .map { item ->
                        ReservationItemDatedOutput(
                                item_id = item.item_id,
                                inventory = item.inventory,
                                amount = item.amount,
                                from_date = reservation.from_date,
                                to_date = reservation.to_date
                        )
                    }
        }.flatten()

        // group all reservations
        return reservationItems
                .groupBy { it.inventory.id }
                .mapNotNull { (inventoryId, reservedItems) ->
                    val item = requireNotNull(items.find { it.inventory_id == inventoryId })
                    val inventoryInfo = requireNotNull(inventoriesInfo.find { it.inventory_id == inventoryId })

                    // perform availability check
                    val inventoryAvailability = getInventoryNotAvailableSlots(
                            item = item,
                            inventoryInfo = inventoryInfo,
                            fromDate = fromDate,
                            toDate = toDate,
                            reservedItems = reservedItems,
                            loans = loans
                    )

                    // return reservation inventory availability
                    if (inventoryAvailability.available_slots.isNotEmpty() || !inventoryAvailability.is_available) {
                        ManagementItem(
                                info = InfoInventoryReserved(
                                        given_amount = item.amount,
                                        available_slots = inventoryAvailability.available_slots
                                ),
                                inventory_id = inventoryId
                        )
                    } else null
                }
    }

    /**
     * Verifies a loan or reservation
     *
     * @param token of the user
     * @param fromDate of the loan or reservation
     * @param toDate / return date of the loan and reservation
     * @param items in the loan or reservation
     */
    private fun verifyLoanOrReservation(
            token: String,
            fromDate: Instant,
            toDate: Instant,
            items: List<InventoryItemInput>
    ): List<ManagementItem> {
        // check for default errors, empty items list or items with invalid amount
        val defaultChecks = mutableListOf<ManagementItem>()
        if (items.isEmpty()) {
            defaultChecks.add(ManagementItem(
                    info = InfoDefaultItemsEmpty(),
                    inventory_id = null
            ))
        }
        items.filter { it.amount <= 0 }.forEach { item ->
            defaultChecks.add(ManagementItem(
                    info = InfoDefaultItemNoAmount(),
                    inventory_id = item.inventory_id
            ))
        }

        // now we know the list is not empty and all amounts are >= 1

        // setup
        val loanTimeInDays = TimeUtils.betweenNowInDays(toDate).toInt()
        val inventoryIds = items.map { it.inventory_id }
        val inventories = inventoryIds.map { inventoryService.readInventoryById(it) }
        val inventoriesInfo = inventoryIds.map { inventoryService.readInventoryInfoByInventoryId(token, it) }
        val loans = loanService.readAllActiveLoans(token)

        // check for amount of days for loaning
        val loanTime = checkLoanTime(loanTimeInDays, items, inventories)

        // check if items are available in stock of inventory
        val inventoryAvailability = checkInventoryAvailability(
                fromDate = fromDate,
                toDate = toDate,
                items = items,
                inventoriesInfo = inventoriesInfo,
                loans = loans
        )

        // check if reservations collide with this loan
        val reservationCollisions = checkReservationCollisions(
                token = token,
                items = items,
                inventoryIds = inventoryIds,
                inventoriesInfo = inventoriesInfo,
                fromDate = fromDate,
                toDate = toDate,
                loans = loans
        )

        // combine all checks/errors
        return listOf(
                defaultChecks,
                loanTime,
                inventoryAvailability,
                reservationCollisions
        ).flatten()
    }

    /**
     * Verifies loan for a given [LoanCreateInput]
     *
     * @param token of the user
     * @param l input to create a loan
     */
    fun verifyLoan(token: String, l: LoanCreateInput): List<ManagementItem> = authService.authenticate(token, AccountPrivilege.WRITE) {
        val loan = improveLoan(l)

        // setup dates and items
        val currentDate = TimeUtils.currentDate()
        val returnDate = l.return_date.toSQLInstant()
        val items = loan.items

        val daysCheck = checkReturnDate(returnDate)
        if (daysCheck != null) {
            listOf(daysCheck)
        } else {
            verifyLoanOrReservation(
                    token = token,
                    fromDate = currentDate,
                    toDate = returnDate,
                    items = items
            )
        }
    }

    /**
     * Verifies a given reservation
     */
    fun verifyReservation(token: String, r: ReservationInput): List<ManagementItem> = authService.authenticate(token, AccountPrivilege.WRITE) {
        // improve reservation by grouping inventory items
        val reservation = improveReservation(r)

        // setup
        val fromDate = reservation.from_date.toSQLInstant()
        val toDate = reservation.to_date.toSQLInstant()
        val items = reservation.items

        // perform checks
        val fromCheck = checkReservationFromDate(fromDate)
        val toCheck = checkReservationToDate(fromDate, toDate)
        val fromToCheckList = listOfNotNull(fromCheck, toCheck)

        if (fromToCheckList.isNotEmpty()) {
            fromToCheckList
        } else {
            verifyLoanOrReservation(
                    token = token,
                    fromDate = fromDate,
                    toDate = toDate,
                    items = items
            )
        }
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
        inventoryService.createMutationsForLoan(token, loanInput, loan.id)
        loanService.readById(token, loan.id)
    }

    fun createReservation(token: String, r: ReservationInput) = authService.authenticate(token, AccountPrivilege.WRITE) {
        val reservationInput = improveReservation(r)
        if (verifyReservation(token, reservationInput).any { it.info.type == ManagementType.ERROR }) {
            throw BadRequestException("loan can't be created")
        }
        val contract = contractService.create(token, reservationInput.convert())
        val reservation = reservationInput.convert(contract)
        reservationService.create(token, reservation)
    }
}