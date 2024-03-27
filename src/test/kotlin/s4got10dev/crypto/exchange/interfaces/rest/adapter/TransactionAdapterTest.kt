package s4got10dev.crypto.exchange.interfaces.rest.adapter

import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.error.BadRequestError

class TransactionAdapterTest {

  private val transactionAdapter = TransactionAdapter()

  @Test
  fun `transactionsQuery should return TransactionsQuery when request is valid`() {
    val userId = randomUUID()
    val page = 0
    val size = 10

    val result = transactionAdapter.transactionsQuery(userId, page, size)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.userId).isEqualTo(userId)
        assertThat(it.page).isEqualTo(page)
        assertThat(it.size).isEqualTo(size)
        true
      }
      .verifyComplete()
  }

  @Test
  fun `transactionsQuery should return BadRequestError when user is null`() {
    val userId = null
    val page = -1
    val size = -1

    val result = transactionAdapter.transactionsQuery(userId, page, size)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Invalid user id")
        true
      }
      .verify()
  }

  @Test
  fun `transactionsQuery should return BadRequestError when page or size is invalid`() {
    val userId = randomUUID()
    val page = -1
    val size = -1

    val result = transactionAdapter.transactionsQuery(userId, page, size)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Invalid page or size")
        true
      }
      .verify()
  }
}
