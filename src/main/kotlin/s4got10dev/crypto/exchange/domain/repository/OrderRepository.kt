package s4got10dev.crypto.exchange.domain.repository

import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderId
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.entity.UserId

interface OrderRepository {

  suspend fun save(order: Order): Order

  suspend fun findById(orderId: OrderId): Order?

  suspend fun findAllByUserId(userId: UserId): List<Order>

  suspend fun findAllByBaseCurrencyAndQuoteCurrencyAndType(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType
  ): List<Order>
}
