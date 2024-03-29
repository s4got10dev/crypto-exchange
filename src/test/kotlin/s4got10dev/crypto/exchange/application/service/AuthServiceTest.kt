package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.error.UnauthorizedError
import s4got10dev.crypto.exchange.domain.repository.UserRepository

class AuthServiceTest {

  private val userRepository = mockk<UserRepository>()
  private val passwordEncoder = mockk<PasswordEncoder>()

  private val authService = AuthService(userRepository, passwordEncoder)

  @Test
  fun `login should return user when username and password are correct`() {
    val username = "user"
    val plainPassword = "password"
    val encodedPassword = "encodedPassword"
    val user = User(randomUUID(), username, encodedPassword, "firstName", "lastName", "email")

    coEvery { userRepository.findByUsername(username) } returns user
    every { passwordEncoder.matches(plainPassword, encodedPassword) } returns true

    val result = runBlocking { authService.login(username, plainPassword) }
    assertThat(result)
      .isNotNull()
      .isEqualTo(user)

    coVerify(exactly = 1) { userRepository.findByUsername(any()) }
    verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
    confirmVerified(userRepository, passwordEncoder)
  }

  @Test
  fun `login should return NotFoundError if user not found`() {
    val username = "user"
    val plainPassword = "password"

    coEvery { userRepository.findByUsername(username) } returns null

    assertThatThrownBy { runBlocking { authService.login(username, plainPassword) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("User with username '$username' not found")

    coVerify(exactly = 1) { userRepository.findByUsername(any()) }
    confirmVerified(userRepository, passwordEncoder)
  }

  @Test
  fun `login should return UnauthorizedError if password is incorrect`() {
    val username = "user"
    val plainPassword = "password"
    val encodedPassword = "encodedPassword"
    val user = User(randomUUID(), username, encodedPassword, "firstName", "lastName", "email")

    coEvery { userRepository.findByUsername(username) } returns user
    every { passwordEncoder.matches(plainPassword, encodedPassword) } returns false

    assertThatThrownBy { runBlocking { authService.login(username, plainPassword) } }
      .isInstanceOf(UnauthorizedError::class.java)
      .hasMessage("Invalid username/password")

    coVerify(exactly = 1) { userRepository.findByUsername(any()) }
    verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
    confirmVerified(userRepository, passwordEncoder)
  }
}
