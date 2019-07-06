package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.inventoryMutation
import nl.csarotterdam.techlab.model.InventoryMutation
import nl.csarotterdam.techlab.model.InventoryMutationInput
import nl.csarotterdam.techlab.model.InventoryMutationSubtype
import nl.csarotterdam.techlab.model.InventoryMutationType
import nl.csarotterdam.techlab.util.setValueCanBeNull
import nl.csarotterdam.techlab.util.toResultOrNull
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.Types
import java.util.*

@Component
class InventoryMutationDataSource : DataSource<InventoryMutation>() {

    override fun read(rs: ResultSet) = InventoryMutation(
            mutation_id = rs.getString(1),
            inventory_id = rs.getString(2),
            type = InventoryMutationType.valueOf(rs.getString(3)),
            subtype = InventoryMutationSubtype.valueOf(rs.getString(4)),
            loan_id = rs.getString(5).toResultOrNull(rs),
            amount = rs.getInt(6),
            time = rs.getTimestamp(7)
    )

    fun list() = database.executeQuery(
            query = config[inventoryMutation.readAll],
            map = this::read
    )

    fun readById(mutationId: String) = database.assertOneResult(database.executeQuery(
            query = config[inventoryMutation.readById],
            init = { it.setString(1, mutationId) },
            map = this::read
    ))

    fun readAllByInventoryId(inventoryId: String) = database.executeQuery(
            query = config[inventoryMutation.readAllByInventoryId],
            init = { it.setString(1, inventoryId) },
            map = this::read
    )

    fun readAllByLoanId(loanId: String) = database.executeQuery(
            query = config[inventoryMutation.readAllByLoanId],
            init = { it.setString(1, loanId) },
            map = this::read
    )

    fun create(im: InventoryMutationInput) = database.execute(
            query = config[inventoryMutation.create],
            init = {
                im.run {
                    it.setString(1, UUID.randomUUID().toString())
                    it.setString(2, inventory_id)
                    it.setString(3, type.toString())
                    it.setString(4, subtype.toString())
                    it.setValueCanBeNull(5, Types.VARCHAR, loan_id) { n, v -> it.setString(n, v) }
                    it.setInt(6, amount)
                }
            }
    )
}