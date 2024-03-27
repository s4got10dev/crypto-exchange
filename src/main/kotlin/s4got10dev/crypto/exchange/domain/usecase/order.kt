package s4got10dev.crypto.exchange.domain.usecase

import java.math.BigDecimal
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderId
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.OPEN
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.WalletId

data class PlaceOrderCommand(
  val userId: UserId,
  val walletId: WalletId,
  val type: OrderType,
  val amount: BigDecimal,
  val baseCurrency: Currency,
  val quoteCurrency: Currency
) : Command {
  fun isBuy() = type == OrderType.BUY
  fun isSell() = type == OrderType.SELL

  fun toOrder(): Order {
    return Order(
      id = null,
      userId = userId,
      walletId = walletId,
      type = type,
      amount = amount,
      baseCurrency = baseCurrency,
      quoteCurrency = quoteCurrency,
      status = OPEN
    )
  }
}

data class CancelOrderCommand(
  val userId: UserId,
  val orderId: OrderId
) : Command

data class OrderQuery(val userId: UserId, val orderId: OrderId) : Query

data class OrdersQuery(val userId: UserId) : Query

data class OrderPlacedEvent(
  val orderId: OrderId
) : Event

data class MatchOrdersEvent(val baseCurrency: Currency, val quoteCurrency: Currency) : Event
