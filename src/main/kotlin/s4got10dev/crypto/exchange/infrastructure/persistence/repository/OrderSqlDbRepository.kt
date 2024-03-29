package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
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

  override suspend fun save(order: Order): Order {
    return repository.save(order.toOrderTable()).toOrder()
  }

  override suspend fun findById(orderId: OrderId): Order? {
    return repository.findById(orderId)?.toOrder()
  }

  override suspend fun findAllByUserId(userId: UserId): List<Order> {
    return repository.findAllByUserId(userId).map { it.toOrder() }
  }

  override suspend fun findAllByBaseCurrencyAndQuoteCurrencyAndType(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType
  ): List<Order> {
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
interface OrderR2dbcRepository : CoroutineCrudRepository<OrderTable, UUID> {

  suspend fun save(user: OrderTable): OrderTable

  suspend fun findAllByUserId(userId: UserId): List<OrderTable>

  suspend fun findAllByBaseCurrencyAndQuoteCurrencyAndTypeAndStatusOrderByCreatedAtAsc(
    baseCurrency: Currency,
    quoteCurrency: Currency,
    buy: OrderType,
    status: OrderStatus
  ): List<OrderTable>
}
