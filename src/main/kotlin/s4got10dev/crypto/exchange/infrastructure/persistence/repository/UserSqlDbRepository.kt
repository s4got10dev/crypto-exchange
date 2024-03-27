package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.EmailAddress
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username
import s4got10dev.crypto.exchange.domain.repository.UserRepository
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toUser
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toUserTable
import s4got10dev.crypto.exchange.infrastructure.persistence.table.UserTable

@Repository
@Transactional
class UserSqlDbRepository(val repository: UserR2dbcRepository) : UserRepository {

  override fun save(user: User): Mono<User> {
    return repository.save(user.toUserTable()).map { it.toUser() }
  }

  override fun findById(id: UserId): Mono<User> {
    return repository.findById(id).map { it.toUser() }
  }

  override fun existsByUsername(username: Username): Mono<Boolean> {
    return repository.existsByUsername(username)
  }

  override fun existsByUsernameOrEmail(username: Username, email: EmailAddress): Mono<Boolean> {
    return repository.existsByUsernameOrEmail(username, email)
  }

  override fun findByUsername(username: Username): Mono<User> {
    return repository.findByUsername(username).map { it.toUser() }
  }
}

@Repository
interface UserR2dbcRepository : ReactiveCrudRepository<UserTable, UUID> {

  fun save(user: UserTable): Mono<UserTable>

  fun existsByUsername(username: String): Mono<Boolean>

  fun existsByUsernameOrEmail(username: String, email: String): Mono<Boolean>

  fun findByUsername(username: String): Mono<UserTable>
}
