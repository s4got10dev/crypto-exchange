package s4got10dev.crypto.exchange.integration

import java.util.UUID.fromString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_LOGIN
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_LOGOUT
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH

class AuthApiIT : BaseApiIT() {

  @Value("classpath:data/auth.sql")
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
  fun `login success`() {
    webTestClient
      .post()
      .uri(API_V1_LOGIN)
      .bodyValue(mapOf("username" to "test-auth-login", "password" to "P@ssw0rd"))
      .exchange()
      .expectCookie().value(COOKIE_AUTH) {
        val token = tokenService.extractPrincipal(it)
        assertThat(token)
          .isNotNull()
          .hasFieldOrPropertyWithValue("userId", fromString("16d75f85-de7d-4c06-a5be-73b52c278a2f"))
          .hasFieldOrPropertyWithValue("username", "test-auth-login")
      }
      .expectStatus().isNoContent()
  }

  @Test
  fun `login with wrong password`() {
    webTestClient
      .post()
      .uri(API_V1_LOGIN)
      .bodyValue(mapOf("username" to "test-auth-login", "password" to "wrong-password"))
      .exchange()
      .expectCookie().doesNotExist(COOKIE_AUTH)
      .expectStatus().isUnauthorized()
  }

  @Test
  fun `logout when logged in`() {
    webTestClient
      .post()
      .uri(API_V1_LOGOUT)
      .cookie(COOKIE_AUTH, token(fromString("16d75f85-de7d-4c06-a5be-73b52c278a2f"), "test-auth-login"))
      .exchange()
      .expectCookie().value(COOKIE_AUTH) {
        assertThat(it).isBlank()
      }
      .expectStatus().isNoContent()
  }

  @Test
  fun `logout when not logged in`() {
    webTestClient
      .post()
      .uri(API_V1_LOGOUT)
      .exchange()
      .expectCookie().doesNotExist(COOKIE_AUTH)
      .expectStatus().isUnauthorized()
  }
}
