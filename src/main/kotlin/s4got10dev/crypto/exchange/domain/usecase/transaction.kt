package s4got10dev.crypto.exchange.domain.usecase

import java.math.BigDecimal
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.TransactionType
import s4got10dev.crypto.exchange.domain.entity.TransactionType.DEPOSIT
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.entity.WalletId
import s4got10dev.crypto.exchange.domain.utils.string

data class TransactionsQuery(val userId: UserId, val page: Int, val size: Int) : Query

sealed class TransactionCreatedEvent(
  val userId: UserId,
  val walletId: WalletId,
  val type: TransactionType,
  val metadata: Map<String, String>
) : Event

data class DepositTransactionCreatedEvent(
  val depositCommand: DepositCommand,
  val wallet: Wallet
) : TransactionCreatedEvent(
  wallet.userId,
  wallet.id!!,
  DEPOSIT,
  mapOf("amount" to depositCommand.amount.string(), "currency" to depositCommand.currency.toString())
)

data class WithdrawTransactionCreatedEvent(
  val withdrawCommand: WithdrawCommand,
  val wallet: Wallet
) : TransactionCreatedEvent(
  wallet.userId,
  wallet.id!!,
  TransactionType.WITHDRAWAL,
  mapOf("amount" to withdrawCommand.amount.string(), "currency" to withdrawCommand.currency.toString())
)

data class OrderPlacedTransactionCreatedEvent(val order: Order) :
  TransactionCreatedEvent(
    order.userId,
    order.walletId,
    TransactionType.ORDER_PLACED,
    mapOf(
      "orderId" to order.id.toString(),
      "amount" to order.amount.string(),
      "type" to order.type.name,
      "baseCurrency" to order.baseCurrency.name,
      "quoteCurrency" to order.quoteCurrency.name
    )
  )

data class OrderCancelledTransactionCreatedEvent(val order: Order) :
  TransactionCreatedEvent(
    order.userId,
    order.walletId,
    TransactionType.ORDER_CANCELED,
    mapOf("orderId" to order.id.toString())
  )

data class OrderPartialFilledTransactionCreatedEvent(val order: Order, val baseAmount: BigDecimal) :
  TransactionCreatedEvent(
    order.userId,
    order.walletId,
    TransactionType.ORDER_PARTIALLY_FILLED,
    mapOf(
      "orderId" to order.id.toString(),
      "amount" to baseAmount.string()
    )
  )

data class OrderFilledTransactionCreatedEvent(val order: Order, val baseAmount: BigDecimal) :
  TransactionCreatedEvent(
    order.userId,
    order.walletId,
    TransactionType.ORDER_FILLED,
    mapOf(
      "orderId" to order.id.toString(),
      "amount" to baseAmount.string()
    )
  )
