package s4got10dev.crypto.exchange.domain.entity

import java.math.BigDecimal
import java.time.Instant

data class Order(
  val id: OrderId?,
  val userId: UserId,
  val walletId: WalletId,
  val type: OrderType,
  var amount: BigDecimal,
  val baseCurrency: Currency,
  val quoteCurrency: Currency,
  var status: OrderStatus,
  val createdAt: Instant? = null,
  var updatedAt: Instant? = null,
  var version: Long = 0L
)

enum class OrderType {
  BUY,
  SELL
}

enum class OrderStatus {
  OPEN,
  FILLED,
  CANCELED
}
