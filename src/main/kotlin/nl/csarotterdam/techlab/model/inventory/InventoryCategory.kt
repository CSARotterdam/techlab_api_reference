package nl.csarotterdam.techlab.model.inventory

enum class InventoryCategory(val text: String) {
    AUGMENTED_REALITY("Augmented Reality (AR)"),
    VIRTUAL_REALITY("Virtual Reality (VR)"),

    SMART_TECHNOLOGY("Smart Technology"),

    MICRO_COMPUTER("Micro Computer"),
    COMPUTER("Computer"),

    DRONE("Drone"),

    RADIO_CONTROLLED("Radio Controlled"),

    GAME_CONSOLE("Game Console"),
    GAME("Game"),

    CABLES("Cables")
}

data class InventoryCategoryOutput(
        val category: InventoryCategory
) {
    val category_text: String = category.text
}