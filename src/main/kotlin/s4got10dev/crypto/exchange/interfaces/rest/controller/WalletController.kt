package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.application.service.WalletService
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_GET_BY_ID
import s4got10dev.crypto.exchange.interfaces.rest.adapter.WalletAdapter
import s4got10dev.crypto.exchange.interfaces.rest.model.CreateWalletRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.WalletResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WithdrawalRequest
import s4got10dev.crypto.exchange.interfaces.rest.openapi.WalletApi

@RestController
class WalletController(
  private val walletService: WalletService,
  private val walletAdapter: WalletAdapter
) : WalletApi {

  override fun createWallet(authPrincipal: AuthPrincipal?, wallet: CreateWalletRequest): Mono<ResponseEntity<Void>> {
    return walletAdapter.createWalletCommand(authPrincipal?.userId, wallet)
      .flatMap { command ->
        walletService.createWallet(command)
      }
      .map {
        ResponseEntity.created(URI(API_V1_WALLETS_GET_BY_ID.replace("{id}", it.walletId.toString()))).build()
      }
  }

  override fun getWallet(authPrincipal: AuthPrincipal?, id: String): Mono<ResponseEntity<WalletResponse>> {
    return walletAdapter.walletQuery(authPrincipal?.userId, id)
      .flatMap { walletService.getWallet(it) }
      .map { ResponseEntity.ok().body(WalletResponse.from(it)) }
  }

  override fun getWallets(authPrincipal: AuthPrincipal?): Mono<ResponseEntity<List<WalletResponse>>> {
    return walletAdapter.walletsQuery(authPrincipal?.userId)
      .flatMap { walletService.getWallets(it) }
      .map { ResponseEntity.ok().body(it.map { wallet -> WalletResponse.from(wallet) }) }
  }

  override fun deposit(
    authPrincipal: AuthPrincipal?,
    id: String,
    request: DepositRequest
  ): Mono<ResponseEntity<WalletResponse>> {
    return walletAdapter.depositCommand(id, request)
      .flatMap { walletService.deposit(it) }
      .map { ResponseEntity.ok().body(WalletResponse.from(it)) }
  }

  override fun withdrawal(
    authPrincipal: AuthPrincipal?,
    id: String,
    request: WithdrawalRequest
  ): Mono<ResponseEntity<WalletResponse>> {
    return walletAdapter.withdrawCommand(id, request)
      .flatMap { walletService.withdraw(it) }
      .map { ResponseEntity.ok().body(WalletResponse.from(it)) }
  }
}
