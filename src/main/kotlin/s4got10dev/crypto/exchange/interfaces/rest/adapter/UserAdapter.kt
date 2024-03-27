package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.CreateUserCommand
import s4got10dev.crypto.exchange.domain.usecase.UserQuery
import s4got10dev.crypto.exchange.interfaces.rest.model.RegisterUserRequest

@Component
class UserAdapter(
  private val validator: Validator,
  private val passwordEncoder: PasswordEncoder
) {

  fun createUserCommand(request: RegisterUserRequest): Mono<CreateUserCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    if (request.username == null || request.email == null || request.firstName == null || request.lastName == null) {
      return BadRequestError("Required fields are missing").toMono()
    }
    return CreateUserCommand(
      username = request.username,
      password = passwordEncoder.encode(request.password),
      firstName = request.firstName,
      lastName = request.lastName,
      email = request.email
    ).toMono()
  }

  fun userQuery(userId: UserId?): Mono<UserQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    return UserQuery(userId = userId).toMono()
  }
}
