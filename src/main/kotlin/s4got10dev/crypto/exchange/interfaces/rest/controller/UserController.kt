package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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

  override suspend fun registerUser(request: RegisterUserRequest): ResponseEntity<Unit> {
    val user = userAdapter.createUserCommand(request)
      .let { userService.registerUser(it) }
    return ResponseEntity.created(URI(API_V1_USERS_GET_BY_ID.replace("{id}", user.userId.toString()))).build()
  }

  override suspend fun getUser(authPrincipal: AuthPrincipal?): ResponseEntity<UserResponse> {
    val user = userAdapter.userQuery(authPrincipal?.userId)
      .let { userService.getUser(it) }
    return ResponseEntity.ok(UserResponse.from(user))
  }
}
