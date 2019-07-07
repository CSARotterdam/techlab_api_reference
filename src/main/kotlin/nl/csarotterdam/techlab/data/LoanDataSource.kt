package nl.csarotterdam.techlab.data

import nl.csarotterdam.techlab.config.loan
import nl.csarotterdam.techlab.model.db.Loan
import nl.csarotterdam.techlab.util.setValueCanBeNull
import nl.csarotterdam.techlab.util.toResultOrNull
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.Types

@Component
class LoanDataSource : DataSource<Loan>() {

    override fun read(rs: ResultSet) = Loan(
            id = rs.getString(1),
            contract_id = rs.getString(2),
            return_date = rs.getDate(3),
            returned_on = rs.getTimestamp(4).toResultOrNull(rs)
    )

    fun readAllActiveLoans() = database.executeQuery(
            query = config[loan.readAllActiveLoans],
            map = this::read
    )

    fun readById(id: String) = database.assertOneResult(database.executeQuery(
            query = config[loan.readById],
            init = { it.setString(1, id) },
            map = this::read
    ))

    fun readByContractId(contractId: String) = database.assertOneResult(database.executeQuery(
            query = config[loan.readByContractId],
            init = { it.setString(1, contractId) },
            map = this::read
    ))

    fun readActiveLoansByUserId(userId: String) = database.executeQuery(
            query = config[loan.readActiveLoansByUserId],
            init = { it.setString(1, userId) },
            map = this::read
    )

    fun readByUserId(userId: String) = database.executeQuery(
            query = config[loan.readByUserId],
            init = { it.setString(1, userId) },
            map = this::read
    )

    fun create(l: Loan) = database.execute(
            query = config[loan.create],
            init = {
                l.run {
                    it.setString(1, id)
                    it.setString(2, contract_id)
                    it.setDate(3, return_date)
                    it.setValueCanBeNull(4, Types.TIMESTAMP, returned_on) { n, v -> it.setTimestamp(n, v) }
                }
            }
    )

    fun setReturned(id: String) = database.execute(
            query = config[loan.setReturned],
            init = { it.setString(1, id) }
    )
}