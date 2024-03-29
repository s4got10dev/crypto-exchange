package s4got10dev.crypto.exchange.interfaces.rest.controller

import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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

  override suspend fun login(request: LoginRequest): ResponseEntity<Unit> {
    val command = authAdapter.performLoginCommand(request)
    val user = authService.login(command.username, command.password)

    val jwt = tokenService.generateToken(user)
    val authCookie = ResponseCookie.fromClientResponse(COOKIE_AUTH, jwt)
      .maxAge(4.hours.toJavaDuration())
      .path("/")
      .build()

    return ResponseEntity.noContent()
      .header("Set-Cookie", authCookie.toString())
      .build()
  }

  override suspend fun logout(authPrincipal: AuthPrincipal?): ResponseEntity<Unit> {
    if (authPrincipal == null) {
      throw BadRequestError("Not logged in")
    }
    val authCookie = ResponseCookie.fromClientResponse(COOKIE_AUTH, "")
      .maxAge(0)
      .path("/")
      .build()

    return ResponseEntity.noContent()
      .header("Set-Cookie", authCookie.toString())
      .build()
  }
}
