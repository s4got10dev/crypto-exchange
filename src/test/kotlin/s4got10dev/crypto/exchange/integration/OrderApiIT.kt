package s4got10dev.crypto.exchange.integration

import java.util.UUID.fromString
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.web.reactive.server.WebTestClient
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.OrderStatus
import s4got10dev.crypto.exchange.domain.entity.OrderType.BUY
import s4got10dev.crypto.exchange.domain.utils.scaled
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_CANCEL
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_GET
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_PLACE
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.OrderResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest

class OrderApiIT : BaseApiIT() {

  @Value("classpath:data/order.sql")
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
  fun `place order invalid`() {
    val userId = "271758ff-b48c-47d1-a74f-c530f97bbbcc"
    val request = PlaceOrderRequest(
      walletId = fromString("ca586f40-cd98-4434-822e-41bdb0dce3e4"),
      type = BUY,
      amount = null,
      baseCurrency = null,
      quoteCurrency = null
    )

    webTestClient
      .post()
      .uri(API_V1_ORDERS_PLACE)
      .cookie(COOKIE_AUTH, token(userId, "test-order-1"))
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
        assertThat(error.detail).isEqualTo("3 validation errors occurred")
        assertThat(error.instance).isEqualTo(API_V1_ORDERS_PLACE)
        assertThat(error.violations)
          .isNotNull()
          .hasSize(3)
          .containsEntry("amount", "Amount must be provided")
          .containsEntry("baseCurrency", "Base Currency must be provided")
          .containsEntry("quoteCurrency", "Quote Currency must be provided")
      }
  }

  @Test
  fun `get order success`() {
    webTestClient
      .get()
      .uri(API_V1_ORDERS_GET, mapOf("id" to "78e4a1b1-135a-441f-ae4b-a1c169fa254f"))
      .cookie(COOKIE_AUTH, token("271758ff-b48c-47d1-a74f-c530f97bbbcc", "test-order-1"))
      .exchange()
      .expectStatus().isOk()
      .expectBody(OrderResponse::class.java)
      .consumeWith {
        val order = it.responseBody
        assertThat(order).isNotNull()
        order as OrderResponse
        assertThat(order.id).isEqualTo(fromString("78e4a1b1-135a-441f-ae4b-a1c169fa254f"))
        assertThat(order.walletId).isEqualTo(fromString("ca586f40-cd98-4434-822e-41bdb0dce3e4"))
        assertThat(order.type).isEqualTo(BUY)
        assertThat(order.amount.scaled()).isEqualTo(1.0.toBigDecimal().scaled())
        assertThat(order.baseCurrency).isEqualTo(BTC)
        assertThat(order.quoteCurrency).isEqualTo(EUR)
        assertThat(order.status).isEqualTo(OrderStatus.CANCELED)
      }
  }

  @Test
  fun `get order not found`() {
    val orderId = randomUUID().toString()
    webTestClient
      .get()
      .uri(API_V1_ORDERS_GET, mapOf("id" to orderId))
      .cookie(COOKIE_AUTH, token("271758ff-b48c-47d1-a74f-c530f97bbbcc", "test-order-1"))
      .exchange()
      .expectStatus().isNotFound()
      .expectBody(ErrorResponse::class.java)
      .consumeWith {
        val error = it.responseBody
        assertThat(error).isNotNull()
        error as ErrorResponse
        assertThat(error.type).isEqualTo("not-found")
        assertThat(error.title).isEqualTo("Resource not found")
        assertThat(error.status).isEqualTo(404)
        assertThat(error.detail).isEqualTo("Order not found")
        assertThat(error.instance).isEqualTo(API_V1_ORDERS_GET.replace("{id}", orderId))
      }
  }

  @Test
  fun `get orders success`() {
    webTestClient
      .get()
      .uri(API_V1_ORDERS)
      .cookie(COOKIE_AUTH, token("1660812a-dad2-4f98-808b-27b91f505b9e", "test-order-2"))
      .exchange()
      .expectStatus().isOk()
      .expectBodyList(OrderResponse::class.java)
      .consumeWith<WebTestClient.ListBodySpec<OrderResponse>> {
        val orders = it.responseBody
        assertThat(orders)
          .isNotNull()
          .hasSize(2)
        assertThat(orders?.map { order -> order.id.toString() })
          .isNotNull()
          .containsExactly("f299b366-5eba-48d3-a19c-d18907053b66", "fd828b86-e7e8-4368-9d9c-1b7bbb6427ad")
      }
  }

  @Test
  fun `cancel open order success`() {
    val orderId = "8a509b2c-4939-4a81-9fc0-c98b557181f0"
    webTestClient
      .patch()
      .uri(API_V1_ORDERS_CANCEL, mapOf("id" to orderId))
      .cookie(COOKIE_AUTH, token("271758ff-b48c-47d1-a74f-c530f97bbbcc", "test-order-1"))
      .exchange()
      .expectStatus().isAccepted()
  }

  @Test
  fun `cancel cancelled order bad request`() {
    val orderId = "78e4a1b1-135a-441f-ae4b-a1c169fa254f"
    webTestClient
      .patch()
      .uri(API_V1_ORDERS_CANCEL, mapOf("id" to orderId))
      .cookie(COOKIE_AUTH, token("271758ff-b48c-47d1-a74f-c530f97bbbcc", "test-order-1"))
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
        assertThat(error.detail).isEqualTo("Order cannot be canceled")
        assertThat(error.instance).isEqualTo(API_V1_ORDERS_CANCEL.replace("{id}", orderId))
      }
  }
}
