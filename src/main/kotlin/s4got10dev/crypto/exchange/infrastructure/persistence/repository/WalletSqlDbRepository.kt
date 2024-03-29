package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toWallet
import s4got10dev.crypto.exchange.infrastructure.persistence.mapper.toWalletTable
import s4got10dev.crypto.exchange.infrastructure.persistence.table.WalletTable

@Repository
@Transactional
class WalletSqlDbRepository(
  val repository: WalletR2dbcRepository,
  val objectMapper: ObjectMapper
) : WalletRepository {

  override suspend fun save(wallet: Wallet): Wallet {
    return repository.save(wallet.toWalletTable(objectMapper)).toWallet(objectMapper)
  }

  override suspend fun existByUserIdAndName(userId: UUID, name: String): Boolean {
    return repository.existsByUserIdAndName(userId, name)
  }

  override suspend fun findById(id: UUID): Wallet? {
    return repository.findById(id)?.toWallet(objectMapper)
  }

  override suspend fun findAllByUserId(userId: UUID): List<Wallet> {
    return repository.findAllByUserId(userId).map { it.toWallet(objectMapper) }
  }
}

@Repository
interface WalletR2dbcRepository : CoroutineCrudRepository<WalletTable, UUID> {

  suspend fun save(user: WalletTable): WalletTable

  suspend fun existsByUserIdAndName(userId: UUID, name: String): Boolean

  suspend fun findAllByUserId(userId: UUID): List<WalletTable>
}
