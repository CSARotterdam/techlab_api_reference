package nl.csarotterdam.techlab.model.inventory

enum class InventoryMutationType {
    ADD, REMOVE
}

enum class InventoryMutationSubtype {
    STOCK, LOAN, BROKEN
}