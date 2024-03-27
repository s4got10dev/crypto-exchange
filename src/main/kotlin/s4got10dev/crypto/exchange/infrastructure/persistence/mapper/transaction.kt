package s4got10dev.crypto.exchange.infrastructure.persistence.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import s4got10dev.crypto.exchange.domain.entity.Transaction
import s4got10dev.crypto.exchange.infrastructure.persistence.table.TransactionTable

fun Transaction.toTransactionTable(objectMapper: ObjectMapper): TransactionTable = TransactionTable(
  id = id,
  userId = userId,
  walletId = walletId,
  type = type,
  metadata = Json.of(objectMapper.writeValueAsString(metadata)),
  createdAt = createdAt
)

fun TransactionTable.toTransaction(objectMapper: ObjectMapper): Transaction = Transaction(
  id = id,
  userId = userId,
  walletId = walletId,
  type = type,
  metadata = objectMapper.readValue(metadata.asString(), Map::class.java)
    .mapKeys { it.key.toString() }.mapValues { it.value.toString() }
    .toMap(),
  createdAt = createdAt
)
