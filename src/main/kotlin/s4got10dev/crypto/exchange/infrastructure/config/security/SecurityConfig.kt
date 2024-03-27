package s4got10dev.crypto.exchange.infrastructure.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.ServerSecurityContextRepository

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class SecurityConfig {

  @Bean
  fun securityWebFilterChain(http: ServerHttpSecurity, repo: ServerSecurityContextRepository): SecurityWebFilterChain {
    return http
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .formLogin { it.disable() }
      .httpBasic { it.disable() }
      .authorizeExchange {
        it
          .pathMatchers("/api-docs/**").permitAll()
          .pathMatchers("/actuator/**").permitAll()
          .pathMatchers("/api/v1/users/register").permitAll()
          .pathMatchers("/api/v1/login").permitAll()
          .anyExchange().authenticated()
      }
      .securityContextRepository(repo)
      .build()
  }

  @Bean fun encoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }
}
