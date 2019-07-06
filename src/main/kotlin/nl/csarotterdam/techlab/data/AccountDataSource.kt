package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.account
import nl.csarotterdam.techlab.model.Account
import nl.csarotterdam.techlab.model.AccountRole
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class AccountDataSource : DataSource<Account>() {

    override fun read(rs: ResultSet) = Account(
            id = rs.getString(1),
            user_id = rs.getString(2),
            username = rs.getString(3),
            passwordHash = rs.getString(4),
            salt = rs.getString(5),
            role = AccountRole.valueOf(rs.getString(6)),
            active = rs.getBoolean(7)
    )

    fun listActive() = database.executeQuery(
            query = config[account.readAllActive],
            map = this::read
    )

    fun list() = database.executeQuery(
            query = config[account.readAll],
            map = this::read
    )

    fun readById() = database.assertOneResult(database.executeQuery(
            query = config[account.readById],
            map = this::read
    ))

    fun create(a: Account) = database.execute(
            query = config[account.create],
            init = {
                a.run {
                    it.setString(1, id)
                    it.setString(2, user_id)
                    it.setString(3, username)
                    it.setString(4, passwordHash)
                    it.setString(5, salt)
                    it.setString(6, role.toString())
                    it.setBoolean(7, active)
                }
            }
    )

    fun setUsername(id: String, username: String) = database.execute(
            query = config[account.setUsername],
            init = {
                it.setString(1, username)

                it.setString(2, id)
            }
    )

    fun setPassword(id: String, passwordHash: String) = database.execute(
            query = config[account.setPassword],
            init = {
                it.setString(1, passwordHash)

                it.setString(2, id)
            }
    )

    fun setRole(id: String, role: AccountRole) = database.execute(
            query = config[account.setRole],
            init = {
                it.setString(1, role.toString())

                it.setString(2, id)
            }
    )

    fun setActive(id: String, active: Boolean) = database.execute(
            query = config[account.setActive],
            init = {
                it.setBoolean(1, active)

                it.setString(2, id)
            }
    )
}