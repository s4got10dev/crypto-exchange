package s4got10dev.crypto.exchange.interfaces.rest.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_CREATE
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_DEPOSIT
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_GET_BY_ID
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_WALLETS_WITHDRAWAL
import s4got10dev.crypto.exchange.interfaces.rest.model.CreateWalletRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.DepositRequest
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WalletResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.WithdrawalRequest

@Tag(name = "wallet")
interface WalletApi {

  @Operation(
    method = "POST",
    summary = "Create new wallet",
    description = "Create new wallet",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Wallet created"
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PostMapping(API_V1_WALLETS_CREATE)
  fun createWallet(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @RequestBody wallet: CreateWalletRequest
  ): Mono<ResponseEntity<Void>>

  @Operation(
    method = "GET",
    summary = "Get wallet by id",
    description = "Get wallet by id",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Wallet found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Void::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Wallet not found",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @GetMapping(API_V1_WALLETS_GET_BY_ID)
  fun getWallet(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @PathVariable("id") id: String
  ): Mono<ResponseEntity<WalletResponse>>

  @Operation(
    method = "GET",
    summary = "Get all wallets",
    description = "Get all wallets",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Wallets found",
        content = [
          Content(array = ArraySchema(schema = Schema(implementation = WalletResponse::class)))
        ]
      )
    ]
  )
  @GetMapping(API_V1_WALLETS)
  fun getWallets(@AuthenticationPrincipal authPrincipal: AuthPrincipal?): Mono<ResponseEntity<List<WalletResponse>>>

  @Operation(
    method = "POST",
    summary = "Deposit to wallet",
    description = "Deposit to wallet",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Deposit accepted",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = WalletResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PostMapping(API_V1_WALLETS_DEPOSIT)
  fun deposit(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @PathVariable("id") id: String,
    @RequestBody request: DepositRequest
  ): Mono<ResponseEntity<WalletResponse>>

  @Operation(
    method = "POST",
    summary = "Withdraw from wallet",
    description = "Withdraw from wallet",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Withdrawal accepted",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = WalletResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PostMapping(API_V1_WALLETS_WITHDRAWAL)
  fun withdrawal(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @PathVariable("id") id: String,
    @RequestBody request: WithdrawalRequest
  ): Mono<ResponseEntity<WalletResponse>>
}
