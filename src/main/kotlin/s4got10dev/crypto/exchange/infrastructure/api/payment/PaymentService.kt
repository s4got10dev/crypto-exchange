package s4got10dev.crypto.exchange.infrastructure.api.payment

import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.PaymentId

interface PaymentService {
  fun receiveMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Mono<Boolean>
  fun sendMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Mono<Boolean>
}

@Component
class MockPaymentService : PaymentService {

  private val log = KotlinLogging.logger {}

  override fun receiveMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Mono<Boolean> {
    if (amount in (100.toBigDecimal()..200.toBigDecimal())) {
      return false.toMono()
    }
    log.info { "Recieving $amount $currency" }
    return true.toMono()
  }

  override fun sendMoney(paymentId: PaymentId, amount: BigDecimal, currency: Currency): Mono<Boolean> {
    if (amount in (100.toBigDecimal()..200.toBigDecimal())) {
      return false.toMono()
    }
    log.info { "Sending $amount $currency" }
    return true.toMono()
  }
}
