package s4got10dev.crypto.exchange.infrastructure.persistence.mapper

import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.utils.trim
import s4got10dev.crypto.exchange.infrastructure.persistence.table.OrderTable

fun Order.toOrderTable(): OrderTable = OrderTable(
  id = id,
  userId = userId,
  walletId = walletId,
  type = type,
  amount = amount.trim(),
  baseCurrency = baseCurrency,
  quoteCurrency = quoteCurrency,
  status = status,
  createdAt = createdAt,
  updatedAt = updatedAt,
  version = version
)

fun OrderTable.toOrder(): Order = Order(
  id = id,
  userId = userId,
  walletId = walletId,
  type = type,
  amount = amount.trim(),
  baseCurrency = baseCurrency,
  quoteCurrency = quoteCurrency,
  status = status,
  createdAt = createdAt,
  updatedAt = updatedAt,
  version = version
)
