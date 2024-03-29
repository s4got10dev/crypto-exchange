package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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

  override suspend fun createWallet(authPrincipal: AuthPrincipal?, wallet: CreateWalletRequest): ResponseEntity<Unit> {
    val wallet = walletAdapter.createWalletCommand(authPrincipal?.userId, wallet)
      .let { walletService.createWallet(it) }
    return ResponseEntity.created(URI(API_V1_WALLETS_GET_BY_ID.replace("{id}", wallet.walletId.toString()))).build()
  }

  override suspend fun getWallet(authPrincipal: AuthPrincipal?, id: String): ResponseEntity<WalletResponse> {
    val wallet = walletAdapter.walletQuery(authPrincipal?.userId, id)
      .let { walletService.getWallet(it) }
    return ResponseEntity.ok().body(WalletResponse.from(wallet))
  }

  override suspend fun getWallets(authPrincipal: AuthPrincipal?): ResponseEntity<List<WalletResponse>> {
    val wallets = walletAdapter.walletsQuery(authPrincipal?.userId)
      .let { walletService.getWallets(it) }
    return ResponseEntity.ok().body(wallets.map { wallet -> WalletResponse.from(wallet) })
  }

  override suspend fun deposit(
    authPrincipal: AuthPrincipal?,
    id: String,
    request: DepositRequest
  ): ResponseEntity<WalletResponse> {
    val wallet = walletAdapter.depositCommand(id, request)
      .let { walletService.deposit(it) }
    return ResponseEntity.ok().body(WalletResponse.from(wallet))
  }

  override suspend fun withdrawal(
    authPrincipal: AuthPrincipal?,
    id: String,
    request: WithdrawalRequest
  ): ResponseEntity<WalletResponse> {
    val wallet = walletAdapter.withdrawCommand(id, request)
      .let { walletService.withdraw(it) }
    return ResponseEntity.ok().body(WalletResponse.from(wallet))
  }
}
