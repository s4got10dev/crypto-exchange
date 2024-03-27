package s4got10dev.crypto.exchange.domain.repository

import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.domain.entity.Wallet

interface WalletRepository {
  fun save(wallet: Wallet): Mono<Wallet>

  fun existByUserIdAndName(userId: UUID, name: String): Mono<Boolean>

  fun findById(id: UUID): Mono<Wallet>

  fun findAllByUserId(userId: UUID): Flux<Wallet>
}
