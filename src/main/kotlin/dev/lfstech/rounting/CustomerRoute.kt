package dev.lfstech.rounting

import dev.lfstech.data.Transactions
import dev.lfstech.rounting.request.TransactionRequest
import dev.lfstech.rounting.response.Balance
import dev.lfstech.rounting.response.StatementResponse
import dev.lfstech.rounting.response.Transaction
import dev.lfstech.rounting.response.TransactionResponse
import dev.lfstech.service.CustomerService
import dev.lfstech.service.TransactionService
import dev.lfstech.util.UnprocessableEntity
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.id.EntityID
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.security.InvalidParameterException
import java.time.LocalDateTime

fun Route.customerRoute() {
    val customerService by closestDI().instance<CustomerService>()
    val transactionService by closestDI().instance<TransactionService>()

    post("/transacoes") {
        val id =
            call.parameters["id"]
                ?.toIntOrNull() ?: throw InvalidParameterException("Id deve ser um inteiro")

        val req: TransactionRequest =
            call.runCatching {
                receive<TransactionRequest>()
            }.getOrElse { throw UnprocessableEntity("Body incorreto") }

        val credit =
            customerService.getCreditLimitById(id)
                ?: throw NotFoundException("Cliente não encontrado")

        val balance = customerService.transact(id, credit, req)

        call.respond(
            message =
                TransactionResponse(
                    saldo = balance,
                    limite = credit,
                ),
        )
    }

    get("/extrato") {
        val id =
            call.parameters["id"]
                ?.toIntOrNull() ?: throw InvalidParameterException("Id deve ser um inteiro")

        val credit =
            customerService.getCreditLimitById(id)
                ?: throw NotFoundException("Cliente não encontrado")

        val transactionWithBalance = transactionService.lastTransactionsWithBalance(id, EntityID(Int.MAX_VALUE, Transactions))
        val balance = transactionWithBalance?.balance ?: 0L
        val transactions =
            if (transactionWithBalance != null) {
                transactionService.transactions(
                    id,
                    transactionWithBalance.transactionId - 10,
                    EntityID(transactionWithBalance.transactionId + 1, Transactions),
                ).reversed()
            } else {
                listOf()
            }
        call.respond(
            message =
                StatementResponse(
                    saldo =
                        Balance(
                            total = balance,
                            limite = credit,
                            data_extrato = LocalDateTime.now(),
                        ),
                    ultimas_transacoes =
                        transactions
                            .map {
                                Transaction(
                                    tipo = it.type,
                                    valor = it.amount,
                                    descricao = it.description,
                                    realizado_em = it.createdAt,
                                )
                            },
                ),
        )
    }
}
