package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.domain.entity.Transaction
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.repository.TransactionRepository
import s4got10dev.crypto.exchange.domain.usecase.DepositCommand
import s4got10dev.crypto.exchange.domain.usecase.DepositTransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.TransactionsQuery

class TransactionServiceTest {

  private val transactionRepository = mockk<TransactionRepository>()

  private val transactionService = TransactionService(transactionRepository)

  @Test
  fun `should handle transaction event`() {
    val depositCommand = mockk<DepositCommand> {
      every { amount } returns 100.0.toBigDecimal()
      every { currency } returns USD
    }
    val wallet = mockk<Wallet> {
      every { id } returns randomUUID()
      every { userId } returns randomUUID()
    }

    coEvery { transactionRepository.save(any()) } returns mockk<Transaction>()

    transactionService.handleTransactionEvent(DepositTransactionCreatedEvent(depositCommand, wallet))

    coVerify(exactly = 1) { transactionRepository.save(any()) }
    confirmVerified(transactionRepository)
  }

  @Test
  fun `should get transactions`() {
    val userId = randomUUID()
    val page = 0
    val size = 10

    val transactionPage = mockk<Page<Transaction>>()

    coEvery { transactionRepository.findAllByUserId(userId, page, size) } returns transactionPage

    val result = runBlocking { transactionService.getTransactions(TransactionsQuery(userId, page, size)) }
    assertThat(result).isEqualTo(transactionPage)

    coVerify(exactly = 1) { transactionRepository.findAllByUserId(userId, page, size) }
    confirmVerified(transactionRepository)
  }
}
