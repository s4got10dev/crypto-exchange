package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import java.util.UUID
import org.springframework.stereotype.Component
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.ValidationError
import s4got10dev.crypto.exchange.domain.usecase.CreateWalletCommand
import s4got10dev.crypto.exchange.domain.usecase.DepositCommand
import s4got10dev.crypto.exchange.domain.usecase.WalletQuery
import s4got10dev.crypto.exchange.domain.usecase.WalletsQuery
import s4got10dev.crypto.exchange.domain.usecase.WithdrawCommand
import s4got10dev.crypto.exchange.domain.utils.negativeOrZero
import s4got10dev.crypto.exchange.interfaces.rest.model.CreateWalletRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.WithdrawalRequest

@Component
class WalletAdapter(
  private val validator: Validator
) {

  fun createWalletCommand(userId: UserId?, request: CreateWalletRequest): CreateWalletCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    val currencies = request.currencies?.filterNotNull()
    if (currencies.isNullOrEmpty()) {
      throw BadRequestError("At least one currency is required")
    }
    if (userId == null || request.name == null) {
      throw BadRequestError("Required fields are missing")
    }
    return CreateWalletCommand(userId = userId, name = request.name, currencies = currencies)
  }

  fun walletQuery(userId: UserId?, walletId: String): WalletQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null) {
      throw BadRequestError("Invalid wallet id")
    }
    return WalletQuery(userId = userId, walletId = walletId)
  }

  fun walletsQuery(userId: UserId?): WalletsQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    return WalletsQuery(userId = userId)
  }

  fun depositCommand(walletId: String?, request: DepositRequest): DepositCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null || request.amount == null || request.currency == null || request.paymentId == null) {
      throw BadRequestError("Required fields are missing")
    }
    if (request.amount.negativeOrZero()) {
      throw ValidationError(mapOf("amount" to "Amount should be positive"), "Validation error occurred")
    }
    return DepositCommand(
      walletId = walletId,
      currency = request.currency,
      amount = request.amount,
      paymentId = request.paymentId
    )
  }

  fun withdrawCommand(walletId: String?, request: WithdrawalRequest): WithdrawCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null || request.amount == null || request.currency == null || request.paymentId == null) {
      throw BadRequestError("Required fields are missing")
    }
    if (request.amount.negativeOrZero()) {
      throw ValidationError(mapOf("amount" to "Amount should be positive"), "Validation error occurred")
    }
    return WithdrawCommand(
      walletId = walletId,
      currency = request.currency,
      amount = request.amount,
      paymentId = request.paymentId
    )
  }
}
