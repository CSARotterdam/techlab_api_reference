package nl.csarotterdam.techlab.model.auth

import nl.csarotterdam.techlab.model.auth.AccountPrivilege.*

enum class AccountRole(
        vararg val privileges: AccountPrivilege
) {
    VIEWER(READ, UNAUTHORIZED_WRITE),
    MANAGER(READ, WRITE),
    ADMIN(*AccountPrivilege.values())
}

enum class AccountPrivilege {
    READ,
    UNAUTHORIZED_WRITE,
    WRITE,
    ADMIN
}