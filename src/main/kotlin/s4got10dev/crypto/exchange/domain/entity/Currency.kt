package s4got10dev.crypto.exchange.domain.entity

import s4got10dev.crypto.exchange.domain.entity.CurrencyType.CRYPTO
import s4got10dev.crypto.exchange.domain.entity.CurrencyType.FIAT

enum class Currency(val type: CurrencyType) {
  USD(FIAT),
  EUR(FIAT),
  BTC(CRYPTO),
  ETH(CRYPTO),
  DOGE(CRYPTO)
}

enum class CurrencyType {
  FIAT,
  CRYPTO
}
