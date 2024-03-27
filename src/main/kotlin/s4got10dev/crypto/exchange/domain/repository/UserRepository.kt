package s4got10dev.crypto.exchange.domain.repository

import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.EmailAddress
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username

interface UserRepository {
  fun save(user: User): Mono<User>

  fun findById(id: UserId): Mono<User>

  fun existsByUsername(username: Username): Mono<Boolean>

  fun existsByUsernameOrEmail(username: Username, email: EmailAddress): Mono<Boolean>

  fun findByUsername(username: Username): Mono<User>
}
