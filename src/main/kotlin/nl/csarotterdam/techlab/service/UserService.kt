package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.UserDataSource
import nl.csarotterdam.techlab.model.NotFoundException
import nl.csarotterdam.techlab.model.User
import nl.csarotterdam.techlab.model.UserInput
import nl.csarotterdam.techlab.model.UserOutput
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserService(
        private val userDataSource: UserDataSource
) {

    private fun User.convert(): UserOutput = this.decrypt().run {
        UserOutput(
                id = id,
                code = code,
                mail = mail,
                mobile_number = mobile_number,
                name = name
        )
    }

    private fun UserInput.convert(): User = User(
            id = UUID.randomUUID().toString(),
            code = code,
            mail = mail,
            mobile_number = mobile_number,
            name = name
    ).encrypt()

    // TODO: encryption
    private fun User.encrypt(): User = User(
            id = id,
            code = code,
            mail = mail,
            mobile_number = mobile_number,
            name = name
    )

    // TODO: decryption
    private fun User.decrypt(): User = User(
            id = id,
            code = code,
            mail = mail,
            mobile_number = mobile_number + "2",
            name = name
    )

    fun readById(id: String) = userDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("user with id '$id' not found")

    fun readByCode(code: String) = userDataSource.readByCode(code)
            .map { it.convert() }

    fun create(u: UserInput) = userDataSource.create(u.convert())

    fun update(u: User) = userDataSource.update(u.encrypt())
}