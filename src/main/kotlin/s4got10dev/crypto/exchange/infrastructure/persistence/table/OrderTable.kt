package s4got10dev.crypto.exchange.infrastructure.persistence.table

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import s4got10dev.crypto.exchange.domain.entity.Currency
import s4got10dev.crypto.exchange.domain.entity.OrderStatus
import s4got10dev.crypto.exchange.domain.entity.OrderType

@Table("trade_order")
data class OrderTable(
  @Id
  @Column("id")
  private val id: UUID?,

  @Column("user_id")
  val userId: UUID,

  @Column("wallet_id")
  val walletId: UUID,

  @Column("type")
  val type: OrderType,

  @Column("amount")
  val amount: BigDecimal,

  @Column("base_currency")
  val baseCurrency: Currency,

  @Column("quote_currency")
  val quoteCurrency: Currency,

  @Column("status")
  val status: OrderStatus,

  @CreatedDate
  @Column("created_at")
  val createdAt: Instant? = null,

  @LastModifiedDate
  @Column("updated_at")
  val updatedAt: Instant? = null,

  @Column("version")
  val version: Long = 0L
) : Persistable<UUID?> {
  override fun getId(): UUID? {
    return id ?: UUID.randomUUID()
  }

  override fun isNew(): Boolean {
    return id == null
  }
}
