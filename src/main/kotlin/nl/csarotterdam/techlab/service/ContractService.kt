package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.ContractDataSource
import nl.csarotterdam.techlab.model.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class ContractService(
        private val contractDataSource: ContractDataSource,

        private val accountService: AccountService,
        private val userService: UserService
) {

    private fun Contract.convert(): ContractOutput = ContractOutput(
            id = id,
            account = accountService.readById(account_id),
            user = userService.readById(user_id),
            created_time = created_time,
            signed_by_account_on = signed_by_account_on,
            signed_by_user_on = signed_by_user_on
    )

    private fun ContractInput.convert(): ContractInputWithId = ContractInputWithId(
            id = UUID.randomUUID().toString(),
            account_id = account_id,
            user_id = user_id
    )

    fun readById(id: String) = contractDataSource.readById(id)
            ?.convert() ?: throw NotFoundException("contract with id '$id' not found")

    fun readByAccountId(accountId: String) = contractDataSource.readByAccountId(accountId)
            .map { it.convert() }

    fun readByUserId(userId: String) = contractDataSource.readByUserId(userId)
            .map { it.convert() }

    fun create(c: ContractInput): ContractInputWithId {
        val account = accountService.readById(c.account_id)
        if (account.user.id == c.user_id) {
            throw BadRequestException("you can't sign your own form")
        }
        val contract = c.convert()
        if (contractDataSource.create(contract)) {
            return contract
        } else {
            throw BadRequestException("something went wrong while creating the contract")
        }
    }

    fun signByAccount(accountId: String) = contractDataSource.signByAccount(accountId)

    fun signByUser(userId: String) = contractDataSource.signByUser(userId)
}