package s4got10dev.crypto.exchange.domain.repository

import java.util.UUID
import s4got10dev.crypto.exchange.domain.entity.Wallet

interface WalletRepository {

  suspend fun save(wallet: Wallet): Wallet

  suspend fun existByUserIdAndName(userId: UUID, name: String): Boolean

  suspend fun findById(id: UUID): Wallet?

  suspend fun findAllByUserId(userId: UUID): List<Wallet>
}
