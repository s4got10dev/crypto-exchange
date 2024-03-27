package s4got10dev.crypto.exchange.infrastructure.persistence.table

import io.r2dbc.postgresql.codec.Json
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import s4got10dev.crypto.exchange.domain.entity.TransactionType

@Table("transaction")
data class TransactionTable(
  @Id
  @Column("id")
  private val id: UUID?,

  @Column("user_id")
  val userId: UUID,

  @Column("wallet_id")
  val walletId: UUID,

  @Column("type")
  val type: TransactionType,

  @Column("metadata")
  var metadata: Json,

  @CreatedDate
  @Column("created_at")
  val createdAt: Instant? = null
) : Persistable<UUID?> {
  override fun getId(): UUID? {
    return id ?: UUID.randomUUID()
  }

  override fun isNew(): Boolean {
    return true
  }
}
