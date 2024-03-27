package s4got10dev.crypto.exchange.infrastructure.auth

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import s4got10dev.crypto.exchange.domain.repository.UserRepository
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH

@Component
class UserSecurityContextRepository(
  private val tokenService: TokenService,
  private val userRepository: UserRepository
) : ServerSecurityContextRepository {

  override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
    throw UnsupportedOperationException()
  }

  override fun load(exchange: ServerWebExchange?): Mono<SecurityContext> {
    val authCookie = exchange?.request?.cookies?.getFirst(COOKIE_AUTH) ?: return empty()

    val principal = runCatching { tokenService.extractPrincipal(authCookie.value) }
      .getOrElse { return empty() } ?: return empty()

    return userRepository.existsByUsername(principal.username)
      .filter { it }
      .map {
        SecurityContextImpl().apply {
          authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        }
      }
  }
}
