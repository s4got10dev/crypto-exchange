package s4got10dev.crypto.exchange.interfaces.rest.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import s4got10dev.crypto.exchange.domain.entity.EmailAddress
import s4got10dev.crypto.exchange.domain.entity.PlainPassword
import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username
import jakarta.validation.constraints.Email as ValidEmail

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RegisterUserRequest(
  @Schema(description = "Username", example = "john_doe")
  @get:Pattern(
    regexp = "^[a-z0-9_-]{3,32}\$",
    message = "Username must be between 3 and 32 alphanumeric characters with underscores and hyphens"
  )
  val username: Username?,

  @Schema(description = "Password", example = "P@ssw0rd")
  @get:Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
  val password: PlainPassword?,

  @Schema(description = "First Name", example = "John")
  @get:NotBlank(message = "First name is required")
  val firstName: String?,

  @Schema(description = "Last Name", example = "Doe")
  @get:NotBlank(message = "Last name is required")
  val lastName: String?,

  @Schema(description = "Email", example = "john.doe@mail.com")
  @get:ValidEmail(message = "Email must be valid")
  val email: EmailAddress?
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserResponse(
  @Schema(description = "User Id", example = "550e8400-e29b-41d4-a716-446655440000")
  val id: UserId,

  @Schema(description = "Username", example = "john_doe")
  val username: Username,

  @Schema(description = "First name", example = "John")
  val firstName: String,

  @Schema(description = "Last name", example = "Doe")
  val lastName: String,

  @Schema(description = "Email", example = "john.doe@mail.com")
  val email: EmailAddress
) {
  companion object {
    fun from(user: User): UserResponse {
      return UserResponse(
        id = user.id!!,
        username = user.username,
        firstName = user.firstName,
        lastName = user.lastName,
        email = user.email
      )
    }
  }
}
