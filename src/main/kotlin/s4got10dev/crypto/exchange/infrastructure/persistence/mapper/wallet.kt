package s4got10dev.crypto.exchange.infrastructure.persistence.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import java.math.BigDecimal
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.infrastructure.persistence.table.WalletTable

fun Wallet.toWalletTable(objectMapper: ObjectMapper): WalletTable = WalletTable(
  id = id,
  userId = userId,
  name = name,
  balance = Json.of(objectMapper.writeValueAsString(balance)),
  createdAt = createdAt,
  updatedAt = updatedAt,
  version = version
)

fun WalletTable.toWallet(objectMapper: ObjectMapper): Wallet = Wallet(
  id = id,
  userId = userId,
  name = name,
  balance = objectMapper.readValue(balance.asString(), Map::class.java)
    .mapKeys { Currency.valueOf(it.key.toString()) }.mapValues { BigDecimal(it.value.toString()) }
    .toMutableMap(),
  createdAt = createdAt,
  updatedAt = updatedAt,
  version = version
)
