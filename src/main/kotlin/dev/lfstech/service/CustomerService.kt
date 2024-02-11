package dev.lfstech.service

import dev.lfstech.data.Customers
import dev.lfstech.rounting.request.TransactionRequest
import dev.lfstech.util.UnprocessableEntity
import org.jetbrains.exposed.sql.transactions.transaction

class CustomerService(
    private val transactionService: TransactionService,
) {
    fun getCreditLimitById(id: Int) =
        transaction {
            Customers.select(Customers.credit)
                .where { Customers.id eq id }
                .map { it[Customers.credit] }
                .firstOrNull()
        }

    fun transact(
        customerId: Int,
        credit: Long,
        req: TransactionRequest,
    ): Long {
        val transactionId = transaction { transactionService.create(customerId, req) }
        return transaction {
            val transactionWithBalance = transactionService.lastTransactionsWithBalance(customerId, transactionId)

            val transactions =
                transactionService.transactions(
                    customerId,
                    transactionWithBalance?.transactionId ?: 0,
                    transactionId,
                )

            val partialBalance =
                transactions
                    .fold(
                        transactionWithBalance?.balance ?: 0L,
                    ) { acc, transaction -> transaction.calculateBalance(acc, credit) }

            val balance =
                when (req.tipo) {
                    'd' -> partialBalance - req.valor
                    'c' -> partialBalance + req.valor
                    else -> throw UnprocessableEntity("Tipo desconhecido")
                }

            if (balance < -credit) {
                throw UnprocessableEntity("Crédito insuficiente")
            }

            transactionService.updateBalance(transactionId, balance)
            balance
        }
    }
}
