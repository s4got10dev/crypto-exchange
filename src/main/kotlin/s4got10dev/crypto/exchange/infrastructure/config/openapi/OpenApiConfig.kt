package s4got10dev.crypto.exchange.infrastructure.config.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

  @Bean
  fun authOpenApi(): OpenAPI = OpenAPI()
    .info(
      Info()
        .title("Crypto Exchange Auth API")
        .description("Set of APIs for authentication, authorization and user management")
        .version("latest")
        .contact(Contact().name("Serhii Homeniuk"))
    )

  @Bean
  fun allOpenApi(): GroupedOpenApi = GroupedOpenApi.builder()
    .group("@all")
    .pathsToMatch("/**")
    .build()

  @Bean
  fun usersOpenApi(): GroupedOpenApi = GroupedOpenApi.builder()
    .group("users")
    .pathsToMatch("/api/v1/users/**")
    .build()
}
