package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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

  override fun save(transaction: Transaction): Mono<Transaction> {
    return repository.save(transaction.toTransactionTable(objectMapper)).map { it.toTransaction(objectMapper) }
  }

  override fun findAllByUserId(userId: UUID, page: Int, size: Int): Mono<Page<Transaction>> {
    val pageRequest = PageRequest.of(page, size)
    return repository.findAllByUserIdOrderByCreatedAtDesc(userId, pageRequest).map { it.toTransaction(objectMapper) }
      .collectList()
      .zipWith(repository.countAllByUserId(userId))
      .map { PageImpl(it.t1, pageRequest, it.t2) }
  }
}

@Repository
interface TransactionR2dbcRepository : ReactiveSortingRepository<TransactionTable, UUID> {

  fun save(user: TransactionTable): Mono<TransactionTable>

  fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Flux<TransactionTable>

  fun countAllByUserId(userId: UUID): Mono<Long>
}
