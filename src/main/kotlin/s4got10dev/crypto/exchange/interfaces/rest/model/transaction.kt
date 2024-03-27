package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import s4got10dev.crypto.exchange.domain.entity.Transaction
import s4got10dev.crypto.exchange.domain.entity.TransactionId
import s4got10dev.crypto.exchange.domain.entity.TransactionType
import s4got10dev.crypto.exchange.domain.entity.WalletId

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransactionResponse(

  @Schema(description = "Transaction Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val id: TransactionId,

  @Schema(description = "Wallet Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val walletId: WalletId,

  @Schema(description = "Transaction Type", example = "DEPOSIT")
  val type: TransactionType,

  @Schema(description = "Metadata", example = "{\"amount\": \"1.0\", \"currency\": \"USD\"}")
  val metadata: Map<String, String>
) {
  companion object {
    fun from(transaction: Transaction): TransactionResponse {
      return TransactionResponse(
        id = transaction.id!!,
        walletId = transaction.walletId,
        type = transaction.type,
        metadata = transaction.metadata
      )
    }
  }
}
