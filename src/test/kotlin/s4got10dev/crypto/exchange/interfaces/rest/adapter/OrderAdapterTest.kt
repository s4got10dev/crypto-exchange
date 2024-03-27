package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
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

    val result = orderAdapter.placeOrderCommand(userId, request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.walletId).isEqualTo(request.walletId)
        assertThat(it.type).isEqualTo(request.type)
        assertThat(it.amount).isEqualTo(request.amount)
        assertThat(it.baseCurrency).isEqualTo(request.baseCurrency)
        assertThat(it.quoteCurrency).isEqualTo(request.quoteCurrency)
        true
      }
      .verifyComplete()
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

    val result = orderAdapter.placeOrderCommand(userId, request)

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
  fun `orderQuery should return OrderQuery when userId and orderId are valid`() {
    val userId = randomUUID()
    val orderId = randomUUID().toString()

    val result = orderAdapter.orderQuery(userId, orderId)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.orderId.toString()).isEqualTo(orderId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `orderQuery should return BadRequestError when userId is null`() {
    val orderId = randomUUID().toString()

    val result = orderAdapter.orderQuery(null, orderId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
      .verify()
  }

  @Test
  fun `orderQuery should return BadRequestError when orderId is invalid`() {
    val userId = randomUUID()
    val orderId = "invalid"

    val result = orderAdapter.orderQuery(userId, orderId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid order id")
        true
      }
      .verify()
  }

  @Test
  fun `ordersQuery should return OrdersQuery when userId is valid`() {
    val userId = randomUUID()

    val result = orderAdapter.ordersQuery(userId)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `ordersQuery should return BadRequestError when userId is null`() {
    val result = orderAdapter.ordersQuery(null)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
      .verify()
  }

  @Test
  fun `cancelOrderCommand should return CancelOrderCommand when userId and orderId are valid`() {
    val userId = randomUUID()
    val orderId = randomUUID().toString()

    val result = orderAdapter.cancelOrderCommand(userId, orderId)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.orderId.toString()).isEqualTo(orderId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `cancelOrderCommand should return BadRequestError when userId is null`() {
    val orderId = randomUUID().toString()

    val result = orderAdapter.cancelOrderCommand(null, orderId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
      .verify()
  }

  @Test
  fun `cancelOrderCommand should return BadRequestError when orderId is invalid`() {
    val userId = randomUUID()
    val orderId = "invalid"

    val result = orderAdapter.cancelOrderCommand(userId, orderId)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid order id")
        true
      }
      .verify()
  }
}
