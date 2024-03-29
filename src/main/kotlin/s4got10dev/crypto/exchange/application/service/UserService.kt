package s4got10dev.crypto.exchange.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.InternalError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.UserRepository
import s4got10dev.crypto.exchange.domain.usecase.CreateUserCommand
import s4got10dev.crypto.exchange.domain.usecase.UserCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.UserQuery

@Service
class UserService(
  private val userRepository: UserRepository
) {

  @Transactional
  suspend fun registerUser(command: CreateUserCommand): UserCreatedEvent {
    val exist = userRepository.existsByUsernameOrEmail(command.username, command.email)
    if (exist) {
      throw BadRequestError("User with such username or email already exists")
    }
    val savedUser = userRepository.save(command.toUser())
    if (savedUser.id == null) {
      throw InternalError("User was not saved")
    }
    return UserCreatedEvent(savedUser.id)
  }

  @Transactional(readOnly = true)
  suspend fun getUser(query: UserQuery): User {
    val user = userRepository.findById(query.userId)
    if (user == null || user.id != query.userId) {
      throw NotFoundError("User '${query.userId}' not found")
    }
    return user
  }
}
