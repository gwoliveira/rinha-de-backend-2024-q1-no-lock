package dev.lfstech.rounting.request

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
    val valor: Long,
    val tipo: Char,
    val descricao: String
)