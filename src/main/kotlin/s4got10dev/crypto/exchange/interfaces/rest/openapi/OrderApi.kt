package s4got10dev.crypto.exchange.interfaces.rest.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_CANCEL
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_GET
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_PLACE
import s4got10dev.crypto.exchange.interfaces.rest.model.ErrorResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.OrderResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest

@Tag(name = "order")
interface OrderApi {

  @Operation(
    method = "POST",
    summary = "Place new order",
    description = "Place new order",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Order placed"
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
  @PostMapping(API_V1_ORDERS_PLACE)
  suspend fun placeOrder(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @RequestBody order: PlaceOrderRequest
  ): ResponseEntity<Unit>

  @Operation(
    method = "GET",
    summary = "Get order by id",
    description = "Get order by id",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Order found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrderResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Order not found",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @GetMapping(API_V1_ORDERS_GET)
  suspend fun getOrder(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @PathVariable id: String
  ): ResponseEntity<OrderResponse>

  @Operation(
    method = "GET",
    summary = "Get all orders",
    description = "Get all orders",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Orders found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OrderResponse::class)
          )
        ]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Orders not found",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @GetMapping(API_V1_ORDERS)
  suspend fun getOrders(@AuthenticationPrincipal authPrincipal: AuthPrincipal?): ResponseEntity<List<OrderResponse>>

  @Operation(
    method = "PATCH",
    summary = "Cancel order by id",
    description = "Cancel order by id",
    responses = [
      ApiResponse(
        responseCode = "202",
        description = "Order canceled"
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
      ),
      ApiResponse(
        responseCode = "404",
        description = "Order not found",
        content = [
          Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      )
    ]
  )
  @PatchMapping(API_V1_ORDERS_CANCEL)
  suspend fun cancelOrder(
    @AuthenticationPrincipal authPrincipal: AuthPrincipal?,
    @PathVariable id: String
  ): ResponseEntity<Unit>
}
