package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import org.springframework.stereotype.Component
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.PerformLoginCommand
import s4got10dev.crypto.exchange.interfaces.rest.model.LoginRequest

@Component
class AuthAdapter(
  private val validator: Validator
) {

  fun performLoginCommand(request: LoginRequest): PerformLoginCommand {
    validator.validateRequest(request)?.let {
      throw it
    }
    if (request.username == null || request.password == null) {
      throw BadRequestError("Required fields are missing")
    }
    return PerformLoginCommand(username = request.username, password = request.password)
  }
}
