package s4got10dev.crypto.exchange.infrastructure.config.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfig {

  @Bean
  @Primary
  fun objectMapper(): ObjectMapper = JsonMapper.builder()
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    .build()
    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .registerModules(JavaTimeModule(), KotlinModule.Builder().build())
}
