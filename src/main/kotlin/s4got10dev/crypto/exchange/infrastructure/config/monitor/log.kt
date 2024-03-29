package s4got10dev.crypto.exchange.infrastructure.config.monitor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.COOKIE
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono.empty

@Component
class LoggingFilter : CoWebFilter() {

  private val logger = KotlinLogging.logger {}

  override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
    val request = exchange.request

    if (request.path.value().startsWith("/actuator") || request.path.value().startsWith("/api-docs")) {
      chain.filter(exchange)
    }

    val requestId = request.id

    with(request) {
      val requestHeaders = headers.withoutAuthorization()
      logger.info { "[$requestId] Request: ($method) ${uri.path} Params: $queryParams Headers: $requestHeaders" }
    }

    exchange.response.beforeCommit {
      with(exchange.response) {
        val responseHeaders = headers.withoutAuthorization()
        logger.info { "[$requestId] Response: ${statusCode?.value()} Headers: $responseHeaders" }
      }
      empty()
    }

    return chain.filter(exchange)
  }
}

fun HttpHeaders.withoutAuthorization(): String {
  return this
    .filter { !it.key.equals(AUTHORIZATION, ignoreCase = true) && !it.key.equals(COOKIE, ignoreCase = true) }
    .entries
    .joinToString(", ") { (name, value) -> "$name=$value" }
}
