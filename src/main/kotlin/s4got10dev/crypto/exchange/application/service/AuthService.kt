package s4got10dev.crypto.exchange.application.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.PlainPassword
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.Username
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.error.UnauthorizedError
import s4got10dev.crypto.exchange.domain.repository.UserRepository

@Service
class AuthService(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) {

  @Transactional(readOnly = true)
  fun login(username: Username, password: PlainPassword): Mono<User> {
    return userRepository.findByUsername(username)
      .switchIfEmpty(NotFoundError("User with username '$username' not found").toMono())
      .flatMap { user ->
        if (passwordEncoder.matches(password, user.password)) {
          user.toMono()
        } else {
          UnauthorizedError("Invalid username/password").toMono()
        }
      }
  }
}
