package s4got10dev.crypto.exchange.integration

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import s4got10dev.crypto.exchange.CryptoExchangeApplication

@TestConfiguration(proxyBeanMethods = false)
class TestCryptoExchangeApplication {
  @Bean
  @ServiceConnection
  fun postgresContainer(): PostgreSQLContainer<*> {
    return PostgreSQLContainer(DockerImageName.parse("postgres:15.6-alpine"))
  }
}

fun main(args: Array<String>) {
  fromApplication<CryptoExchangeApplication>().with(TestCryptoExchangeApplication::class).run(*args)
}
