package s4got10dev.crypto.exchange.integration

import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.UUID.fromString
import java.util.UUID.randomUUID
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.withPollDelay
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.OrderType.BUY
import s4got10dev.crypto.exchange.domain.entity.OrderType.SELL
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_PLACE
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_TRANSACTIONS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_DEPOSIT
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TradeMatchingIT : BaseApiIT() {

  @Value("classpath:data/trade.sql")
  private lateinit var sqlScript: Resource

  companion object {
    var initiated = false
  }

  private val user1id = "d8f0f301-c53e-4350-9d65-43fd8ab4ebd5"
  private val user1username = "test-trade-1"
  private val wallet1id = "52e33e9d-7cc2-4482-8643-3f8d162af345"
  private val user2id = "aede8a3f-b9bf-4df6-93ca-f2d934c860d4"
  private val user2username = "test-trade-2"
  private val wallet2id = "c7efa8c0-8c7b-471d-a9f0-65b4247a57f3"

  @BeforeEach
  fun setUp() {
    if (initiated) return
    connectionFactory.executeSqlScript(sqlScript)
    initiated = true
  }

  @Order(1)
  @Test
  fun `make deposits`() {
    val payment1Id = randomUUID()
    val payment2Id = randomUUID()
    val deposit1 = DepositRequest(10.toBigDecimal(), BTC, payment1Id)
    val deposit2 = DepositRequest(1_000_000.toBigDecimal(), EUR, payment2Id)

    webTestClient
      .post()
      .uri(API_V1_WALLETS_DEPOSIT, mapOf("id" to wallet1id))
      .cookie(COOKIE_AUTH, token(user1id, user1username))
      .bodyValue(deposit1)
      .exchange()
      .expectStatus().isOk()

    webTestClient
      .post()
      .uri(API_V1_WALLETS_DEPOSIT, mapOf("id" to wallet2id))
      .cookie(COOKIE_AUTH, token(user2id, user2username))
      .bodyValue(deposit2)
      .exchange()
      .expectStatus().isOk()

    webTestClient
      .get()
      .uri(API_V1_TRANSACTIONS)
      .cookie(COOKIE_AUTH, token(user1id, user1username))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.total").isEqualTo(1)
      .jsonPath("$.content[0].type").isEqualTo("DEPOSIT")
      .jsonPath("$.content[0].metadata.amount").isEqualTo("10")
      .jsonPath("$.content[0].metadata.currency").isEqualTo("BTC")

    webTestClient
      .get()
      .uri(API_V1_TRANSACTIONS)
      .cookie(COOKIE_AUTH, token(user2id, user2username))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.total").isEqualTo(1)
      .jsonPath("$.content[0].type").isEqualTo("DEPOSIT")
      .jsonPath("$.content[0].metadata.amount").isEqualTo("1000000")
      .jsonPath("$.content[0].metadata.currency").isEqualTo("EUR")
  }

  @Order(2)
  @Test
  fun `place buy order`() {
    val order = PlaceOrderRequest(fromString(wallet2id), BUY, 4.toBigDecimal(), BTC, EUR)

    webTestClient
      .post()
      .uri(API_V1_ORDERS_PLACE)
      .cookie(COOKIE_AUTH, token(user2id, user2username))
      .bodyValue(order)
      .exchange()
      .expectStatus().isCreated()

    webTestClient
      .get()
      .uri(API_V1_TRANSACTIONS)
      .cookie(COOKIE_AUTH, token(user2id, user2username))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.total").isEqualTo(2)
      .jsonPath("$.content[0].type").isEqualTo("ORDER_PLACED")
      .jsonPath("$.content[0].metadata.type").isEqualTo("BUY")
      .jsonPath("$.content[0].metadata.amount").isEqualTo("4")
      .jsonPath("$.content[0].metadata.baseCurrency").isEqualTo("BTC")
      .jsonPath("$.content[0].metadata.quoteCurrency").isEqualTo("EUR")
  }

  @Order(3)
  @Test
  fun `place sell order`() {
    val order = PlaceOrderRequest(fromString(wallet1id), SELL, 2.toBigDecimal(), BTC, EUR)

    webTestClient
      .post()
      .uri(API_V1_ORDERS_PLACE)
      .cookie(COOKIE_AUTH, token(user1id, user1username))
      .bodyValue(order)
      .exchange()
      .expectStatus().isCreated()

    webTestClient
      .get()
      .uri(API_V1_TRANSACTIONS)
      .cookie(COOKIE_AUTH, token(user1id, user1username))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.total").isEqualTo(2)
      .jsonPath("$.content[0].type").isEqualTo("ORDER_PLACED")
      .jsonPath("$.content[0].metadata.type").isEqualTo("SELL")
      .jsonPath("$.content[0].metadata.amount").isEqualTo("2")
      .jsonPath("$.content[0].metadata.baseCurrency").isEqualTo("BTC")
      .jsonPath("$.content[0].metadata.quoteCurrency").isEqualTo("EUR")

    await withPollInterval ofMillis(100L) withPollDelay ofMillis(20L) atMost ofSeconds(10L) untilAsserted {
      webTestClient
        .get()
        .uri(API_V1_TRANSACTIONS)
        .cookie(COOKIE_AUTH, token(user1id, user1username))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.total").isEqualTo(3)
        .jsonPath("$.content[0].type").isEqualTo("ORDER_FILLED")
        .jsonPath("$.content[0].metadata.amount").isEqualTo("2")
    }

    webTestClient
      .get()
      .uri(API_V1_TRANSACTIONS)
      .cookie(COOKIE_AUTH, token(user2id, user2username))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.total").isEqualTo(3)
      .jsonPath("$.content[0].type").isEqualTo("ORDER_PARTIALLY_FILLED")
      .jsonPath("$.content[0].metadata.amount").isEqualTo("2")
  }
}
