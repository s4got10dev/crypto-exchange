package s4got10dev.crypto.exchange.application.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.InternalError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.UserRepository
import s4got10dev.crypto.exchange.domain.usecase.CreateUserCommand
import s4got10dev.crypto.exchange.domain.usecase.UserQuery

class UserServiceTest {

  private val userRepository = mockk<UserRepository>()

  private val userService = UserService(userRepository)

  @Test
  fun `registerUser should return user when user is saved`() {
    val command = CreateUserCommand("username", "password", "firstName", "lastName", "email")
    val user = User(randomUUID(), "username", "password", "firstName", "lastName", "email")

    every { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns false.toMono()
    every { userRepository.save(command.toUser()) } returns user.toMono()

    StepVerifier.create(userService.registerUser(command))
      .expectNextMatches {
        assertThat(it).isNotNull
        assertThat(it.userId).isEqualTo(user.id)
        true
      }
      .verifyComplete()

    verify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    verify(exactly = 1) { userRepository.save(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `registerUser should return BadRequestError when username and email not unique`() {
    val command = CreateUserCommand("username", "password", "firstName", "lastName", "email")

    every { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns true.toMono()

    StepVerifier.create(userService.registerUser(command))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(BadRequestError::class.java)
          .hasMessage("User with such username or email already exists")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `registerUser should return InternalError if user is not saved`() {
    val command = CreateUserCommand("username", "password", "firstName", "lastName", "email")
    val user = User(null, "username", "password", "firstName", "lastName", "email")

    every { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns false.toMono()
    every { userRepository.save(command.toUser()) } returns user.toMono()

    StepVerifier.create(userService.registerUser(command))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(InternalError::class.java)
          .hasMessage("User was not saved")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    verify(exactly = 1) { userRepository.save(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return user when user is found`() {
    val userId = randomUUID()
    val user = User(userId, "username", "password", "firstName", "lastName", "email")

    every { userRepository.findById(userId) } returns user.toMono()

    StepVerifier.create(userService.getUser(UserQuery(userId)))
      .expectNextMatches {
        assertThat(it).isNotNull
        assertThat(it.id).isEqualTo(userId)
        true
      }
      .verifyComplete()

    verify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return InternalError if wrong user is found`() {
    val userId = randomUUID()
    val user = User(randomUUID(), "username", "password", "firstName", "lastName", "email")

    every { userRepository.findById(userId) } returns user.toMono()

    StepVerifier.create(userService.getUser(UserQuery(userId)))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("User '$userId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return InternalError if user is not found`() {
    val userId = randomUUID()

    every { userRepository.findById(userId) } returns Mono.empty()

    StepVerifier.create(userService.getUser(UserQuery(userId)))
      .expectErrorMatches {
        assertThat(it)
          .isInstanceOf(NotFoundError::class.java)
          .hasMessage("User '$userId' not found")
        true
      }
      .verify()

    verify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }
}
