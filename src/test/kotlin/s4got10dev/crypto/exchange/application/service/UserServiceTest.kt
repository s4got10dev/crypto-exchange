package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
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

    coEvery { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns false
    coEvery { userRepository.save(command.toUser()) } returns user

    val result = runBlocking { userService.registerUser(command) }
    assertThat(result).isNotNull()
    assertThat(result.userId).isEqualTo(user.id)

    coVerify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    coVerify(exactly = 1) { userRepository.save(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `registerUser should return BadRequestError when username and email not unique`() {
    val command = CreateUserCommand("username", "password", "firstName", "lastName", "email")

    coEvery { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns true

    assertThatThrownBy { runBlocking { userService.registerUser(command) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("User with such username or email already exists")

    coVerify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `registerUser should return InternalError if user is not saved`() {
    val command = CreateUserCommand("username", "password", "firstName", "lastName", "email")
    val user = User(null, "username", "password", "firstName", "lastName", "email")

    coEvery { userRepository.existsByUsernameOrEmail(command.username, command.email) } returns false
    coEvery { userRepository.save(command.toUser()) } returns user

    assertThatThrownBy { runBlocking { userService.registerUser(command) } }
      .isInstanceOf(InternalError::class.java)
      .hasMessage("User was not saved")

    coVerify(exactly = 1) { userRepository.existsByUsernameOrEmail(any(), any()) }
    coVerify(exactly = 1) { userRepository.save(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return user when user is found`() {
    val userId = randomUUID()
    val user = User(userId, "username", "password", "firstName", "lastName", "email")

    coEvery { userRepository.findById(userId) } returns user

    val result = runBlocking { userService.getUser(UserQuery(userId)) }
    assertThat(result).isNotNull()
    assertThat(result.id).isEqualTo(userId)

    coVerify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return InternalError if wrong user is found`() {
    val userId = randomUUID()

    coEvery { userRepository.findById(userId) } returns null

    assertThatThrownBy { runBlocking { userService.getUser(UserQuery(userId)) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("User '$userId' not found")

    coVerify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }

  @Test
  fun `getUser should return InternalError if user is not found`() {
    val userId = randomUUID()

    coEvery { userRepository.findById(userId) } returns null

    assertThatThrownBy { runBlocking { userService.getUser(UserQuery(userId)) } }
      .isInstanceOf(NotFoundError::class.java)
      .hasMessage("User '$userId' not found")

    coVerify(exactly = 1) { userRepository.findById(any()) }
    confirmVerified(userRepository)
  }
}
