package s4got10dev.crypto.exchange.interfaces.rest.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS_ME
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS_REGISTER
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.RegisterUserRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.UserResponse

@Tag(name = "user")
interface UserApi {

  @Operation(
    method = "POST",
    summary = "Register new user",
    description = "Register new user",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "User registered"
      ), ApiResponse(
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
  @PostMapping(API_V1_USERS_REGISTER)
  fun registerUser(@RequestBody request: RegisterUserRequest): Mono<ResponseEntity<Void>>

  @Operation(
    method = "GET",
    summary = "Get user",
    description = "Get user",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "User found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = UserResponse::class)
          )
        ]
      ), ApiResponse(
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
  @GetMapping(API_V1_USERS_ME)
  fun getUser(@AuthenticationPrincipal authPrincipal: AuthPrincipal?): Mono<ResponseEntity<UserResponse>>
}
