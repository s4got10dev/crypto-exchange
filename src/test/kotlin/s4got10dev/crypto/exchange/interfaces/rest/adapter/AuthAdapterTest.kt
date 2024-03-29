package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
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
    assertThat(result.username).isEqualTo(request.username)
    assertThat(result.password).isEqualTo(request.password)
  }

  @Test
  fun `performLoginCommand should return ValidationError when fields are null`() {
    val request = LoginRequest(null, null)

    every { validator.validate(request) } returns emptySet()

    assertThatThrownBy { authAdapter.performLoginCommand(request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }
}
