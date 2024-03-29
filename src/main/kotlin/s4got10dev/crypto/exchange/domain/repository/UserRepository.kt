package s4got10dev.crypto.exchange.domain.repository

import s4got10dev.crypto.exchange.domain.entity.EmailAddress
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username

interface UserRepository {

  suspend fun save(user: User): User

  suspend fun findById(id: UserId): User?

  suspend fun existsByUsername(username: Username): Boolean

  suspend fun existsByUsernameOrEmail(username: Username, email: EmailAddress): Boolean

  suspend fun findByUsername(username: Username): User?
}
