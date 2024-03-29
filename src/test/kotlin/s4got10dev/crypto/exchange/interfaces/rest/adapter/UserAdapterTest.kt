package s4got10dev.crypto.exchange.interfaces.rest.adapter

import io.mockk.every
import io.mockk.mockk
import jakarta.validation.Validator
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
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
    assertThat(result.username).isEqualTo(request.username)
    assertThat(result.password).isEqualTo(encryptedPassword)
    assertThat(result.firstName).isEqualTo(request.firstName)
    assertThat(result.lastName).isEqualTo(request.lastName)
    assertThat(result.email).isEqualTo(request.email)
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

    assertThatThrownBy { userAdapter.createUserCommand(request) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Required fields are missing")
  }

  @Test
  fun `userQuery should return UserQuery when userId is valid`() {
    val userId = randomUUID()

    val result = userAdapter.userQuery(userId)
    assertThat(result.userId).isEqualTo(userId)
  }

  @Test
  fun `userQuery should return BadRequestError when userId is invalid`() {
    val userId = null

    assertThatThrownBy { userAdapter.userQuery(userId) }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }
}
