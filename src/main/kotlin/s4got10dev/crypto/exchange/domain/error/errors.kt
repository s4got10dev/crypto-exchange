package s4got10dev.crypto.exchange.domain.error

sealed class CryptoExchangeError(
  val type: ErrorType,
  val title: ErrorTitle,
  override val message: ErrorMessage,
  cause: Throwable? = null
) : RuntimeException(message, cause)

class ValidationError(val violations: Map<String, String>, message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "validation-error",
    "Request contains fields not passing validation",
    message.withCause(cause),
    cause
  )

class BadRequestError(message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "bad-request",
    "Bad request",
    message.withCause(cause),
    cause
  )

class NotFoundError(message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "not-found",
    "Resource not found",
    message.withCause(cause),
    cause
  )

class NonProcessableError(message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "non-processable",
    "Request cannot be processed",
    message.withCause(cause),
    cause
  )

class UnauthorizedError(message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "unauthorized",
    "Unauthorized",
    message.withCause(cause),
    cause
  )

internal class InternalError(message: ErrorMessage, cause: Throwable? = null) :
  CryptoExchangeError(
    "internal-error",
    "Internal error occurred",
    message.withCause(cause),
    cause
  )

internal fun ErrorMessage.withCause(cause: Throwable?): ErrorMessage {
  if (cause?.message != null) {
    return "$this caused by '${cause.message}'"
  }
  return this
}
