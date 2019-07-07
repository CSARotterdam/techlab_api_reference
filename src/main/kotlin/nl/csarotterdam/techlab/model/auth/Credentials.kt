package nl.csarotterdam.techlab.model.auth

import nl.csarotterdam.techlab.model.db.UserOutput

data class Credentials(
        val username: String,
        val password: String
)

data class CredentialsToken(
        val username: String,
        val token: String,
        val role: AccountRole,
        val user: UserOutput
)