package s4got10dev.crypto.exchange.domain.entity

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.Instant

data class Wallet(
  val id: WalletId?,
  val userId: UserId,
  val name: String,
  val balance: MutableMap<Currency, BigDecimal>,
  val createdAt: Instant? = null,
  var updatedAt: Instant? = null,
  var version: Long = 0L
) {

  fun getBalance(currency: Currency): BigDecimal {
    return balance[currency] ?: ZERO
  }

  private fun setBalance(currency: Currency, amount: BigDecimal) {
    balance[currency] = amount
  }

  fun addBalance(currency: Currency, amount: BigDecimal) {
    setBalance(currency, getBalance(currency) + amount)
  }

  fun subtractBalance(currency: Currency, amount: BigDecimal) {
    setBalance(currency, getBalance(currency) - amount)
  }
}
