package s4got10dev.crypto.exchange.domain.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderId
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.entity.UserId

interface OrderRepository {
  fun save(order: Order): Mono<Order>

  fun findById(orderId: OrderId): Mono<Order>

  fun findAllByUserId(userId: UserId): Flux<Order>

  fun findAllByBaseCurrencyAndQuoteCurrencyAndType(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType
  ): Flux<Order>
}
