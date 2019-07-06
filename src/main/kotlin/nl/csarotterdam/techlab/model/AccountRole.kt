package nl.csarotterdam.techlab.model

import nl.csarotterdam.techlab.model.AccountPrivilege.READ
import nl.csarotterdam.techlab.model.AccountPrivilege.WRITE

enum class AccountRole(
        vararg val privileges: AccountPrivilege
) {
    VIEWER(READ),
    MANAGER(READ, WRITE),
    ADMIN(*AccountPrivilege.values())
}

enum class AccountPrivilege {
    READ,
    WRITE,
    PRIVATE
}