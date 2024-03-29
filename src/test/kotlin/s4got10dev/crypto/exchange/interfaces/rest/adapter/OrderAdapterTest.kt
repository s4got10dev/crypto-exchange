package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.domain.entity.OrderType
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest

class OrderAdapterTest {

  private val validator = mockk<Validator>()

  private val orderAdapter = OrderAdapter(validator)

  @Test
  fun `placeOrderCommand should return PlaceOrderCommand when request is valid`() {
    val request = PlaceOrderRequest(
      walletId = randomUUID(),
      type = OrderType.BUY,
      amount = 1.5.toBigDecimal(),
      baseCurrency = BTC,
      quoteCurrency = USD
    )
    val userId = randomUUID()

    every { validator.validate(request) } returns emptySet()

    val result = runBlocking { orderAdapter.placeOrderCommand(userId, request) }
    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.walletId).isEqualTo(request.walletId)
    assertThat(result.type).isEqualTo(request.type)
    assertThat(result.amount).isEqualTo(request.amount)
    assertThat(result.baseCurrency).isEqualTo(request.baseCurrency)
    assertThat(result.quoteCurrency).isEqualTo(request.quoteCurrency)
  }

  @Test
  fun `placeOrderCommand should return BadRequestError when request is invalid`() {
    val request = PlaceOrderRequest(
      walletId = null,
      type = null,
      amount = null,
      baseCurrency = null,
      quoteCurrency = null
    )
    val userId = randomUUID()

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { runBlocking { orderAdapter.placeOrderCommand(userId, request) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `orderQuery should return OrderQuery when userId and orderId are valid`() {
    val userId = randomUUID()
    val orderId = randomUUID().toString()

    val result = runBlocking { orderAdapter.orderQuery(userId, orderId) }
    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.orderId.toString()).isEqualTo(orderId)
  }

  @Test
  fun `orderQuery should return BadRequestError when userId is null`() {
    val orderId = randomUUID().toString()

    assertThatThrownBy { runBlocking { orderAdapter.orderQuery(null, orderId) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `orderQuery should return BadRequestError when orderId is invalid`() {
    val userId = randomUUID()
    val orderId = "invalid"

    assertThatThrownBy { runBlocking { orderAdapter.orderQuery(userId, orderId) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid order id")
  }

  @Test
  fun `ordersQuery should return OrdersQuery when userId is valid`() {
    val userId = randomUUID()

    val result = runBlocking { orderAdapter.ordersQuery(userId) }
    assertThat(result.userId).isEqualTo(userId)
  }

  @Test
  fun `ordersQuery should return BadRequestError when userId is null`() {
    assertThatThrownBy { runBlocking { orderAdapter.ordersQuery(null) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `cancelOrderCommand should return CancelOrderCommand when userId and orderId are valid`() {
    val userId = randomUUID()
    val orderId = randomUUID().toString()


    val result = runBlocking { orderAdapter.cancelOrderCommand(userId, orderId) }
    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.orderId.toString()).isEqualTo(orderId)
  }

  @Test
  fun `cancelOrderCommand should return BadRequestError when userId is null`() {
    val orderId = randomUUID().toString()

    assertThatThrownBy { runBlocking { orderAdapter.cancelOrderCommand(null, orderId) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `cancelOrderCommand should return BadRequestError when orderId is invalid`() {
    val userId = randomUUID()
    val orderId = "invalid"

    assertThatThrownBy { runBlocking { orderAdapter.cancelOrderCommand(userId, orderId) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid order id")
  }
}
