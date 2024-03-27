package s4got10dev.crypto.exchange.application.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
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

    every { userRepository.findByUsername(username) } returns user.toMono()
    every { passwordEncoder.matches(plainPassword, encodedPassword) } returns true

    StepVerifier.create(authService.login(username, plainPassword))
      .expectNextMatches {
        assertThat(it)
          .isNotNull
          .isEqualTo(user)
        true
      }
      .verifyComplete()

    verify(exactly = 1) { userRepository.findByUsername(any()) }
    verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
    confirmVerified(userRepository, passwordEncoder)
  }

  @Test
  fun `login should return NotFoundError if user not found`() {
    val username = "user"
    val plainPassword = "password"

    every { userRepository.findByUsername(username) } returns Mono.empty()

    StepVerifier.create(authService.login(username, plainPassword))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("User with username '$username' not found")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.findByUsername(any()) }
    confirmVerified(userRepository, passwordEncoder)
  }

  @Test
  fun `login should return UnauthorizedError if password is incorrect`() {
    val username = "user"
    val plainPassword = "password"
    val encodedPassword = "encodedPassword"
    val user = User(randomUUID(), username, encodedPassword, "firstName", "lastName", "email")

    every { userRepository.findByUsername(username) } returns user.toMono()
    every { passwordEncoder.matches(plainPassword, encodedPassword) } returns false

    StepVerifier.create(authService.login(username, plainPassword))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(UnauthorizedError::class.java)
          .hasMessage("Invalid username/password")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.findByUsername(any()) }
    verify(exactly = 1) { passwordEncoder.matches(any(), any()) }
    confirmVerified(userRepository, passwordEncoder)
  }
}
