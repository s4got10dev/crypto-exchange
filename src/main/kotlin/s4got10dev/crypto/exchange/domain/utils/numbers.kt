package s4got10dev.crypto.exchange.domain.utils

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.RoundingMode

fun BigDecimal?.negativeOrZero(): Boolean {
  return (this ?: ZERO) <= ZERO
}

fun BigDecimal.isZero(): Boolean {
  return this.scaled() == ZERO.scaled()
}

fun BigDecimal?.scaled(): BigDecimal {
  return this?.setScale(18, RoundingMode.HALF_UP) ?: ZERO
}

fun BigDecimal.trim(): BigDecimal {
  return this.stripTrailingZeros()
}

fun BigDecimal.string(): String {
  return this.stripTrailingZeros().toPlainString()
}
