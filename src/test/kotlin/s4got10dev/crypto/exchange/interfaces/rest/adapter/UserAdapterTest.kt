package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.interfaces.rest.model.RegisterUserRequest

class UserAdapterTest {

  private val validator = mockk<Validator>()
  private val passwordEncoder = mockk<PasswordEncoder>()

  private val userAdapter = UserAdapter(validator, passwordEncoder)

  @Test
  fun `createUserCommand should return CreateUserCommand when request is valid`() {
    val request = RegisterUserRequest(
      username = "username",
      password = "password",
      firstName = "firstName",
      lastName = "lastName",
      email = "email"
    )
    val encryptedPassword = "encryptedPassword"

    every { validator.validate(request) } returns emptySet()
    every { passwordEncoder.encode(request.password) } returns encryptedPassword

    val result = userAdapter.createUserCommand(request)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.username).isEqualTo(request.username)
        assertThat(it.password).isEqualTo(encryptedPassword)
        assertThat(it.firstName).isEqualTo(request.firstName)
        assertThat(it.lastName).isEqualTo(request.lastName)
        assertThat(it.email).isEqualTo(request.email)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `createUserCommand should return BadRequestError when request is invalid`() {
    val request = RegisterUserRequest(
      username = null,
      password = null,
      firstName = null,
      lastName = null,
      email = null
    )

    every { validator.validate(request) } returns setOf()

    val result = userAdapter.createUserCommand(request)

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
  fun `userQuery should return UserQuery when userId is valid`() {
    val userId = randomUUID()
    val result = userAdapter.userQuery(userId)
    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `userQuery should return BadRequestError when userId is invalid`() {
    val userId = null
    val result = userAdapter.userQuery(userId)
    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("Invalid user id")
        true
      }
      .verify()
  }
}
