package dev.lfstech.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Transactions : IntIdTable("transactions") {
    val customer = reference("customer_id", Customers)
    val type = char("type")
    val amount = long("amount")
    val description = varchar("description", 10)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val balance = long("balance").nullable()
}
