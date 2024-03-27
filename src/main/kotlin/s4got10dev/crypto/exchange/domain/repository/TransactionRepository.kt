package s4got10dev.crypto.exchange.domain.repository

import java.util.UUID
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.Transaction

interface TransactionRepository {
  fun save(transaction: Transaction): Mono<Transaction>

  fun findAllByUserId(userId: UUID, page: Int, size: Int): Mono<Page<Transaction>>
}
