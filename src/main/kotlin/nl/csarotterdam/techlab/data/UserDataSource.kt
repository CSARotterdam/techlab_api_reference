package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.user
import nl.csarotterdam.techlab.model.User
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class UserDataSource : DataSource<User>() {

    override fun read(rs: ResultSet) = User(
            id = rs.getString(1),
            code = rs.getString(2),
            mail = rs.getString(3),
            mobile_number = rs.getString(4),
            name = rs.getString(5)
    )

    fun readById(userId: String) = database.assertOneResult(database.executeQuery(
            query = config[user.readById],
            init = { it.setString(1, userId) },
            map = this::read
    ))

    fun readByCode(code: String) = database.executeQuery(
            query = config[user.readByCode],
            init = { it.setString(2, "%$code%") },
            map = this::read
    )

    fun create(usr: User) = database.execute(
            query = config[user.create],
            init = {
                usr.run {
                    it.setString(1, id)
                    it.setString(2, code)
                    it.setString(3, mail)
                    it.setString(4, mobile_number)
                    it.setString(5, name)
                }
            }
    )

    fun update(usr: User) = database.execute(
            query = config[user.update],
            init = {
                usr.run {
                    it.setString(1, code)
                    it.setString(2, mail)
                    it.setString(3, mobile_number)
                    it.setString(4, name)

                    it.setString(5, id)
                }
            }
    )
}