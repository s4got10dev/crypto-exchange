package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.PerformLoginCommand
import s4got10dev.crypto.exchange.interfaces.rest.model.LoginRequest

@Component
class AuthAdapter(
  private val validator: Validator
) {

  fun performLoginCommand(request: LoginRequest): Mono<PerformLoginCommand> {
    validator.validateRequest(request)?.let {
      return it.toMono()
    }
    if (request.username == null || request.password == null) {
      return BadRequestError("Required fields are missing").toMono()
    }
    return PerformLoginCommand(username = request.username, password = request.password).toMono()
  }
}
