package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.PaymentId
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.entity.WalletId

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateWalletRequest(

  @Schema(description = "Wallet Name", example = "Primary Wallet")
  @get:Size(min = 4, max = 30, message = "Wallet name must be between 4 and 30 characters")
  val name: String?,

  @Schema(description = "Currencies", example = "[\"USD\", \"BTC\"]")
  @get:Size(min = 1, message = "At least one currency must be provided")
  val currencies: List<Currency?>?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WalletResponse(
  @Schema(description = "Wallet Id", example = "550e8400-e29b-41d4-a716-446655440000")
  val id: WalletId,

  @Schema(description = "Wallet Name", example = "Primary Wallet")
  val name: String,

  @Schema(description = "Balances", example = "{\"USD\": \"0.00\", \"EUR\": \"0.00\"})")
  val balance: Map<Currency, String>
) {
  companion object {
    fun from(wallet: Wallet): WalletResponse {
      with(wallet) {
        return WalletResponse(id!!, name, balance.mapValues { it.value.toString() })
      }
    }
  }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DepositRequest(

  @Schema(description = "Deposit amount", example = "12.53")
  @get:NotNull(message = "Deposit amount must be provided")
  @JsonFormat(shape = STRING)
  val amount: BigDecimal?,

  @Schema(description = "Currency", example = "USD")
  @get:NotNull(message = "Currency must be provided")
  val currency: Currency?,

  @Schema(description = "Payment Id", example = "550e8400-e29b-41d4-a716-446655440000")
  @get:NotNull(message = "Payment Id must be provided")
  val paymentId: PaymentId?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WithdrawalRequest(

  @Schema(description = "Withdrawal amount", example = "12.53")
  @get:NotNull(message = "Withdrawal amount must be provided")
  @JsonFormat(shape = STRING)
  val amount: BigDecimal?,

  @Schema(description = "Currency", example = "USD")
  @get:NotNull(message = "Currency must be provided")
  val currency: Currency?,

  @Schema(description = "Payment Id", example = "550e8400-e29b-41d4-a716-446655440000")
  @get:NotNull(message = "Payment Id must be provided")
  val paymentId: PaymentId?
)
