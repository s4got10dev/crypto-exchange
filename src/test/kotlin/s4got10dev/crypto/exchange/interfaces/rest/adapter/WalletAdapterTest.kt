package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
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
    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.name).isEqualTo(request.name)
    assertThat(result.currencies).containsExactlyInAnyOrder(BTC, ETH)
  }

  @Test
  fun `createWalletCommand should return BadRequestError when currencies are empty`() {
    val request = CreateWalletRequest(name = "walletName", currencies = emptyList())
    val userId = randomUUID()

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.createWalletCommand(userId, request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("At least one currency is required")
  }

  @Test
  fun `createWalletCommand should return BadRequestError when userId is null`() {
    val request = CreateWalletRequest(name = "walletName", currencies = listOf(BTC, ETH))

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.createWalletCommand(null, request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `walletQuery should return WalletQuery when userId and walletId are valid`() {
    val userId = randomUUID()
    val walletId = randomUUID().toString()

    val result = walletAdapter.walletQuery(userId, walletId)

    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.walletId.toString()).isEqualTo(walletId)
  }

  @Test
  fun `walletQuery should return BadRequestError when userId is null`() {
    val walletId = randomUUID().toString()

    assertThatThrownBy { walletAdapter.walletQuery(null, walletId) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `walletQuery should return BadRequestError when walletId is invalid`() {
    val userId = randomUUID()
    val walletId = "invalid"

    assertThatThrownBy { walletAdapter.walletQuery(userId, walletId) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid wallet id")
  }

  @Test
  fun `walletsQuery should return WalletsQuery when userId is valid`() {
    val userId = randomUUID()

    val result = walletAdapter.walletsQuery(userId)
    assertThat(result.userId).isEqualTo(userId)
  }

  @Test
  fun `walletsQuery should return BadRequestError when userId is null`() {
    assertThatThrownBy { walletAdapter.walletsQuery(null) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `depositCommand should return DepositCommand when request is valid`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.depositCommand(walletId.toString(), request)

    assertThat(result.walletId).isEqualTo(walletId)
    assertThat(result.amount).isEqualTo(10.20.toBigDecimal())
    assertThat(result.currency).isEqualTo(BTC)
  }

  @Test
  fun `depositCommand should return BadRequestError when walletId is invalid`() {
    val walletId = "invalid"
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.depositCommand(walletId, request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `depositCommand should return ValidationError when amount is negative`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = (-10.20).toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.depositCommand(walletId.toString(), request) }
      .isInstanceOf(ValidationError::class.java)
      .hasMessage("Validation error occurred")
  }

  @Test
  fun `depositCommand should return BadRequestError when currency is null`() {
    val walletId = randomUUID()
    val request = DepositRequest(amount = 10.20.toBigDecimal(), currency = null, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.depositCommand(walletId.toString(), request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `withdrawalCommand should return WithdrawalCommand when request is valid`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    val result = walletAdapter.withdrawCommand(walletId.toString(), request)

    assertThat(result.walletId).isEqualTo(walletId)
    assertThat(result.amount).isEqualTo(10.20.toBigDecimal())
    assertThat(result.currency).isEqualTo(BTC)
  }

  @Test
  fun `withdrawalCommand should return BadRequestError when walletId is invalid`() {
    val walletId = "invalid"
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.withdrawCommand(walletId, request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `withdrawalCommand should return ValidationError when amount is negative`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = (-10.20).toBigDecimal(), currency = BTC, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.withdrawCommand(walletId.toString(), request) }
      .isInstanceOf(ValidationError::class.java)
      .hasMessage("Validation error occurred")
  }

  @Test
  fun `withdrawalCommand should return BadRequestError when currency is null`() {
    val walletId = randomUUID()
    val request = WithdrawalRequest(amount = 10.20.toBigDecimal(), currency = null, paymentId = randomUUID())

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { walletAdapter.withdrawCommand(walletId.toString(), request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }
}
