package s4got10dev.crypto.exchange.infrastructure.auth

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.repository.UserRepository
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH

@Component
class UserSecurityContextRepository(
  private val tokenService: TokenService,
  private val userRepository: UserRepository
) : ServerSecurityContextRepository {

  override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> = mono {
    throw UnsupportedOperationException()
  }

  override fun load(exchange: ServerWebExchange?): Mono<SecurityContext?> = mono {
    val authCookie = exchange?.request?.cookies?.getFirst(COOKIE_AUTH)
      ?: return@mono null

    val principal = runCatching { tokenService.extractPrincipal(authCookie.value) }.getOrNull()
      ?: return@mono null

    val exist = userRepository.existsByUsername(principal.username)
    if (!exist) {
      return@mono null
    }
    val securityContext = SecurityContextImpl().apply {
      authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
    }
    return@mono securityContext
  }
}
