package nl.csarotterdam.techlab.model

abstract class Table(
        private val tableName: String
) {
    fun getTableName() = tableName
}