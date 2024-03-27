package s4got10dev.crypto.exchange.application.service

import java.math.BigDecimal.ZERO
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
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
  fun createWallet(command: CreateWalletCommand): Mono<WalletCreatedEvent> {
    return walletRepository.existByUserIdAndName(command.userId, command.name)
      .flatMap {
        if (it) {
          return@flatMap BadRequestError(
            "Wallet with name '${command.name}' already exists for user '${command.userId}'"
          ).toMono()
        }
        walletRepository.save(command.toWallet())
          .flatMap { wallet ->
            if (wallet.id == null) {
              InternalError("Wallet was not saved").toMono()
            } else {
              WalletCreatedEvent(wallet.id).toMono()
            }
          }
      }
  }

  @Transactional(readOnly = true)
  fun getWallet(query: WalletQuery): Mono<Wallet> {
    return walletRepository.findById(query.walletId)
      .switchIfEmpty(NotFoundError("Wallet '${query.walletId}' not found").toMono())
      .flatMap {
        if (it.userId != query.userId) {
          NotFoundError("Wallet '${query.walletId}' not found").toMono()
        } else {
          it.toMono()
        }
      }
  }

  @Transactional(readOnly = true)
  fun getWallets(query: WalletsQuery): Mono<List<Wallet>> {
    return walletRepository.findAllByUserId(query.userId).collectList()
  }

  @Transactional
  fun deposit(command: DepositCommand): Mono<Wallet> {
    return walletRepository.findById(command.walletId)
      .switchIfEmpty(NotFoundError("Wallet '${command.walletId}' not found").toMono())
      .flatMap { wallet ->
        paymentService.receiveMoney(command.paymentId, command.amount, command.currency)
          .flatMap {
            if (it) {
              val balance = wallet.balance.getOrDefault(command.currency, ZERO)
              wallet.balance[command.currency] = balance + command.amount
              walletRepository.save(wallet)
            } else {
              NonProcessableError("Deposit payment failed").toMono()
            }
          }
      }
      .map { wallet ->
        applicationEventPublisher.publishEvent(DepositTransactionCreatedEvent(command, wallet))
        wallet
      }
  }

  @Transactional
  fun withdraw(command: WithdrawCommand): Mono<Wallet> {
    return walletRepository.findById(command.walletId)
      .switchIfEmpty(NotFoundError("Wallet '${command.walletId}' not found").toMono())
      .flatMap { wallet ->
        if (wallet.balance.getOrDefault(command.currency, ZERO) < command.amount) {
          BadRequestError("Insufficient funds in wallet '${command.walletId}'").toMono()
        } else {
          wallet.toMono()
        }
      }
      .flatMap { wallet ->
        paymentService.sendMoney(command.paymentId, command.amount, command.currency).flatMap {
          if (it) {
            wallet.balance[command.currency] = wallet.balance.getOrDefault(command.currency, ZERO) - command.amount
            walletRepository.save(wallet)
          } else {
            NonProcessableError("Withdrawal payment failed").toMono()
          }
        }
      }
      .map { wallet ->
        applicationEventPublisher.publishEvent(WithdrawTransactionCreatedEvent(command, wallet))
        wallet
      }
  }
}
