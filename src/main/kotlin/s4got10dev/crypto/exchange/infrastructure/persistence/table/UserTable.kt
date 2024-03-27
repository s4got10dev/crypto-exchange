package s4got10dev.crypto.exchange.infrastructure.persistence.table

import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_account")
data class UserTable(
  @Id
  @Column("id")
  private val id: UUID?,

  @Column("username")
  val username: String,

  @Column("password")
  val password: String,

  @Column("email")
  val email: String,

  @Column("first_name")
  val firstName: String,

  @Column("last_name")
  val lastName: String,

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
