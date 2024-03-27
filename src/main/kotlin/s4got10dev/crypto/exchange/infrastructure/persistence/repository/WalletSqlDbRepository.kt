package s4got10dev.crypto.exchange.infrastructure.persistence.repository

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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

  override fun save(wallet: Wallet): Mono<Wallet> {
    return repository.save(wallet.toWalletTable(objectMapper)).map { it.toWallet(objectMapper) }
  }

  override fun existByUserIdAndName(userId: UUID, name: String): Mono<Boolean> {
    return repository.existsByUserIdAndName(userId, name)
  }

  override fun findById(id: UUID): Mono<Wallet> {
    return repository.findById(id).map { it.toWallet(objectMapper) }
  }

  override fun findAllByUserId(userId: UUID): Flux<Wallet> {
    return repository.findAllByUserId(userId).map { it.toWallet(objectMapper) }
  }
}

@Repository
interface WalletR2dbcRepository : ReactiveCrudRepository<WalletTable, UUID> {

  fun save(user: WalletTable): Mono<WalletTable>

  fun existsByUserIdAndName(userId: UUID, name: String): Mono<Boolean>

  fun findAllByUserId(userId: UUID): Flux<WalletTable>
}
