package s4got10dev.crypto.exchange.interfaces.rest.controller

import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.application.service.AuthService
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.infrastructure.auth.TokenService
import s4got10dev.crypto.exchange.interfaces.rest.COOKIE_AUTH
import s4got10dev.crypto.exchange.interfaces.rest.adapter.AuthAdapter
import s4got10dev.crypto.exchange.interfaces.rest.model.LoginRequest
import s4got10dev.crypto.exchange.interfaces.rest.openapi.AuthApi

@RestController
class AuthController(
  private val authService: AuthService,
  private val tokenService: TokenService,
  private val authAdapter: AuthAdapter
) : AuthApi {

  override fun login(request: LoginRequest): Mono<ResponseEntity<Void>> {
    return authAdapter.performLoginCommand(request)
      .flatMap { authService.login(it.username, it.password) }
      .map {
        val jwt = tokenService.generateToken(it)
        val authCookie = ResponseCookie.fromClientResponse(COOKIE_AUTH, jwt)
          .maxAge(4.hours.toJavaDuration())
          .path("/")
          .build()

        ResponseEntity.noContent()
          .header("Set-Cookie", authCookie.toString())
          .build()
      }
  }

  override fun logout(authPrincipal: AuthPrincipal?): Mono<ResponseEntity<Void>> {
    if (authPrincipal == null) {
      return BadRequestError("Not logged in").toMono()
    }
    val authCookie = ResponseCookie.fromClientResponse(COOKIE_AUTH, "")
      .maxAge(0)
      .path("/")
      .build()

    return just(
      ResponseEntity.noContent()
        .header("Set-Cookie", authCookie.toString())
        .build()
    )
  }
}
