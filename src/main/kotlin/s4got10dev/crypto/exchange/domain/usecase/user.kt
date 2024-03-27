package s4got10dev.crypto.exchange.domain.usecase

import s4got10dev.crypto.exchange.domain.entity.EmailAddress
import s4got10dev.crypto.exchange.domain.entity.EncodedPassword
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username

data class CreateUserCommand(
  val username: Username,
  val password: EncodedPassword,
  val firstName: String,
  val lastName: String,
  val email: EmailAddress
) : Command {

  fun toUser(): User {
    return User(
      id = null,
      username = username,
      password = password,
      firstName = firstName,
      lastName = lastName,
      email = email
    )
  }
}

data class UserQuery(val userId: UserId) : Query

data class UserCreatedEvent(
  val userId: UserId
) : Event
