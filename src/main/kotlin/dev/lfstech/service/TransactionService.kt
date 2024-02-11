package dev.lfstech.service

import dev.lfstech.data.Customers
import dev.lfstech.data.Transactions
import dev.lfstech.data.toTransaction
import dev.lfstech.data.toTransactionIdBalance
import dev.lfstech.rounting.request.TransactionRequest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TransactionService {
    fun create(
        customerId: Int,
        req: TransactionRequest,
    ) = transaction {
        Transactions.insertAndGetId {
            it[amount] = req.valor
            it[type] = req.tipo
            it[description] = req.descricao
            it[customer] = customerId
        }
    }

    fun updateBalance(
        transactionID: EntityID<Int>,
        balance: Long,
    ) = transaction {
        Transactions.update({ Transactions.id eq transactionID }) {
            it[Transactions.balance] = balance
        }
    }

    fun transactions(
        clientId: Int,
        lowerBounder: Int,
        higherBounder: EntityID<Int>,
    ) = transaction {
        Transactions.select(Transactions.type, Transactions.amount, Transactions.description, Transactions.createdAt)
            .where { Transactions.customer eq EntityID(clientId, Customers) }
            .andWhere { Transactions.id greater EntityID(lowerBounder, Transactions) }
            .andWhere { Transactions.id less higherBounder }
            .orderBy(Transactions.createdAt to SortOrder.ASC)
            .map { it.toTransaction() }
            .toList()
    }

    fun lastTransactionsWithBalance(
        clientId: Int,
        transactionID: EntityID<Int>,
    ) = transaction {
        Transactions.select(Transactions.id, Transactions.balance)
            .where { Transactions.customer eq EntityID(clientId, Customers) }
            .andWhere { Transactions.id less transactionID }
            .andWhere { Transactions.balance.isNotNull() }
            .limit(1)
            .orderBy(Transactions.createdAt to SortOrder.DESC)
            .map { it.toTransactionIdBalance() }
            .firstOrNull()
    }
}
