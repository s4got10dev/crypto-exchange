package s4got10dev.crypto.exchange.interfaces.rest.mapper

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.CryptoExchangeError
import s4got10dev.crypto.exchange.domain.error.InternalError
import s4got10dev.crypto.exchange.domain.error.NonProcessableError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.error.UnauthorizedError
import s4got10dev.crypto.exchange.domain.error.ValidationError
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse

internal fun CryptoExchangeError.toErrorResponse(instance: String, details: Map<String, Any>): ErrorResponse =
  when (this) {
    is ValidationError -> ErrorResponse(type, title, BAD_REQUEST.value(), message, instance, details, violations)
    is BadRequestError -> ErrorResponse(type, title, BAD_REQUEST.value(), message, instance, details)
    is NotFoundError -> ErrorResponse(type, title, NOT_FOUND.value(), message, instance, details)
    is NonProcessableError -> ErrorResponse(type, title, UNPROCESSABLE_ENTITY.value(), message, instance, details)
    is UnauthorizedError -> ErrorResponse(type, title, UNAUTHORIZED.value(), message, instance, details)
    is InternalError -> ErrorResponse(type, title, INTERNAL_SERVER_ERROR.value(), message, instance, details)
  }

internal fun Throwable.toErrorResponse(instance: String, details: Map<String, Any>): ErrorResponse = when (this) {
  is CryptoExchangeError -> this.toErrorResponse(instance, details)
  else -> ErrorResponse(
    "internal-error",
    "Internal error occurred",
    INTERNAL_SERVER_ERROR.value(),
    message ?: "Unknown Error",
    instance,
    details
  )
}
