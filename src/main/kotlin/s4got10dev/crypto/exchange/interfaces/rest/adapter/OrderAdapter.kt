package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import java.util.UUID
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.CancelOrderCommand
import s4got10dev.crypto.exchange.domain.usecase.OrderQuery
import s4got10dev.crypto.exchange.domain.usecase.OrdersQuery
import s4got10dev.crypto.exchange.domain.usecase.PlaceOrderCommand
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest

@Component
class OrderAdapter(
  private val validator: Validator
) {

  fun placeOrderCommand(userId: UserId?, request: PlaceOrderRequest): Mono<PlaceOrderCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    if (userId == null || request.walletId == null || request.type == null ||
      request.amount == null || request.baseCurrency == null || request.quoteCurrency == null
    ) {
      return BadRequestError("Required fields are missing").toMono()
    }
    return PlaceOrderCommand(
      userId = userId,
      walletId = request.walletId,
      type = request.type,
      amount = request.amount,
      baseCurrency = request.baseCurrency,
      quoteCurrency = request.quoteCurrency
    ).toMono()
  }

  fun orderQuery(userId: UserId?, orderId: String): Mono<OrderQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    val orderId = runCatching { UUID.fromString(orderId) }.getOrNull()
    if (orderId == null) {
      return BadRequestError("Invalid order id").toMono()
    }
    return OrderQuery(userId = userId, orderId = orderId).toMono()
  }

  fun ordersQuery(userId: UserId?): Mono<OrdersQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    return OrdersQuery(userId = userId).toMono()
  }

  fun cancelOrderCommand(userId: UserId?, orderId: String): Mono<CancelOrderCommand> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    val orderId = runCatching { UUID.fromString(orderId) }.getOrNull()
    if (orderId == null) {
      return BadRequestError("Invalid order id").toMono()
    }
    return CancelOrderCommand(
      userId = userId,
      orderId = orderId
    ).toMono()
  }
}
