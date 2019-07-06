package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.ReservationDataSource
import nl.csarotterdam.techlab.data.ReservationItemDataSource
import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component

@Component
class ReservationService(
        private val reservationDataSource: ReservationDataSource,
        private val reservationItemDataSource: ReservationItemDataSource,

        private val contractService: ContractService,
        private val inventoryService: InventoryService
) {

    private fun Reservation.convert(): ReservationOutput = ReservationOutput(
            id = id,
            contract = contractService.readById(contract_id),
            items = readReservationItemsById(id),
            from_date = from_date,
            to_date = to_date,
            activated_on = activated_on
    )

    private fun ReservationItem.convert(): ReservationItemOutput = ReservationItemOutput(
            item_id = item_id,
            inventory = inventoryService.readInventoryById(inventory_id),
            amount = amount
    )

    fun listCurrent() = reservationDataSource.listCurrent()
            .map { it.convert() }

    fun list() = reservationDataSource.list()
            .map { it.convert() }

    fun readById(id: String) = reservationDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("reservation with id '$id' not found")

    fun readByUserId(userId: String) = reservationDataSource.readByUserId(userId)
            .map { it.convert() }

    fun create(reservation: Reservation) = reservationDataSource.create(reservation)

    fun setActivated(id: String): Boolean {
        val reservation = readById(id)
        if (reservation.activated_on != null) {
            throw BadRequestException("this reservation is already used")
        }
        return reservationDataSource.setActivated(id)
    }

    fun delete(id: String) = reservationDataSource.delete(id)

    private fun readReservationItemsById(id: String) = reservationItemDataSource
            .readByReservationId(id)
            .map { it.convert() }
}