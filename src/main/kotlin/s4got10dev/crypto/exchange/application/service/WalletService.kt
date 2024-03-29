package s4got10dev.crypto.exchange.application.service

import java.math.BigDecimal.ZERO
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.InternalError
import s4got10dev.crypto.exchange.domain.error.NonProcessableError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.domain.usecase.CreateWalletCommand
import s4got10dev.crypto.exchange.domain.usecase.DepositCommand
import s4got10dev.crypto.exchange.domain.usecase.DepositTransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.WalletCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.WalletQuery
import s4got10dev.crypto.exchange.domain.usecase.WalletsQuery
import s4got10dev.crypto.exchange.domain.usecase.WithdrawCommand
import s4got10dev.crypto.exchange.domain.usecase.WithdrawTransactionCreatedEvent
import s4got10dev.crypto.exchange.infrastructure.api.payment.PaymentService

@Service
class WalletService(
  private val walletRepository: WalletRepository,
  private val paymentService: PaymentService,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

  @Transactional
  suspend fun createWallet(command: CreateWalletCommand): WalletCreatedEvent {
    val exist = walletRepository.existByUserIdAndName(command.userId, command.name)
    if (exist) {
      throw BadRequestError("Wallet with name '${command.name}' already exists for user '${command.userId}'")
    }
    val wallet = walletRepository.save(command.toWallet())
    if (wallet.id == null) {
      throw InternalError("Wallet was not saved")
    }
    return WalletCreatedEvent(wallet.id)
  }

  @Transactional(readOnly = true)
  suspend fun getWallet(query: WalletQuery): Wallet {
    val wallet = walletRepository.findById(query.walletId)
    if (wallet == null || wallet.userId != query.userId) {
      throw NotFoundError("Wallet '${query.walletId}' not found")
    }
    return wallet
  }

  @Transactional(readOnly = true)
  suspend fun getWallets(query: WalletsQuery): List<Wallet> {
    return walletRepository.findAllByUserId(query.userId)
  }

  @Transactional
  suspend fun deposit(command: DepositCommand): Wallet {
    val wallet = walletRepository.findById(command.walletId)
      ?: throw NotFoundError("Wallet '${command.walletId}' not found")
    val processed = paymentService.receiveMoney(command.paymentId, command.amount, command.currency)
    if (!processed) {
      throw NonProcessableError("Deposit payment failed")
    }
    wallet.addBalance(command.currency, command.amount)
    val savedWallet = walletRepository.save(wallet)
    applicationEventPublisher.publishEvent(DepositTransactionCreatedEvent(command, savedWallet))
    return savedWallet
  }

  @Transactional
  suspend fun withdraw(command: WithdrawCommand): Wallet {
    val wallet = walletRepository.findById(command.walletId)
      ?: throw NotFoundError("Wallet '${command.walletId}' not found")
    if (wallet.balance.getOrDefault(command.currency, ZERO) < command.amount) {
      throw BadRequestError("Insufficient funds in wallet '${command.walletId}'")
    }
    val processed = paymentService.sendMoney(command.paymentId, command.amount, command.currency)
    if (!processed) {
      throw NonProcessableError("Withdrawal payment failed")
    }
    wallet.subtractBalance(command.currency, command.amount)
    val savedWallet = walletRepository.save(wallet)
    applicationEventPublisher.publishEvent(WithdrawTransactionCreatedEvent(command, savedWallet))
    return savedWallet
  }
}
