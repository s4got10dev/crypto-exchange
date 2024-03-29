package s4got10dev.crypto.exchange.infrastructure.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.springframework.stereotype.Service
import s4got10dev.crypto.exchange.domain.entity.Token
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username

const val JWT_SECRET = "secret-key"

@Service class TokenService {

  fun generateToken(user: User): Token {
    return generateToken(user.id, user.username)
  }

  fun generateToken(userId: UserId?, username: Username): Token {
    return JWT.create().withSubject(username).withClaim("userId", userId.toString())
      .withExpiresAt(Instant.now().plus(4, ChronoUnit.HOURS)).sign(Algorithm.HMAC256(JWT_SECRET))
  }

  fun extractPrincipal(token: Token): AuthPrincipal? {
    val decoded = runCatching { JWT.require(Algorithm.HMAC256(JWT_SECRET)).build().verify(token) }.getOrNull()

    val userId = runCatching { UUID.fromString(decoded?.getClaim("userId")?.asString()) }.getOrNull()

    val username = decoded?.subject
    if (userId == null || username == null) {
      return null
    }

    return AuthPrincipal(userId = userId, username = decoded.subject)
  }
}
