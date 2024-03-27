package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderId
import s4got10dev.crypto.exchange.domain.entity.OrderStatus
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.entity.WalletId

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PlaceOrderRequest(

  @Schema(description = "Wallet Id", example = "123e4567-e89b-12d3-a456-426614174000")
  @get:NotNull(message = "Wallet Id must be provided")
  val walletId: WalletId?,

  @Schema(description = "Order Type", example = "BUY")
  @get:NotNull(message = "Order Type must be provided")
  val type: OrderType?,

  @Schema(description = "Amount", example = "1.0")
  @get:NotNull(message = "Amount must be provided")
  val amount: BigDecimal?,

  @Schema(description = "Base Currency", example = "BTC")
  @get:NotNull(message = "Base Currency must be provided")
  val baseCurrency: Currency?,

  @Schema(description = "Quote Currency", example = "USD")
  @get:NotNull(message = "Quote Currency must be provided")
  val quoteCurrency: Currency?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OrderResponse(

  @Schema(description = "Order Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val id: OrderId,

  @Schema(description = "Wallet Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val walletId: WalletId,

  @Schema(description = "Order Type", example = "BUY")
  val type: OrderType,

  @Schema(description = "Amount", example = "1.0")
  val amount: BigDecimal,

  @Schema(description = "Base Currency", example = "BTC")
  val baseCurrency: Currency,

  @Schema(description = "Quote Currency", example = "USD")
  val quoteCurrency: Currency,

  @Schema(description = "Status", example = "OPEN")
  val status: OrderStatus
) {
  companion object {
    fun from(order: Order): OrderResponse {
      return OrderResponse(
        id = order.id!!,
        walletId = order.walletId,
        type = order.type,
        amount = order.amount,
        baseCurrency = order.baseCurrency,
        quoteCurrency = order.quoteCurrency,
        status = order.status
      )
    }
  }
}
