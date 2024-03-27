package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import java.util.UUID
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
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

  fun createWalletCommand(userId: UserId?, request: CreateWalletRequest): Mono<CreateWalletCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    val currencies = request.currencies?.filterNotNull()
    if (currencies.isNullOrEmpty()) {
      return BadRequestError("At least one currency is required").toMono()
    }
    if (userId == null || request.name == null) {
      return BadRequestError("Required fields are missing").toMono()
    }
    return CreateWalletCommand(userId = userId, name = request.name, currencies = currencies).toMono()
  }

  fun walletQuery(userId: UserId?, walletId: String): Mono<WalletQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null) {
      return BadRequestError("Invalid wallet id").toMono()
    }
    return WalletQuery(userId = userId, walletId = walletId).toMono()
  }

  fun walletsQuery(userId: UserId?): Mono<WalletsQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    return WalletsQuery(userId = userId).toMono()
  }

  fun depositCommand(walletId: String?, request: DepositRequest): Mono<DepositCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null || request.amount == null || request.currency == null || request.paymentId == null) {
      return BadRequestError("Required fields are missing").toMono()
    }
    if (request.amount.negativeOrZero()) {
      return ValidationError(mapOf("amount" to "Amount should be positive"), "Validation error occurred").toMono()
    }
    return DepositCommand(
      walletId = walletId,
      currency = request.currency,
      amount = request.amount,
      paymentId = request.paymentId
    ).toMono()
  }

  fun withdrawCommand(walletId: String?, request: WithdrawalRequest): Mono<WithdrawCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    val walletId = runCatching { UUID.fromString(walletId) }.getOrNull()
    if (walletId == null || request.amount == null || request.currency == null || request.paymentId == null) {
      return BadRequestError("Required fields are missing").toMono()
    }
    if (request.amount.negativeOrZero()) {
      return ValidationError(mapOf("amount" to "Amount should be positive"), "Validation error occurred").toMono()
    }
    return WithdrawCommand(
      walletId = walletId,
      currency = request.currency,
      amount = request.amount,
      paymentId = request.paymentId
    ).toMono()
  }
}
