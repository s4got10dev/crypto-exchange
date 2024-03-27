package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.application.service.UserService
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_USERS_GET_BY_ID
import s4got10dev.crypto.exchange.interfaces.rest.adapter.UserAdapter
import s4got10dev.crypto.exchange.interfaces.rest.model.RegisterUserRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.UserResponse
import s4got10dev.crypto.exchange.interfaces.rest.openapi.UserApi

@RestController
class UserController(
  private val userService: UserService,
  private val userAdapter: UserAdapter
) : UserApi {

  override fun registerUser(request: RegisterUserRequest): Mono<ResponseEntity<Void>> {
    return userAdapter.createUserCommand(request)
      .flatMap { userService.registerUser(it) }
      .map { ResponseEntity.created(URI(API_V1_USERS_GET_BY_ID.replace("{id}", it.userId.toString()))).build() }
  }

  override fun getUser(authPrincipal: AuthPrincipal?): Mono<ResponseEntity<UserResponse>> {
    return userAdapter.userQuery(authPrincipal?.userId)
      .flatMap { userService.getUser(it) }
      .map { ResponseEntity.ok(UserResponse.from(it)) }
  }
}
