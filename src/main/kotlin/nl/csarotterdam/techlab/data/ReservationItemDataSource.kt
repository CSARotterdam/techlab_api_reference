package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.reservationItem
import nl.csarotterdam.techlab.model.db.ReservationItem
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class ReservationItemDataSource : DataSource<ReservationItem>() {

    override fun read(rs: ResultSet) = ReservationItem(
            item_id = rs.getString(1),
            reservation_id = rs.getString(2),
            inventory_id = rs.getString(3),
            amount = rs.getInt(4)
    )

    fun readById(itemId: String) = database.assertOneResult(database.executeQuery(
            query = config[reservationItem.readById],
            init = { it.setString(1, itemId) },
            map = this::read
    ))

    fun readByReservationId(reservationId: String) = database.executeQuery(
            query = config[reservationItem.readByReservationId],
            init = { it.setString(1, reservationId) },
            map = this::read
    )

    fun create(ri: ReservationItem) = database.execute(
            query = config[reservationItem.create],
            init = {
                ri.run {
                    it.setString(1, item_id)
                    it.setString(2, reservation_id)
                    it.setString(3, inventory_id)
                    it.setInt(4, amount)
                }
            }
    )
}