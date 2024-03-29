package s4got10dev.crypto.exchange.infrastructure.api.payment

import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import org.springframework.stereotype.Component
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.PaymentId

interface PaymentService {
  suspend fun receiveMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Boolean
  suspend fun sendMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Boolean
}

@Component
class MockPaymentService : PaymentService {

  private val log = KotlinLogging.logger {}

  override suspend fun receiveMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Boolean {
    if (amount in (100.toBigDecimal()..200.toBigDecimal())) {
      return false
    }
    log.info { "Receiving $amount $currency" }
    return true
  }

  override suspend fun sendMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Boolean {
    if (amount in (100.toBigDecimal()..200.toBigDecimal())) {
      return false
    }
    log.info { "Sending $amount $currency" }
    return true
  }
}
