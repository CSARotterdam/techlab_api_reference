package nl.csarotterdam.techlab.controller

import nl.csarotterdam.techlab.model.AUTHORIZATION
import nl.csarotterdam.techlab.model.Inventory
import nl.csarotterdam.techlab.model.InventoryCategory
import nl.csarotterdam.techlab.model.InventoryInput
import nl.csarotterdam.techlab.service.InventoryService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/inventory")
class InventoryController(
        private val inventoryService: InventoryService
) {

    @GetMapping("")
    fun listInventory() = inventoryService.listInventory()

    @GetMapping("/mutations")
    fun listMutations(
            @RequestHeader(AUTHORIZATION) token: String
    ) = inventoryService.listMutations(token)

    @GetMapping("/id/{id}")
    fun readInventoryById(
            @PathVariable id: String
    ) = inventoryService.readInventoryById(id)

    @GetMapping("/search/name/{name}")
    fun searchInventoryByName(
            @PathVariable name: String
    ) = inventoryService.searchInventoryByName(name)

    @GetMapping("/search/category/{category}")
    fun searchInventoryByCategory(
            @PathVariable category: InventoryCategory
    ) = inventoryService.searchInventoryByCategory(category)

    @GetMapping("/categories")
    fun getCategories() = inventoryService.getInventoryCategories()

    @GetMapping("/mutations/id/{mutationId}")
    fun readMutationById(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable mutationId: String
    ) = inventoryService.readMutationById(token, mutationId)

    @GetMapping("/mutations/inventory/{inventoryId}")
    fun readAllMutationsByInventoryId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable inventoryId: String
    ) = inventoryService.readAllMutationsByInventoryId(token, inventoryId)

    @GetMapping("/mutations/loan/{loanId}")
    fun readAllMutationsByLoanId(
            @RequestHeader(AUTHORIZATION) token: String,
            @PathVariable loanId: String
    ) = inventoryService.readAllMutationsByLoanId(token, loanId)

    @PostMapping("/create")
    fun createInventory(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody inventory: InventoryInput
    ) = inventoryService.createInventory(token, inventory)

    @PutMapping("/update")
    fun updateInventory(
            @RequestHeader(AUTHORIZATION) token: String,
            @RequestBody inventory: Inventory
    ) = inventoryService.updateInventory(token, inventory)
}