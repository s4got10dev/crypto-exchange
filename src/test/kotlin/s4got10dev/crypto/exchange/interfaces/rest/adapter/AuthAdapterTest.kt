package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.interfaces.rest.model.LoginRequest

class AuthAdapterTest {

  private val validator = mockk<Validator>()

  private val authAdapter = AuthAdapter(validator)

  @Test
  fun `performLoginCommand should return PerformLoginCommand when request is valid`() {
    val request = LoginRequest("username", "password")

    every { validator.validate(request) } returns emptySet()

    val result = authAdapter.performLoginCommand(request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.username).isEqualTo(request.username)
        assertThat(it.password).isEqualTo(request.password)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `performLoginCommand should return ValidationError when fields are null`() {
    val request = LoginRequest(null, null)

    every { validator.validate(request) } returns emptySet()

    val result = authAdapter.performLoginCommand(request)

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
