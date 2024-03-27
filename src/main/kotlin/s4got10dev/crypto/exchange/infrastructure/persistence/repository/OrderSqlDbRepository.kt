package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderId
import s4got10dev.crypto.exchange.domain.entity.OrderStatus
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.OPEN
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.repository.OrderRepository
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toOrder
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toOrderTable
import s4got10dev.crypto.exchange.infrastructure.persistence.table.OrderTable

@Repository
@Transactional
class OrderSqlDbRepository(
  val repository: OrderR2dbcRepository
) : OrderRepository {

  override fun save(order: Order): Mono<Order> {
    return repository.save(order.toOrderTable()).map { it.toOrder() }
  }

  override fun findById(orderId: OrderId): Mono<Order> {
    return repository.findById(orderId).map { it.toOrder() }
  }

  override fun findAllByUserId(userId: UserId): Flux<Order> {
    return repository.findAllByUserId(userId).map { it.toOrder() }
  }

  override fun findAllByBaseCurrencyAndQuoteCurrencyAndType(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType
  ): Flux<Order> {
    return repository.findAllByBaseCurrencyAndQuoteCurrencyAndTypeAndStatusOrderByCreatedAtAsc(
      baseCurrency,
      quoteCurrency,
      buy,
      OPEN
    ).map {
      it.toOrder()
    }
  }
}

@Repository
interface OrderR2dbcRepository : ReactiveCrudRepository<OrderTable, UUID> {

  fun save(user: OrderTable): Mono<OrderTable>

  fun findAllByUserId(userId: UserId): Flux<OrderTable>

  fun findAllByBaseCurrencyAndQuoteCurrencyAndTypeAndStatusOrderByCreatedAtAsc(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType,
    status: OrderStatus
  ): Flux<OrderTable>
}
