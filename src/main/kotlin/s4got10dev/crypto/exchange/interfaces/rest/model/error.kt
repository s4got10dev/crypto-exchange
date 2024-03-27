package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import s4got10dev.crypto.exchange.domain.error.ErrorTitle
import s4got10dev.crypto.exchange.domain.error.ErrorType

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ErrorResponse(
  @Schema(
    description = "Type of error",
    example = "validation-error"
  )
  val type: ErrorType,

  @Schema(
    description = "Title of error",
    example = "Your request have invalid fields"
  )
  val title: ErrorTitle,

  @Schema(
    description = "HTTP status code",
    example = "400"
  )
  val status: Int,

  @Schema(
    description = "Detail of error",
    example = "Number of fields are invalid"
  )
  val detail: String,

  @Schema(
    description = "Instance of error",
    example = "/api/v1/hello-world"
  )
  val instance: String,

  @Schema(
    description = "Additional details of error",
    example = """{ "request_id": "1234567-8" }"""
  )
  val details: Map<String, Any>,

  @Schema(
    description = "Field level violations of error",
    example = """{ "field_x": "should be provided" }"""
  )
  val violations: Map<String, String>? = null
) {

  fun details() = "Request: '$instance', Error occurred: '$title' - '$detail'"
}
