package s4got10dev.crypto.exchange.infrastructure.auth

import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username

data class AuthPrincipal(
  val userId: UserId,
  val username: Username
)
