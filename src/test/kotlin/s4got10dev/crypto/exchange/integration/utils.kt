package s4got10dev.crypto.exchange.integration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.core.io.Resource
import org.springframework.r2dbc.connection.init.ScriptUtils
import reactor.core.publisher.Mono

fun ConnectionFactory.executeSqlScript(sqlScript: Resource) {
  Mono.from(this.create())
    .flatMap { connection ->
      ScriptUtils.executeSqlScript(connection, sqlScript)
        .doFinally { connection.close() }
    }.block()
}
