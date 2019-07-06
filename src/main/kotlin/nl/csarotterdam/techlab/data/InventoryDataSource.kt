package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.inventory
import nl.csarotterdam.techlab.model.Inventory
import nl.csarotterdam.techlab.model.InventoryCategory
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class InventoryDataSource : DataSource<Inventory>() {

    override fun read(rs: ResultSet) = Inventory(
            id = rs.getString(1),
            name = rs.getString(2),
            manufacturer = rs.getString(3),
            category = InventoryCategory.valueOf(rs.getString(4)),
            loan_time_in_days = rs.getInt(5)
    )

    fun list() = database.executeQuery(
            query = config[inventory.readAll],
            map = this::read
    )

    fun readById(id: String) = database.assertOneResult(database.executeQuery(
            query = config[inventory.readById],
            init = { it.setString(1, id) },
            map = this::read
    ))

    fun searchByName(name: String) = database.executeQuery(
            query = config[inventory.searchByName],
            init = { it.setString(1, "%$name$") },
            map = this::read
    )

    fun searchByCategory(category: InventoryCategory) = database.executeQuery(
            query = config[inventory.searchByCategory],
            init = { it.setString(1, category.toString()) },
            map = this::read
    )

    fun create(i: Inventory) = database.execute(
            query = config[inventory.create],
            init = {
                i.run {
                    it.setString(1, id)
                    it.setString(2, name)
                    it.setString(3, manufacturer)
                    it.setString(4, category.toString())
                    it.setInt(5, loan_time_in_days)
                }
            }
    )

    fun update(i: Inventory) = database.execute(
            query = config[inventory.update],
            init = {
                i.run {
                    it.setString(1, name)
                    it.setString(2, manufacturer)
                    it.setString(3, category.toString())
                    it.setInt(4, loan_time_in_days)

                    it.setString(5, id)
                }
            }
    )
}