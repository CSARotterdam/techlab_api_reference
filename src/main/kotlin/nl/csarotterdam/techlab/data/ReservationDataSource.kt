package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.reservation
import nl.csarotterdam.techlab.model.Reservation
import nl.csarotterdam.techlab.util.setValueCanBeNull
import nl.csarotterdam.techlab.util.toResultOrNull
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.Types

@Component
class ReservationDataSource : DataSource<Reservation>() {

    override fun read(rs: ResultSet) = Reservation(
            id = rs.getString(1),
            user_id = rs.getString(2),
            contract_id = rs.getString(3),
            from_date = rs.getDate(4),
            to_date = rs.getDate(5),
            activated_on = rs.getTimestamp(6).toResultOrNull(rs)
    )

    fun listCurrent() = database.executeQuery(
            query = config[reservation.readAllCurrent],
            map = this::read
    )

    fun list() = database.executeQuery(
            query = config[reservation.readAll],
            map = this::read
    )

    fun readById(id: String) = database.assertOneResult(database.executeQuery(
            query = config[reservation.readById],
            init = { it.setString(1, id) },
            map = this::read
    ))

    fun readByUserId(userId: String) = database.executeQuery(
            query = config[reservation.readByUserId],
            init = { it.setString(1, userId) },
            map = this::read
    )

    fun create(r: Reservation) = database.execute(
            query = config[reservation.create],
            init = {
                r.run {
                    it.setString(1, id)
                    it.setString(2, user_id)
                    it.setString(3, contract_id)
                    it.setDate(4, from_date)
                    it.setDate(5, to_date)
                    it.setValueCanBeNull(6, Types.TIMESTAMP, activated_on) { n, v -> it.setTimestamp(n, v) }
                }
            }
    )

    fun setActivated(id: String) = database.execute(
            query = config[reservation.setActivated],
            init = { it.setString(1, id) }
    )

    fun delete(id: String) = database.execute(
            query = config[reservation.delete],
            init = { it.setString(1, id) }
    )
}