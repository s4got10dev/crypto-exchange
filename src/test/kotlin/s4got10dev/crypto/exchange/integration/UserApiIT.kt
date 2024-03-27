package s4got10dev.crypto.exchange.integration

import java.util.UUID.fromString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS_ME
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS_REGISTER
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.RegisterUserRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.UserResponse

class UserApiIT : BaseApiIT() {

  @Value("classpath:data/user.sql")
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
  fun `register success`() {
    val request = RegisterUserRequest(
      username = "test-registration-new",
      password = "P@ssw0rd",
      firstName = "Test",
      lastName = "Registration New",
      email = "test-registration-new@mail.com"
    )

    webTestClient
      .post()
      .uri(API_V1_USERS_REGISTER)
      .bodyValue(request)
      .exchange()
      .expectStatus().isCreated()
      .expectHeader().value("Location") {
        assertThat(it).startsWith(API_V1_USERS)
      }
  }

  @Test
  fun `register with existing username`() {
    val request = RegisterUserRequest(
      username = "test-registration-existing",
      password = "P@ssw0rd",
      firstName = "Test",
      lastName = "Registration Existing",
      email = "test-registration-existing@mail.com"
    )

    webTestClient
      .post()
      .uri(API_V1_USERS_REGISTER)
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
        assertThat(error.detail).isEqualTo("User with such username or email already exists")
        assertThat(error.instance).isEqualTo(API_V1_USERS_REGISTER)
      }
  }

  @Test
  fun `me success`() {
    webTestClient
      .get()
      .uri(API_V1_USERS_ME)
      .cookie(COOKIE_AUTH, token(fromString("5649f7c3-c0e0-4734-8e7e-512161744fb6"), "test-me"))
      .exchange()
      .expectStatus().isOk()
      .expectBody(UserResponse::class.java)
      .consumeWith {
        val user = it.responseBody
        assertThat(user).isNotNull()
        user as UserResponse
        assertThat(user.username).isEqualTo("test-me")
        assertThat(user.firstName).isEqualTo("Test")
        assertThat(user.lastName).isEqualTo("Me")
        assertThat(user.email).isEqualTo("test-me@mail.com")
      }
  }
}
