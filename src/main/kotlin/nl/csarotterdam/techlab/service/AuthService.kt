package nl.csarotterdam.techlab.service

import com.natpryce.konfig.Configuration
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mu.KotlinLogging
import nl.csarotterdam.techlab.config.server
import nl.csarotterdam.techlab.data.AccountDataSource
import nl.csarotterdam.techlab.data.UserDataSource
import nl.csarotterdam.techlab.model.*
import nl.csarotterdam.techlab.model.auth.AccountPrivilege
import nl.csarotterdam.techlab.model.auth.AccountRole
import nl.csarotterdam.techlab.model.auth.Credentials
import nl.csarotterdam.techlab.model.auth.CredentialsToken
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class AuthService(
        private val accountDataSource: AccountDataSource,
        private val userDataSource: UserDataSource,

        config: Configuration
) {

    private val logger = KotlinLogging.logger { }

    private val secret = config[server.secret]

    private val loginToken = createInfiniteToken("LOGIN_TOKEN", AccountRole.VIEWER)

    private fun Account.convert(token: String): AccountOutput = AccountOutput(
            id = id,
            user = readUserById(token, user_id),
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

    fun listActive(token: String) = authenticate(token, AccountPrivilege.ADMIN) {
        accountDataSource.listActive().map { it.convert(token) }
    }

    fun list(token: String) = authenticate(token, AccountPrivilege.ADMIN) {
        accountDataSource.list().map { it.convert(token) }
    }

    fun readById(token: String, id: String) = authenticate(token, AccountPrivilege.READ) {
        accountDataSource.readById(id)?.convert(token)
                ?: throw NotFoundException("account with id '$id' not found")
    }

    fun create(token: String, a: AccountInput) = authenticate(token, AccountPrivilege.ADMIN) {
        accountDataSource.create(a.convert())
    }

    fun setUsername(token: String, id: String, username: String) = authenticate(token, AccountPrivilege.WRITE, id) {
        accountDataSource.setUsername(id, username)
    }

    fun setPassword(token: String, id: String, password: String): Boolean = authenticate(token, AccountPrivilege.WRITE, id) {
        val account = accountDataSource.readById(id)
                ?: throw NotFoundException("account with '$id' not found")

        val passwordHash = hashPassword(password, account.salt)
        accountDataSource.setPassword(id, passwordHash)
    }

    fun setRole(token: String, id: String, role: AccountRole) = authenticate(token, AccountPrivilege.ADMIN) {
        accountDataSource.setRole(id, role)
    }

    fun setActive(token: String, id: String, active: Boolean) = authenticate(token, AccountPrivilege.ADMIN) {
        accountDataSource.setActive(id, active)
    }

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

    fun readUserById(token: String, id: String) = authenticate(token, AccountPrivilege.READ) {
        userDataSource.readById(id)?.convert()
                ?: throw NotFoundException("user with id '$id' not found")
    }

    fun readUserByCode(token: String, code: String) = authenticate(token, AccountPrivilege.READ) {
        userDataSource.readByCode(code).map { it.convert() }
    }

    fun createUser(token: String, u: UserInput) = authenticate(token, AccountPrivilege.WRITE) {
        userDataSource.create(u.convert())
    }

    fun updateUser(token: String, u: User) = authenticate(token, AccountPrivilege.WRITE) {
        userDataSource.update(u.encrypt())
    }

    private fun findAccountByUsernameAndPassword(credentials: Credentials): AccountOutput {
        val account = accountDataSource.readByUsername(credentials.username)
                ?: throw UnauthorizedException("incorrect username")

        val passwordHash = hashPassword(credentials.password, account.salt)
        if (passwordHash == account.passwordHash) {
            return account.convert(loginToken)
        } else {
            throw UnauthorizedException("wrong credentials")
        }
    }

    fun login(credentials: Credentials): CredentialsToken {
        val account = findAccountByUsernameAndPassword(credentials)
        return CredentialsToken(
                username = account.username,
                token = createToken(account),
                role = account.role,
                user = account.user
        )
    }

    fun <T> authenticate(token: String, privilege: AccountPrivilege, id: String? = null, action: () -> T): T {
        try {
            val claims = Jwts.parser()
                    .setSigningKey(secret.toByteArray())
                    .parseClaimsJws(token)
                    .body

            val user = parseClaims(claims)
            if (privilege in user.role.privileges) {
                logger.info { "User '${user.username}' has '$privilege' privilege" }
                if (id == null) {
                    return action()
                } else if (id == user.accountId || AccountPrivilege.ADMIN in user.role.privileges) {
                    logger.info { "User '${user.username}' passes ID-check with role '${user.role}' [id: $id, accountId: ${user.accountId}]" }
                    return action()
                }
            }
        } catch (e: JwtException) {
            logger.info { "Error on parsing Jwt: $e" }
        } catch (e: IllegalArgumentException) {
            logger.info { "Error on parsing Jwt: $e" }
        }
        throw UnauthorizedException("not authorized")
    }

    private fun parseClaims(claims: Claims): AuthorizedUser {
        val username = claims.subject
        val accountId = requireNotNull(claims["account_id"]).toString()
        val role = AccountRole.valueOf(requireNotNull(claims["role"]).toString())
        return AuthorizedUser(
                username = username,
                accountId = accountId,
                role = role
        )
    }

    private fun createToken(account: AccountOutput): String {
        val claims = Jwts.claims()
        claims.subject = account.username
        claims.expiration = Date.from(ZonedDateTime.now()
                .plusSeconds(TOKEN_AGE)
                .toInstant())
        claims["account_id"] = account.id
        claims["role"] = account.role.toString()

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret.toByteArray())
                .compact()
    }

    private fun createInfiniteToken(username: String, role: AccountRole): String {
        val claims = Jwts.claims()
        claims.subject = username
        claims.expiration = Date.from(ZonedDateTime.now()
                .plusYears(1000)
                .toInstant())
        claims["account_id"] = username
        claims["role"] = role.toString()

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret.toByteArray())
                .compact()
    }

    private data class AuthorizedUser(
            val username: String,
            val accountId: String,
            val role: AccountRole
    )

    companion object {
        // token lasts for 2 hours
        private const val TOKEN_AGE: Long = 2 * 60 * 60
    }
}