package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.contract
import nl.csarotterdam.techlab.model.Contract
import nl.csarotterdam.techlab.util.toResultOrNull
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class ContractDataSource : DataSource<Contract>() {

    override fun read(rs: ResultSet) = Contract(
            id = rs.getString(1),
            account_id = rs.getString(2),
            user_id = rs.getString(3),
            created_time = rs.getTimestamp(4),
            signed_by_account_on = rs.getTimestamp(5).toResultOrNull(rs),
            signed_by_user_on = rs.getTimestamp(6).toResultOrNull(rs)
    )

    fun readById(id: String) = database.assertOneResult(database.executeQuery(
            query = config[contract.readById],
            init = { it.setString(1, id) },
            map = this::read
    ))

    fun readByAccountId(accountId: String) = database.executeQuery(
            query = config[contract.readByAccountId],
            init = { it.setString(1, accountId) },
            map = this::read
    )

    fun readByUserId(userId: String) = database.executeQuery(
            query = config[contract.readByUserId],
            init = { it.setString(1, userId) },
            map = this::read
    )

    fun create(c: Contract) = database.execute(
            query = config[contract.create],
            init = {
                c.run {
                    it.setString(1, id)
                    it.setString(2, account_id)
                    it.setString(3, user_id)
                }
            }
    )

    fun signByAccount(id: String) = database.execute(
            query = config[contract.signByAccount],
            init = { it.setString(1, id) }
    )

    fun signByUser(id: String) = database.execute(
            query = config[contract.signByUser],
            init = { it.setString(1, id) }
    )
}