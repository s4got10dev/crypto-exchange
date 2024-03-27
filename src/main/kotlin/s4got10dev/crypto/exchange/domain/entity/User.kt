package s4got10dev.crypto.exchange.domain.entity

data class User(
  val id: UserId?,
  val username: Username,
  val password: EncodedPassword,
  val firstName: String,
  val lastName: String,
  val email: EmailAddress
)
