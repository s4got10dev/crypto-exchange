package s4got10dev.crypto.exchange.interfaces.rest.adapter

import jakarta.validation.Validator
import s4got10dev.crypto.exchange.domain.error.ValidationError

fun Validator.validateRequest(request: Any): ValidationError? {
  validate(request).let { violations ->
    if (violations.isNotEmpty()) {
      return ValidationError(
        violations.associate { it.propertyPath.toString() to it.message },
        "${violations.size} validation errors occurred"
      )
    }
  }
  return null
}
