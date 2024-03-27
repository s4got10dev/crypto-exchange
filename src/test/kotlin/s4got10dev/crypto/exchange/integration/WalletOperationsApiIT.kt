package s4got10dev.crypto.exchange.integration

import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_DEPOSIT
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_WITHDRAWAL
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WalletResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WithdrawalRequest

class WalletOperationsApiIT : BaseApiIT() {

  @Value("classpath:data/wallet-ops.sql")
  private lateinit var sqlScript: Resource

  companion object {
    var initiated = false
  }

  @BeforeEach
  fun setUp() {
    if (initiated) return
    connectionFactory.executeSqlScript(sqlScript)
    initiated = true
  }

  @Test
  fun `deposit success`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0a18a128-4ae2-4c73-afaa-9d9e5f88235c"
    val request = DepositRequest(amount = 10000.toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_DEPOSIT, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk()
      .expectBody(WalletResponse::class.java)
      .consumeWith {
        val wallet = it.responseBody
        assertThat(wallet).isNotNull()
        wallet as WalletResponse
        assertThat(wallet.name).isEqualTo("test-1-wallet-1")
        assertThat(wallet.balance)
          .hasSize(3)
          .hasEntrySatisfying(BTC) { balance -> assertThat(balance).isEqualTo("6") }
          .hasEntrySatisfying(EUR) { balance -> assertThat(balance).isEqualTo("4740600") }
          .hasEntrySatisfying(USD) { balance -> assertThat(balance).isEqualTo("53011.6") }
      }
  }

  @Test
  fun `deposit with negative amount`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0a18a128-4ae2-4c73-afaa-9d9e5f88235c"
    val request = DepositRequest(amount = (-10000).toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_DEPOSIT, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("validation-error")
        assertThat(error.title).isEqualTo("Request contains fields not passing validation")
        assertThat(error.status).isEqualTo(400)
        assertThat(error.violations)
          .isNotNull()
          .hasSize(1)
          .hasEntrySatisfying("amount") { violation -> assertThat(violation).isEqualTo("Amount should be positive") }
      }
  }

  @Test
  fun `deposit error when payment failed`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0a18a128-4ae2-4c73-afaa-9d9e5f88235c"
    val request = DepositRequest(amount = 102.toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_DEPOSIT, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isEqualTo(422)
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("non-processable")
        assertThat(error.title).isEqualTo("Request cannot be processed")
        assertThat(error.status).isEqualTo(422)
        assertThat(error.detail).isEqualTo("Deposit payment failed")
      }
  }

  @Test
  fun `withdrawal success`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0bfee18c-6fe5-4850-a07d-c2c3b824c479"
    val request = WithdrawalRequest(amount = 10000.toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_WITHDRAWAL, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isOk()
      .expectBody(WalletResponse::class.java)
      .consumeWith {
        val wallet = it.responseBody
        assertThat(wallet).isNotNull()
        wallet as WalletResponse
        assertThat(wallet.name).isEqualTo("test-1-wallet-2")
        assertThat(wallet.balance)
          .hasSize(2)
          .hasEntrySatisfying(BTC) { balance -> assertThat(balance).isEqualTo("1.05") }
          .hasEntrySatisfying(USD) { balance -> assertThat(balance).isEqualTo("113468.6") }
      }
  }

  @Test
  fun `withdrawal from non existing balance`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0bfee18c-6fe5-4850-a07d-c2c3b824c479"
    val request = WithdrawalRequest(amount = 10000.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_WITHDRAWAL, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("bad-request")
        assertThat(error.title).isEqualTo("Bad request")
        assertThat(error.status).isEqualTo(400)
        assertThat(error.detail).isEqualTo("Insufficient funds in wallet '$walletId'")
        assertThat(error.instance).isEqualTo(API_V1_WALLETS_WITHDRAWAL.replace("{id}", walletId))
      }
  }

  @Test
  fun `withdrawal with negative amount`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0bfee18c-6fe5-4850-a07d-c2c3b824c479"
    val request = WithdrawalRequest(amount = (-10000).toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_WITHDRAWAL, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("validation-error")
        assertThat(error.title).isEqualTo("Request contains fields not passing validation")
        assertThat(error.status).isEqualTo(400)
        assertThat(error.violations)
          .isNotNull()
          .hasSize(1)
          .hasEntrySatisfying("amount") { violation -> assertThat(violation).isEqualTo("Amount should be positive") }
      }
  }

  @Test
  fun `withdrawal error when payment failed`() {
    val userId = "cd91b02e-bc8b-4bec-9404-f861eeba59b8"
    val walletId = "0bfee18c-6fe5-4850-a07d-c2c3b824c479"
    val request = WithdrawalRequest(amount = 102.toBigDecimal(), currency = USD, paymentId = randomUUID())

    webTestClient
      .post()
      .uri(API_V1_WALLETS_WITHDRAWAL, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-ops-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isEqualTo(422)
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("non-processable")
        assertThat(error.title).isEqualTo("Request cannot be processed")
        assertThat(error.status).isEqualTo(422)
        assertThat(error.detail).isEqualTo("Withdrawal payment failed")
      }
  }
}
