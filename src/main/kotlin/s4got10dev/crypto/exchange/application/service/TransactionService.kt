package s4got10dev.crypto.exchange.application.service

import org.springframework.context.event.EventListener
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.Transaction
import s4got10dev.crypto.exchange.domain.repository.TransactionRepository
import s4got10dev.crypto.exchange.domain.usecase.TransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.TransactionsQuery

@Service
class TransactionService(
  private val transactionRepository: TransactionRepository
) {

  @EventListener
  fun handleTransactionEvent(event: TransactionCreatedEvent) {
    val transaction = Transaction(
      id = null,
      userId = event.userId,
      walletId = event.walletId,
      type = event.type,
      metadata = event.metadata
    )
    transactionRepository.save(transaction).subscribe()
  }

  @Transactional(readOnly = true)
  fun getTransactions(query: TransactionsQuery): Mono<Page<Transaction>> {
    return transactionRepository.findAllByUserId(query.userId, query.page, query.size)
  }
}
