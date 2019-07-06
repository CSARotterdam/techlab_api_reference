package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.AccountDataSource
import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class AccountService(
        private val accountDataSource: AccountDataSource,

        private val userService: UserService
) {

    private fun Account.convert(): AccountOutput = AccountOutput(
            id = id,
            user = userService.readById(user_id),
            username = username,
            role = role,
            active = active
    )

    // TODO: encryption
    private fun AccountInput.convert(): Account {
        val salt = UUID.randomUUID().toString()
        val passwordHash = hashPassword(password, salt)
        return Account(
                id = UUID.randomUUID().toString(),
                user_id = user_id,
                username = username,
                passwordHash = passwordHash,
                salt = salt,
                role = role,
                active = true
        )
    }

    private fun hashPassword(password: String, salt: String): String {
        return password
    }

    fun listActive() = accountDataSource.listActive()
            .map { it.convert() }

    fun list() = accountDataSource.list()
            .map { it.convert() }

    fun readById(id: String) = accountDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("account with id '$id' not found")

    fun readByUsername(username: String) = accountDataSource.readByUsername(username)
            ?: throw NotFoundException("account with username '$username' not found")

    fun create(a: AccountInput) = accountDataSource.create(a.convert())

    fun setUsername(id: String, username: String) = accountDataSource.setUsername(id, username)

    fun setPassword(id: String, password: String): Boolean {
        val account = accountDataSource.readById(id)
                ?: throw NotFoundException("account with '$id' not found")

        val passwordHash = hashPassword(password, account.salt)
        return accountDataSource.setPassword(id, passwordHash)
    }

    fun setRole(id: String, role: AccountRole) = accountDataSource.setRole(id, role)

    fun setActive(id: String, active: Boolean) = accountDataSource.setActive(id, active)
}