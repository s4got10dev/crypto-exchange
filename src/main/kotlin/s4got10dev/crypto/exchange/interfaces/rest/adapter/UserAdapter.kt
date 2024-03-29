package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
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

  fun createUserCommand(request: RegisterUserRequest): CreateUserCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    if (request.username == null || request.email == null || request.firstName == null || request.lastName == null) {
      throw BadRequestError("Required fields are missing")
    }
    return CreateUserCommand(
      username = request.username,
      password = passwordEncoder.encode(request.password),
      firstName = request.firstName,
      lastName = request.lastName,
      email = request.email
    )
  }

  fun userQuery(userId: UserId?): UserQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    return UserQuery(userId = userId)
  }
}
