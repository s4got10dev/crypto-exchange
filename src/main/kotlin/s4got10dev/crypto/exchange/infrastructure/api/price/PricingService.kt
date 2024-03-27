package s4got10dev.crypto.exchange.infrastructure.api.price

import java.math.BigDecimal
import org.springframework.stereotype.Component
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.DOGE
import s4got10dev.crypto.exchange.domain.entity.Currency.ETH
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.Currency.USD

interface PricingService {
  fun getPrice(baseCurrency: Currency, quoteCurrency: Currency): BigDecimal
}

@Component
class MockPricingService : PricingService {

  private val exchangeRates = mapOf(
    USD to mapOf(
      USD to BigDecimal("1"),
      EUR to BigDecimal("0.927"),
      BTC to BigDecimal("70000"),
      ETH to BigDecimal("19.55"),
      DOGE to BigDecimal("392181.20")
    ),
    EUR to mapOf(
      USD to BigDecimal("1.079"),
      EUR to BigDecimal("1"),
      BTC to BigDecimal("0.000015"),
      ETH to BigDecimal("0.00030"),
      DOGE to BigDecimal("6.04")
    ),
    ETH to mapOf(
      USD to BigDecimal("3590.80"),
      EUR to BigDecimal("3315.21"),
      ETH to BigDecimal("1"),
      DOGE to BigDecimal("20012.11"),
      BTC to BigDecimal("0.051")
    ),
    DOGE to mapOf(
      USD to BigDecimal("0.18"),
      EUR to BigDecimal("0.17"),
      ETH to BigDecimal("0.000050"),
      DOGE to BigDecimal("1"),
      BTC to BigDecimal("0.0000026")
    ),
    BTC to mapOf(
      USD to BigDecimal("70233.2"),
      EUR to BigDecimal("64850"),
      ETH to BigDecimal("19.56"),
      DOGE to BigDecimal("391397.70"),
      BTC to BigDecimal("1")
    )
  )

  override fun getPrice(baseCurrency: Currency, quoteCurrency: Currency): BigDecimal {
    return exchangeRates[baseCurrency]?.get(quoteCurrency)
      ?: throw IllegalArgumentException("Unsupported currency pair")
  }
}
