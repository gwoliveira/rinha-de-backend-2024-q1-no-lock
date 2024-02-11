package dev.lfstech.model

import dev.lfstech.util.UnprocessableEntity
import java.time.LocalDateTime

data class Transaction(
    val type: Char,
    val amount: Long,
    val description: String,
    val createdAt: LocalDateTime,
) {
    fun calculateBalance(
        currentBalance: Long,
        credit: Long,
    ) = when (type) {
        'd' -> if (currentBalance - amount >= -credit) currentBalance - amount else currentBalance
        'c' -> currentBalance + amount
        else -> throw UnprocessableEntity("Tipo desconhecido")
    }
}
