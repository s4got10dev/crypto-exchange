package s4got10dev.crypto.exchange.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
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
  fun registerUser(command: CreateUserCommand): Mono<UserCreatedEvent> {
    return userRepository.existsByUsernameOrEmail(command.username, command.email)
      .flatMap {
        if (it) {
          return@flatMap BadRequestError("User with such username or email already exists").toMono()
        }
        userRepository.save(command.toUser())
          .flatMap { user ->
            if (user.id == null) {
              InternalError("User was not saved").toMono()
            } else {
              UserCreatedEvent(user.id).toMono()
            }
          }
      }
  }

  @Transactional(readOnly = true)
  fun getUser(query: UserQuery): Mono<User> {
    return userRepository.findById(query.userId)
      .switchIfEmpty(NotFoundError("User '${query.userId}' not found").toMono())
      .flatMap {
        if (it.id != query.userId) {
          NotFoundError("User '${query.userId}' not found").toMono()
        } else {
          it.toMono()
        }
      }
  }
}
