package s4got10dev.crypto.exchange.interfaces.rest.adapter

import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import s4got10dev.crypto.exchange.domain.error.BadRequestError

class TransactionAdapterTest {

  private val transactionAdapter = TransactionAdapter()

  @Test
  fun `transactionsQuery should return TransactionsQuery when request is valid`() {
    val userId = randomUUID()
    val page = 0
    val size = 10

    val result = transactionAdapter.transactionsQuery(userId, page, size)
    assertThat(result.userId).isEqualTo(userId)
    assertThat(result.page).isEqualTo(page)
    assertThat(result.size).isEqualTo(size)
  }

  @Test
  fun `transactionsQuery should return BadRequestError when user is null`() {
    val userId = null
    val page = -1
    val size = -1

    assertThatThrownBy { runBlocking { transactionAdapter.transactionsQuery(userId, page, size) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid user id")
  }

  @Test
  fun `transactionsQuery should return BadRequestError when page or size is invalid`() {
    val userId = randomUUID()
    val page = -1
    val size = -1

    assertThatThrownBy { runBlocking { transactionAdapter.transactionsQuery(userId, page, size) } }
      .isInstanceOf(BadRequestError::class.java)
      .hasMessage("Invalid page or size")
  }
}
