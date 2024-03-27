package s4got10dev.crypto.exchange.interfaces.rest.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_TRANSACTIONS
import s4got10dev.crypto.exchange.interfaces.rest.model.TransactionResponse

@Tag(name = "transaction")
interface TransactionApi {

  @Operation(
    method = "GET",
    summary = "Get transactions",
    description = "Get transactions",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Transactions",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = TransactionPage::class)
          )
        ]
      )
    ]
  )
  @GetMapping(API_V1_TRANSACTIONS)
  fun getTransactions(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @RequestParam("page") page: Int = 0,
    @RequestParam("size") size: Int = 10
  ): Mono<ResponseEntity<Page<TransactionResponse>>>

  abstract class TransactionPage : Page<TransactionResponse>
}
