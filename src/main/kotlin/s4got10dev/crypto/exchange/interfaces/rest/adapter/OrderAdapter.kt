package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import java.util.UUID
import org.springframework.stereotype.Component
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

  fun placeOrderCommand(userId: UserId?, request: PlaceOrderRequest): PlaceOrderCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    if (userId == null || request.walletId == null || request.type == null ||
      request.amount == null || request.baseCurrency == null || request.quoteCurrency == null
    ) {
      throw BadRequestError("Required fields are missing")
    }
    return PlaceOrderCommand(
      userId = userId,
      walletId = request.walletId,
      type = request.type,
      amount = request.amount,
      baseCurrency = request.baseCurrency,
      quoteCurrency = request.quoteCurrency
    )
  }

  fun orderQuery(userId: UserId?, orderId: String): OrderQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    val orderId = runCatching { UUID.fromString(orderId) }.getOrNull()
    if (orderId == null) {
      throw BadRequestError("Invalid order id")
    }
    return OrderQuery(userId = userId, orderId = orderId)
  }

  fun ordersQuery(userId: UserId?): OrdersQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    return OrdersQuery(userId = userId)
  }

  fun cancelOrderCommand(userId: UserId?, orderId: String): CancelOrderCommand {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    val orderId = runCatching { UUID.fromString(orderId) }.getOrNull()
    if (orderId == null) {
      throw BadRequestError("Invalid order id")
    }
    return CancelOrderCommand(userId = userId, orderId = orderId)
  }
}
