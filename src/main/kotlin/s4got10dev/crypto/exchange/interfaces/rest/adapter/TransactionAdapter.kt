package s4got10dev.crypto.exchange.interfaces.rest.adapter

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.TransactionsQuery

@Component
class TransactionAdapter {

  fun transactionsQuery(userId: UserId?, page: Int, size: Int): Mono<TransactionsQuery> {
    if (userId == null) {
      return BadRequestError("Invalid user id").toMono()
    }
    if (page < 0 || size < 0) {
      return BadRequestError("Invalid page or size").toMono()
    }
    return TransactionsQuery(userId, page, size).toMono()
  }
}
