package s4got10dev.crypto.exchange.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.web.reactive.server.WebTestClient
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_CREATE
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_GET_BY_ID
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.model.CreateWalletRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WalletResponse

class WalletApiIT : BaseApiIT() {

  @Value("classpath:data/wallet.sql")
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
  fun `create wallet success`() {
    val request = CreateWalletRequest(name = "test-wallet-new", currencies = listOf(USD, BTC))

    webTestClient
      .post()
      .uri(API_V1_WALLETS_CREATE)
      .cookie(COOKIE_AUTH, token("d7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb", "test-wallet-1"))
      .bodyValue(request)
      .exchange()
      .expectStatus().isCreated()
      .expectHeader().value("Location") {
        assertThat(it).startsWith(API_V1_WALLETS)
      }
  }

  @Test
  fun `create wallet with existing name`() {
    val userId = "d7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb"
    val request = CreateWalletRequest(name = "test-wallet-existing", currencies = listOf(USD, BTC))

    webTestClient
      .post()
      .uri(API_V1_WALLETS_CREATE)
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-1"))
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
        assertThat(error.detail).isEqualTo("Wallet with name '${request.name}' already exists for user '$userId'")
        assertThat(error.instance).isEqualTo(API_V1_WALLETS_CREATE)
      }
  }

  @Test
  fun `get wallet success`() {
    val walletId = "480dbb7b-c7fe-4131-b1bd-632faa199aca"

    webTestClient
      .get()
      .uri(API_V1_WALLETS_GET_BY_ID, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token("d7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb", "test-wallet-1"))
      .exchange()
      .expectStatus().isOk()
      .expectBody(WalletResponse::class.java)
      .consumeWith {
        val wallet = it.responseBody
        assertThat(wallet).isNotNull()
        wallet as WalletResponse
        assertThat(wallet.name).isEqualTo("test-wallet-existing")
        assertThat(wallet.balance)
          .hasSize(3)
          .hasEntrySatisfying(BTC) { balance -> assertThat(balance).isEqualTo("6") }
          .hasEntrySatisfying(EUR) { balance -> assertThat(balance).isEqualTo("4740600") }
          .hasEntrySatisfying(USD) { balance -> assertThat(balance).isEqualTo("43011.6") }
      }
  }

  @Test
  fun `get wallet not found`() {
    val walletId = "84b1c156-87eb-40ad-8e61-d662cb997ceb"

    webTestClient
      .get()
      .uri(API_V1_WALLETS_GET_BY_ID, mapOf("id" to walletId))
      .cookie(COOKIE_AUTH, token("d7096b98-ad8d-4ec7-ad4b-5341cdf6a1fb", "test-wallet-1"))
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
        assertThat(error.detail).isEqualTo("Wallet '$walletId' not found")
        assertThat(error.instance).isEqualTo(API_V1_WALLETS_GET_BY_ID.replace("{id}", walletId))
      }
  }

  @Test
  fun `get wallets success`() {
    val userId = "ec038f0f-b2d4-4142-993c-7ca2da2970b1"

    webTestClient
      .get()
      .uri(API_V1_WALLETS)
      .cookie(COOKIE_AUTH, token(userId, "test-wallet-2"))
      .exchange()
      .expectStatus().isOk()
      .expectBodyList(WalletResponse::class.java)
      .consumeWith<WebTestClient.ListBodySpec<WalletResponse>> {
        val wallets = it.responseBody
        assertThat(wallets).isNotNull()
        assertThat(wallets).hasSize(2)
        wallets as List<WalletResponse>
        assertThat(wallets[0].name).isEqualTo("test-2-wallet-1")
        assertThat(wallets[1].name).isEqualTo("test-2-wallet-2")
      }
  }
}
