package dev.lfstech.data

import dev.lfstech.model.Transaction
import dev.lfstech.model.TransactionIdBalance
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toTransaction() =
    Transaction(
        type = this[Transactions.type],
        amount = this[Transactions.amount],
        description = this[Transactions.description],
        createdAt = this[Transactions.createdAt],
    )

fun ResultRow.toTransactionIdBalance() =
    TransactionIdBalance(
        transactionId = this[Transactions.id].value,
        balance = this[Transactions.balance],
    )
