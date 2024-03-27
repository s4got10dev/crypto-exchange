package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.ETH
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.ValidationError
import s4got10dev.crypto.exchange.interfaces.rest.model.CreateWalletRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.WithdrawalRequest

class WalletAdapterTest {

  private val validator = mockk<Validator>()

  private val walletAdapter = WalletAdapter(validator)

  @Test
  fun `createWalletCommand should return CreateWalletCommand when request is valid`() {
    val request = CreateWalletRequest(name = "walletName", currencies = listOf(BTC, ETH))
    val userId = randomUUID()

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.createWalletCommand(userId, request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.name).isEqualTo(request.name)
        assertThat(it.currencies).containsExactlyInAnyOrder(BTC, ETH)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `createWalletCommand should return BadRequestError when currencies are empty`() {
    val request = CreateWalletRequest(name = "walletName", currencies = emptyList())
    val userId = randomUUID()

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.createWalletCommand(userId, request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("At least one currency is required")
        true
      }
      .verify()
  }

  @Test
  fun `createWalletCommand should return BadRequestError when userId is null`() {
    val request = CreateWalletRequest(name = "walletName", currencies = listOf(BTC, ETH))

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.createWalletCommand(null, request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Required fields are missing")
        true
      }
      .verify()
  }

  @Test
  fun `walletQuery should return WalletQuery when userId and walletId are valid`() {
    val userId = randomUUID()
    val walletId = randomUUID().toString()

    val result = walletAdapter.walletQuery(userId, walletId)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.walletId.toString()).isEqualTo(walletId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `walletQuery should return BadRequestError when userId is null`() {
    val walletId = randomUUID().toString()

    val result = walletAdapter.walletQuery(null, walletId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
  }

  @Test
  fun `walletQuery should return BadRequestError when walletId is invalid`() {
    val userId = randomUUID()
    val walletId = "invalid"

    val result = walletAdapter.walletQuery(userId, walletId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid wallet id")
        true
      }
  }

  @Test
  fun `walletsQuery should return WalletsQuery when userId is valid`() {
    val userId = randomUUID()

    val result = walletAdapter.walletsQuery(userId)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `walletsQuery should return BadRequestError when userId is null`() {
    val result = walletAdapter.walletsQuery(null)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
  }

  @Test
  fun `depositCommand should return DepositCommand when request is valid`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.depositCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.walletId).isEqualTo(walletId)
        assertThat(it.amount).isEqualTo(10.20.toBigDecimal())
        assertThat(it.currency).isEqualTo(BTC)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `depositCommand should return BadRequestError when walletId is invalid`() {
    val walletId = "invalid"
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.depositCommand(walletId, request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Required fields are missing")
        true
      }
      .verify()
  }

  @Test
  fun `depositCommand should return ValidationError when amount is negative`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = (-10.20).toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.depositCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(ValidationError::class.java)
          .hasMessage("Validation error occurred")
        true
      }
      .verify()
  }

  @Test
  fun `depositCommand should return BadRequestError when currency is null`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = null, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.depositCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Required fields are missing")
        true
      }
      .verify()
  }

  @Test
  fun `withdrawalCommand should return WithdrawalCommand when request is valid`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.withdrawCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.walletId).isEqualTo(walletId)
        assertThat(it.amount).isEqualTo(10.20.toBigDecimal())
        assertThat(it.currency).isEqualTo(BTC)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `withdrawalCommand should return BadRequestError when walletId is invalid`() {
    val walletId = "invalid"
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.withdrawCommand(walletId, request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Required fields are missing")
        true
      }
      .verify()
  }

  @Test
  fun `withdrawalCommand should return ValidationError when amount is negative`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = (-10.20).toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.withdrawCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(ValidationError::class.java)
          .hasMessage("Validation error occurred")
        true
      }
      .verify()
  }

  @Test
  fun `withdrawalCommand should return BadRequestError when currency is null`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = null, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.withdrawCommand(walletId.toString(), request)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Required fields are missing")
        true
      }
      .verify()
  }
}
