package s4got10dev.crypto.exchange.integration

import io.r2dbc.spi.ConnectionFactory
import java.util.UUID.fromString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.entity.Username
import s4got10dev.crypto.exchange.infrastructure.auth.TokenService

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = [TestCryptoExchangeApplication::class]
)
abstract class BaseApiIT {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var connectionFactory: ConnectionFactory

  @Autowired
  protected lateinit var tokenService: TokenService

  protected fun token(userId: UserId, username: Username): String {
    return tokenService.generateToken(userId, username)
  }

  protected fun token(userId: String, username: Username): String {
    return tokenService.generateToken(fromString(userId), username)
  }
}
