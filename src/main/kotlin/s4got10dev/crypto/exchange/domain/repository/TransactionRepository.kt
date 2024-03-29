package s4got10dev.crypto.exchange.domain.repository

import java.util.UUID
import org.springframework.data.domain.Page
import s4got10dev.crypto.exchange.domain.entity.Transaction

interface TransactionRepository {

  suspend fun save(transaction: Transaction): Transaction

  suspend fun findAllByUserId(userId: UUID, page: Int, size: Int): Page<Transaction>
}
