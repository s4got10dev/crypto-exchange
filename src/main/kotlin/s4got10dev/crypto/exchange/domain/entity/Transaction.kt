package s4got10dev.crypto.exchange.domain.entity

import java.time.Instant

data class Transaction(
  val id: TransactionId?,
  val userId: UserId,
  val walletId: WalletId,
  val type: TransactionType,
  val metadata: Map<String, String>,
  val createdAt: Instant? = null
)

enum class TransactionType {
  DEPOSIT,
  WITHDRAWAL,
  ORDER_PLACED,
  ORDER_PARTIALLY_FILLED,
  ORDER_FILLED,
  ORDER_CANCELED
}
