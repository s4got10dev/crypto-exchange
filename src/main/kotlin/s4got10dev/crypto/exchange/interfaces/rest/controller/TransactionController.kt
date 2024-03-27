package s4got10dev.crypto.exchange.interfaces.rest.controller

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.application.service.TransactionService
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.adapter.TransactionAdapter
import s4got10dev.crypto.exchange.interfaces.rest.model.TransactionResponse
import s4got10dev.crypto.exchange.interfaces.rest.openapi.TransactionApi

@RestController
class TransactionController(
  private val transactionAdapter: TransactionAdapter,
  private val transactionService: TransactionService
) : TransactionApi {

  override fun getTransactions(
    authPrincipal: AuthPrincipal?,
    page: Int,
    size: Int
  ): Mono<ResponseEntity<Page<TransactionResponse>>> {
    return transactionAdapter.transactionsQuery(authPrincipal?.userId, page, size)
      .flatMap { transactionService.getTransactions(it) }
      .map { ResponseEntity.ok(it.map { transaction -> TransactionResponse.from(transaction) }) }
  }
}
