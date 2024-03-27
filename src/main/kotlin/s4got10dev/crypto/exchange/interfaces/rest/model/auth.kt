package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import s4got10dev.crypto.exchange.domain.entity.PlainPassword
import s4got10dev.crypto.exchange.domain.entity.Username

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginRequest(

  @Schema(description = "Username", example = "john_doe")
  @get:NotBlank(message = "Username is required")
  val username: Username?,

  @Schema(description = "Password", example = "P@ssw0rd")
  @get:NotBlank(message = "Password is required")
  val password: PlainPassword?
)
