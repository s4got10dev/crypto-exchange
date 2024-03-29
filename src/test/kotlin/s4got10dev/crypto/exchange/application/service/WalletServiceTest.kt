package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.math.BigDecimal.ZERO
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
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

    coEvery { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns false
    coEvery { walletRepository.save(command.toWallet()) } returns wallet

    val result = runBlocking { walletService.createWallet(command) }
    assertThat(result)
      .isNotNull()
      .isEqualTo(WalletCreatedEvent(wallet.id!!))

    coVerify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    coVerify(exactly = 1) { walletRepository.save(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `createWallet should return BadRequestError if wallet with same name exists`() {
    val command = CreateWalletCommand(randomUUID(), "name", listOf(USD, BTC))
    val wallet = Wallet(null, command.userId, command.name, mutableMapOf(USD to ZERO, BTC to ZERO))

    coEvery { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns true

    assertThatThrownBy { runBlocking { walletService.createWallet(command) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Wallet with name '${command.name}' already exists for user '${command.userId}'")

    coVerify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    confirmVerified(walletRepository, applicationEventPublisher)
  }

  @Test
  fun `createWallet should return InternalError if wallet is not created`() {
    val command = CreateWalletCommand(randomUUID(), "name", listOf(USD, BTC))
    val wallet = Wallet(null, command.userId, command.name, mutableMapOf(USD to ZERO, BTC to ZERO))

    coEvery { walletRepository.existByUserIdAndName(wallet.userId, wallet.name) } returns false
    coEvery { walletRepository.save(command.toWallet()) } returns wallet

    assertThatThrownBy { runBlocking { walletService.createWallet(command) } }
      .isInstanceOf(InternalError::class.java)
      .hasMessage("Wallet was not saved")

    coVerify(exactly = 1) { walletRepository.existByUserIdAndName(any(), any()) }
    coVerify(exactly = 1) { walletRepository.save(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return wallet when wallet is found`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val wallet = Wallet(walletId, userId, "name", mutableMapOf(USD to ZERO, BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = runBlocking { walletService.getWallet(WalletQuery(userId, walletId)) }
    assertThat(result)
      .isNotNull()
      .isEqualTo(wallet)

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return BadRequestError if wallet is found but user is different`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to ZERO, BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet

    assertThatThrownBy { runBlocking { walletService.getWallet(WalletQuery(userId, walletId)) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("Wallet '$walletId' not found")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallet should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId
    val userId = randomUUID() as UserId

    coEvery { walletRepository.findById(walletId) } returns null

    assertThatThrownBy { runBlocking { walletService.getWallet(WalletQuery(userId, walletId)) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("Wallet '$walletId' not found")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `getWallets should return empty list if no wallet found`() {
    val userId = randomUUID() as UserId

    coEvery { walletRepository.findAllByUserId(userId) } returns emptyList()

    val result = runBlocking { walletService.getWallets(WalletsQuery(userId)) }
    assertThat(result).isEmpty()

    coVerify(exactly = 1) { walletRepository.findAllByUserId(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return void when deposit is successful`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { walletRepository.save(any()) } returns wallet
    coEvery { paymentService.receiveMoney(any(), any(), any()) } returns true

    val result =
      runBlocking { walletService.deposit(DepositCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())) }
    assertThat(result.balance[USD]).isEqualTo(30.30.toBigDecimal())

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(30.30.toBigDecimal())
        }
      )
    }
    coVerify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    coVerify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return void when deposit is successful on new currency`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { walletRepository.save(any()) } returns wallet
    coEvery { paymentService.receiveMoney(any(), any(), any()) } returns true

    val result =
      runBlocking { walletService.deposit(DepositCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())) }
    assertThat(result.balance[USD]).isEqualTo(20.30.toBigDecimal())

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(20.30.toBigDecimal())
        }
      )
    }
    coVerify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    coVerify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId

    coEvery { walletRepository.findById(walletId) } returns null

    assertThatThrownBy { runBlocking { walletService.deposit(DepositCommand(walletId, ZERO, USD, randomUUID())) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("Wallet '$walletId' not found")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `deposit should return NonProcessableError if payment fails`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { paymentService.receiveMoney(any(), any(), any()) } returns false

    assertThatThrownBy {
      runBlocking {
        walletService.deposit(
          DepositCommand(
            walletId,
            20.30.toBigDecimal(),
            USD,
            randomUUID()
          )
        )
      }
    }
      .isInstanceOf(NonProcessableError::class.java)
      .hasMessage("Deposit payment failed")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    coVerify(exactly = 1) { paymentService.receiveMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return void when withdrawal is successful`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 30.30.toBigDecimal(), BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { walletRepository.save(any()) } returns wallet
    coEvery { paymentService.sendMoney(any(), any(), any()) } returns true

    val result =
      runBlocking { walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID())) }
    assertThat(result.balance[USD]).isEqualTo(10.0.toBigDecimal())

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.balance[USD]).isEqualTo(10.0.toBigDecimal())
        }
      )
    }
    coVerify(exactly = 1) { paymentService.sendMoney(any(), any(), any()) }
    coVerify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return NotFoundError if wallet is not found`() {
    val walletId = randomUUID() as WalletId

    coEvery { walletRepository.findById(walletId) } returns null

    assertThatThrownBy { runBlocking { walletService.withdraw(WithdrawCommand(walletId, ZERO, USD, randomUUID())) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("Wallet '$walletId' not found")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return BadRequestError if wallet has insufficient funds`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 10.toBigDecimal(), BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { walletRepository.save(any()) } returnsArgument 0

    assertThatThrownBy {
      runBlocking {
        walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID()))
      }
    }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Insufficient funds in wallet '$walletId'")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }

  @Test
  fun `withdraw should return NonProcessableError if payment fails`() {
    val walletId = randomUUID() as WalletId
    val wallet = Wallet(walletId, randomUUID(), "name", mutableMapOf(USD to 30.30.toBigDecimal(), BTC to ZERO))

    coEvery { walletRepository.findById(walletId) } returns wallet
    coEvery { paymentService.sendMoney(any(), any(), any()) } returns false

    assertThatThrownBy {
      runBlocking {
        walletService.withdraw(WithdrawCommand(walletId, 20.30.toBigDecimal(), USD, randomUUID()))
      }
    }
      .isInstanceOf(NonProcessableError::class.java)
      .hasMessage("Withdrawal payment failed")

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    coVerify(exactly = 1) { paymentService.sendMoney(any(), any(), any()) }
    confirmVerified(walletRepository, paymentService, applicationEventPublisher)
  }
}
