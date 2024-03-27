package s4got10dev.crypto.exchange.domain.usecase

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.PaymentId
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.entity.WalletId

data class CreateWalletCommand(
  val userId: UserId,
  val name: String,
  val currencies: List<Currency>
) : Command {

  fun toWallet(): Wallet {
    return Wallet(
      id = null,
      userId = userId,
      name = name,
      balance = currencies.associateWith { ZERO }.toMutableMap()
    )
  }
}

data class DepositCommand(
  val walletId: WalletId,
  val amount: BigDecimal,
  val currency: Currency,
  val paymentId: PaymentId
) : Command

data class WithdrawCommand(
  val walletId: WalletId,
  val amount: BigDecimal,
  val currency: Currency,
  val paymentId: PaymentId
) : Command

data class WalletQuery(val userId: UserId, val walletId: WalletId) : Query

data class WalletsQuery(val userId: UserId) : Query

data class WalletCreatedEvent(
  val walletId: WalletId
) : Event
