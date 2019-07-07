package nl.csarotterdam.techlab.service

import nl.csarotterdam.techlab.data.ContractDataSource
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import nl.csarotterdam.techlab.model.db.Contract
import nl.csarotterdam.techlab.model.db.ContractInput
import nl.csarotterdam.techlab.model.db.ContractInputWithId
import nl.csarotterdam.techlab.model.db.ContractOutput
import nl.csarotterdam.techlab.model.misc.BadRequestException
import nl.csarotterdam.techlab.model.misc.NotFoundException
import org.springframework.stereotype.Component
import java.util.*

@Component
class ContractService(
        private val contractDataSource: ContractDataSource,

        private val authService: AuthService
) {

    private fun Contract.convert(token: String): ContractOutput = ContractOutput(
            id = id,
            account = authService.readById(token, account_id),
            user = authService.readUserById(token, user_id),
            created_time = created_time,
            signed_by_account_on = signed_by_account_on,
            signed_by_user_on = signed_by_user_on
    )

    private fun ContractInput.convert(): ContractInputWithId = ContractInputWithId(
            id = UUID.randomUUID().toString(),
            account_id = account_id,
            user_id = user_id
    )

    fun readById(token: String, id: String) = authService.authenticate(token, AccountPrivilege.READ) {
        contractDataSource.readById(id)
                ?.convert(token) ?: throw NotFoundException("contract with id '$id' not found")
    }

    fun readByAccountId(token: String, accountId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        contractDataSource.readByAccountId(accountId)
                .map { it.convert(token) }
    }

    fun readByUserId(token: String, userId: String) = authService.authenticate(token, AccountPrivilege.READ) {
        contractDataSource.readByUserId(userId)
                .map { it.convert(token) }
    }

    fun create(token: String, c: ContractInput): ContractInputWithId = authService.authenticate(token, AccountPrivilege.WRITE) {
        val account = authService.readById(token, c.account_id)
        if (account.user.id == c.user_id) {
            throw BadRequestException("you can't sign your own form")
        }
        val contract = c.convert()
        if (contractDataSource.create(contract)) {
            contract
        } else {
            throw BadRequestException("something went wrong while creating the contract")
        }
    }

    fun signByAccount(token: String, accountId: String) = authService.authenticate(token, AccountPrivilege.WRITE) {
        contractDataSource.signByAccount(accountId)
    }

    fun signByUser(token: String, userId: String) = authService.authenticate(token, AccountPrivilege.UNAUTHORIZED_WRITE) {
        contractDataSource.signByUser(userId)
    }
}