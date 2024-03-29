package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
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

  override suspend fun save(user: User): User {
    return repository.save(user.toUserTable()).toUser()
  }

  override suspend fun findById(id: UserId): User? {
    return repository.findById(id)?.toUser()
  }

  override suspend fun existsByUsername(username: Username): Boolean {
    return repository.existsByUsername(username)
  }

  override suspend fun existsByUsernameOrEmail(username: Username, email: EmailAddress): Boolean {
    return repository.existsByUsernameOrEmail(username, email)
  }

  override suspend fun findByUsername(username: Username): User? {
    return repository.findByUsername(username)?.toUser()
  }
}

@Repository
interface UserR2dbcRepository : CoroutineCrudRepository<UserTable, UUID> {

  suspend fun save(user: UserTable): UserTable

  suspend fun existsByUsername(username: String): Boolean

  suspend fun existsByUsernameOrEmail(username: String, email: String): Boolean

  suspend fun findByUsername(username: String): UserTable?
}
