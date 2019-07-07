package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.ReservationDataSource
import nl.csarotterdam.techlab.data.ReservationItemDataSource
import nl.csarotterdam.techlab.model.*
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import org.springframework.stereotype.Component

@Component
class ReservationService(
        private val reservationDataSource: ReservationDataSource,
        private val reservationItemDataSource: ReservationItemDataSource,

        private val contractService: ContractService,
        private val inventoryService: InventoryService,
        private val authService: AuthService
) {

    private fun Reservation.convert(token: String): ReservationOutput = ReservationOutput(
            id = id,
            contract = contractService.readById(token, contract_id),
            items = readReservationItemsById(token, id),
            from_date = from_date,
            to_date = to_date,
            activated_on = activated_on
    )

    private fun ReservationItem.convert(): ReservationItemOutput = ReservationItemOutput(
            item_id = item_id,
            inventory = inventoryService.readInventoryById(inventory_id),
            amount = amount
    )

    fun listCurrent(token: String) = authService.authenticate(token, AccountPrivilege.READ) {
        reservationDataSource.listCurrent().map { it.convert(token) }
    }

    fun list(token: String) = authService.authenticate(token, AccountPrivilege.READ) {
        reservationDataSource.list().map { it.convert(token) }
    }

    fun readById(token: String, id: String) = authService.authenticate(token, AccountPrivilege.READ) {
        reservationDataSource.readById(id)?.convert(token)
                ?: throw NotFoundException("reservation with id '$id' not found")
    }

    fun readByUserId(token: String, userId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        reservationDataSource.readByUserId(userId).map { it.convert(token) }
    }

    fun create(token: String, reservation: Reservation) = authService.authenticate(token, AccountPrivilege.WRITE) {
        reservationDataSource.create(reservation)
    }

    fun setActivated(token: String, id: String): Boolean = authService.authenticate(token, AccountPrivilege.WRITE) {
        val reservation = readById(token, id)
        if (reservation.activated_on != null) {
            throw BadRequestException("this reservation is already used")
        }
        reservationDataSource.setActivated(id)
    }

    fun delete(token: String, id: String) = authService.authenticate(token, AccountPrivilege.WRITE) {
        reservationDataSource.delete(id)
    }

    private fun readReservationItemsById(token: String, id: String) = authService.authenticate(token, AccountPrivilege.READ) {
        reservationItemDataSource
                .readByReservationId(id)
                .map { it.convert() }
    }
}