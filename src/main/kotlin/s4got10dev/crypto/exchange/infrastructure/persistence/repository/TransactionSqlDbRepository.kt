package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import s4got10dev.crypto.exchange.domain.entity.Transaction
import s4got10dev.crypto.exchange.domain.repository.TransactionRepository
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toTransaction
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toTransactionTable
import s4got10dev.crypto.exchange.infrastructure.persistence.table.TransactionTable

@Repository
@Transactional
class TransactionSqlDbRepository(
  val repository: TransactionR2dbcRepository,
  val objectMapper: ObjectMapper
) : TransactionRepository {

  override suspend fun save(transaction: Transaction): Transaction {
    return repository.save(transaction.toTransactionTable(objectMapper)).toTransaction(objectMapper)
  }

  override suspend fun findAllByUserId(userId: UUID, page: Int, size: Int): Page<Transaction> {
    val pageRequest = PageRequest.of(page, size)


    val transactions = repository.findAllByUserIdOrderByCreatedAtDesc(userId, pageRequest).toList()
      .map { it.toTransaction(objectMapper) }
    val total = repository.countAllByUserId(userId)
    return PageImpl(transactions, pageRequest, total)
  }
}

@Repository
interface TransactionR2dbcRepository : CoroutineSortingRepository<TransactionTable, UUID> {

  suspend fun save(user: TransactionTable): TransactionTable

  fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Flow<TransactionTable>

  suspend fun countAllByUserId(userId: UUID): Long
}
