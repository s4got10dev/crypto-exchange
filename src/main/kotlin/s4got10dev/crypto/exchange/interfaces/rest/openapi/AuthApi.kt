package s4got10dev.crypto.exchange.interfaces.rest.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_LOGIN
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_LOGOUT
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.LoginRequest

@Tag(name = "auth")
interface AuthApi {

  @Operation(
    method = "POST",
    summary = "Login user",
    description = "Login user",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "User logged in, auth cookie added to response"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PostMapping(API_V1_LOGIN)
  suspend fun login(@RequestBody request: LoginRequest): ResponseEntity<Unit>

  @Operation(
    method = "POST",
    summary = "Logout user",
    description = "Logout user",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "User logged out, auth cookie removed from response"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PostMapping(API_V1_LOGOUT)
  suspend fun logout(@AuthenticationPrincipal authPrincipal: AuthPrincipal?): ResponseEntity<Unit>
}
