package nl.csarotterdam.techlab.model.inventory

data class InventoryInfo(
        val inventory_id: String,
        val stock_amount: Int,
        val loaned_amount: Int,
        val broken_amount: Int
)