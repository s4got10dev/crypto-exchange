package s4got10dev.crypto.exchange.infrastructure.persistence.table

import io.r2dbc.postgresql.codec.Json
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet")
data class WalletTable(
  @Id
  @Column("id")
  private val id: UUID?,

  @Column("user_id")
  val userId: UUID,

  @Column("name")
  val name: String,

  @Column("balance")
  var balance: Json,

  @CreatedDate
  @Column("created_at")
  val createdAt: Instant? = null,

  @LastModifiedDate
  @Column("updated_at")
  val updatedAt: Instant? = null,

  @Version
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
