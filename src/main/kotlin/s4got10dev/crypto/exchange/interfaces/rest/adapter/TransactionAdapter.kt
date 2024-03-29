package s4got10dev.crypto.exchange.interfaces.rest.adapter

import org.springframework.stereotype.Component
import s4got10dev.crypto.exchange.domain.entity.UserId
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.usecase.TransactionsQuery

@Component
class TransactionAdapter {

  fun transactionsQuery(userId: UserId?, page: Int, size: Int): TransactionsQuery {
    if (userId == null) {
      throw BadRequestError("Invalid user id")
    }
    if (page < 0 || size < 0) {
      throw BadRequestError("Invalid page or size")
    }
    return TransactionsQuery(userId, page, size)
  }
}
