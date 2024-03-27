package s4got10dev.crypto.exchange.interfaces.rest.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.interfaces.rest.mapper.toErrorResponse

@Component
@Order(-2)
class ErrorWebExceptionHandler(
  errorAttributes: ErrorAttributes,
  webProperties: WebProperties,
  applicationContext: ApplicationContext,
  serverCodecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {

  private val log = KotlinLogging.logger { }

  init {
    this.setMessageWriters(serverCodecConfigurer.writers)
    this.setMessageReaders(serverCodecConfigurer.readers)
  }

  override fun getRoutingFunction(errorAttributes: ErrorAttributes) =
    RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest -> renderErrorResponse(request) }

  private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse?> {
    val error = getError(request)
    val details: Map<String, String> = mapOf(
      "request_id" to request.exchange().request.id,
      "timestamp" to Instant.now().toString()
    )

    val errorResponse = error.toErrorResponse(request.path(), details)

    val httpStatus = HttpStatus.valueOf(errorResponse.status)
    if (httpStatus.is4xxClientError) {
      log.warn { errorResponse.details() }
    } else if (httpStatus.is5xxServerError) {
      log.error(error) { errorResponse.details() }
    }

    return ServerResponse.status(errorResponse.status)
      .contentType(APPLICATION_JSON)
      .body(BodyInserters.fromValue(errorResponse))
  }
}
