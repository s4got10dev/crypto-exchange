package s4got10dev.crypto.exchange.application.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal.ZERO
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.entity.WalletId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.InternalError
import s4got10dev.crypto.exchange.domain.error.NonProcessableError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.domain.usecase.CreateWalletCommand
import s4got10dev.crypto.exchange.domain.usecase.DepositCommand
import s4got10dev.crypto.exchange.domain.usecase.TransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.WalletCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.WalletQuery
import s4got10dev.crypto.exchange.domain.usecase.WalletsQuery
import s4got10dev.crypto.exchange.domain.usecase.WithdrawCommand
import s4got10dev.crypto.exchange.infrastructure.api.payment.PaymentService

class WalletServiceTest {

  private val walletRepository = mockk<WalletRepository>()
  private val paymentService = mockk<PaymentService>()
  private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

  private val walletService = WalletService(walletRepository, paymentService, applicationEventPublisher)

  @Test
  fun `createWallet should return WalletCreatedEvent when wallet is created`() {
    val command = CreateWalletCommand(randomUUID(), "name", listOf(USD, BTC))
    val wallet = Wallet(randomUUID(), command.userId, command.name, mutableMapOf(USD to ZERO, BTC to ZERO))

    every { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns false.toMono()
    every { walletRepository.save(command.toWallet()) } returns wallet.toMono()

    StepVerifier.create(walletService.createWallet(command))
      .expectNextMatches {
        assertThat(it)
          .isNotNull
          .isEqualTo(WalletCreatedEvent(wallet.id!!))
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    verify(exactly = 1) { walletRepository.save(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `createWallet should return BadRequestError if wallet with same name exists`() {
    val command = CreateWalletCommand(randomUUID(), "name", listOf(USD, BTC))
    val wallet = Wallet(null, command.userId, command.name, mutableMapOf(USD to ZERO, BTC to ZERO))

    every { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns true.toMono()

    StepVerifier.create(walletService.createWallet(command))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Wallet with name '${command.name}' already exists for user '${command.userId}'")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    confirmVerified(walletRepository, applicationEventPublisher)
  }

  @Test
  fun `createWallet should return InternalError if wallet is not created`() {
    val command = CreateWalletCommand(randomUUID(), "name", listOf(USD, BTC))
    val wallet = Wallet(null, command.userId, command.name, mutableMapOf(USD to ZERO, BTC to ZERO))

    every { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns false.toMono()
    every { walletRepository.save(command.toWallet()) } returns wallet.toMono()

    StepVerifier.create(walletService.createWallet(command))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(InternalError::class.java)
          .hasMessage("Wallet was not saved")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    verify(exactly = 1) { walletRepository.save(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return wallet when wallet is found`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val wallet = Wallet(walletId, userId, "name", mutableMapOf(USD to ZERO, BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()

    StepVerifier.create(walletService.getWallet(WalletQuery(userId, walletId)))
      .expectNextMatches {
        assertThat(it)
          .isNotNull
          .isEqualTo(wallet)
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return BadRequestError if wallet is found but user is different`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to ZERO, BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()

    StepVerifier.create(walletService.getWallet(WalletQuery(userId, walletId)))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("Wallet '$walletId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId
    val userId = randomUUID() as UserId

    every { walletRepository.findById(walletId) } returns Mono.empty()

    StepVerifier.create(walletService.getWallet(WalletQuery(userId, walletId)))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("Wallet '$walletId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallets should return empty list if no wallet found`() {
    val userId = randomUUID() as UserId

    every { walletRepository.findAllByUserId(userId) } returns Flux.empty()

    StepVerifier.create(walletService.getWallets(WalletsQuery(userId)))
      .expectNextMatches {
        assertThat(it).isEmpty()
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.findAllByUserId(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return void when deposit is successful`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { walletRepository.save(any()) } returns wallet.toMono()
    every { paymentService.receiveMoney(any(), any(), any()) } returns true.toMono()

    StepVerifier.create(walletService.deposit(DepositCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectNextMatches {
        assertThat(it.balance[USD]).isEqualTo(30.30.toBigDecimal())
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(30.30.toBigDecimal())
        }
      )
    }
    verify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    verify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return void when deposit is successful on new currency`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { walletRepository.save(any()) } returns wallet.toMono()
    every { paymentService.receiveMoney(any(), any(), any()) } returns true.toMono()

    StepVerifier.create(walletService.deposit(DepositCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectNextMatches {
        assertThat(it.balance[USD]).isEqualTo(20.30.toBigDecimal())
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(20.30.toBigDecimal())
        }
      )
    }
    verify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    verify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId

    every { walletRepository.findById(walletId) } returns Mono.empty()

    StepVerifier.create(walletService.deposit(DepositCommand(walletId, ZERO, USD, randomUUID())))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("Wallet '$walletId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return NonProcessableError if payment fails`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { paymentService.receiveMoney(any(), any(), any()) } returns false.toMono()

    StepVerifier.create(walletService.deposit(DepositCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NonProcessableError::class.java)
          .hasMessage("Deposit payment failed")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return void when withdrawal is successful`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 30.30.toBigDecimal(), BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { walletRepository.save(any()) } returns wallet.toMono()
    every { paymentService.sendMoney(any(), any(), any()) } returns true.toMono()

    StepVerifier.create(walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectNextMatches {
        assertThat(it.balance[USD]).isEqualTo(10.0.toBigDecimal())
        true
      }
      .verifyComplete()

    verify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(10.0.toBigDecimal())
        }
      )
    }
    verify(exactly = 1) { paymentService.sendMoney(any(), any(), any()) }
    verify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId

    every { walletRepository.findById(walletId) } returns Mono.empty()

    StepVerifier.create(walletService.withdraw(WithdrawCommand(walletId, ZERO, USD, randomUUID())))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("Wallet '$walletId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return BadRequestError if wallet has insufficient funds`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { walletRepository.save(any()) } returnsArgument 0

    StepVerifier.create(walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Insufficient funds in wallet '$walletId'")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return NonProcessableError if payment fails`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 30.30.toBigDecimal(), BTC to ZERO))

    every { walletRepository.findById(walletId) } returns wallet.toMono()
    every { paymentService.sendMoney(any(), any(), any()) } returns false.toMono()

    StepVerifier.create(walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NonProcessableError::class.java)
          .hasMessage("Withdrawal payment failed")
        true
      }
      .verify()

    verify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) { paymentService.sendMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }
}
