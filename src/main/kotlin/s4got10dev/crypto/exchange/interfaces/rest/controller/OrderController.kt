package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import s4got10dev.crypto.exchange.application.service.OrderService
import s4got10dev.crypto.exchange.infrastructure.auth.AuthPrincipal
import s4got10dev.crypto.exchange.interfaces.rest.API_V1_ORDERS_GET
import s4got10dev.crypto.exchange.interfaces.rest.adapter.OrderAdapter
import s4got10dev.crypto.exchange.interfaces.rest.model.OrderResponse
import s4got10dev.crypto.exchange.interfaces.rest.model.PlaceOrderRequest
import s4got10dev.crypto.exchange.interfaces.rest.openapi.OrderApi

@RestController
class OrderController(
  private val orderAdapter: OrderAdapter,
  private val orderService: OrderService
) : OrderApi {

  override fun placeOrder(authPrincipal: AuthPrincipal?, order: PlaceOrderRequest): Mono<ResponseEntity<Void>> {
    return orderAdapter.placeOrderCommand(authPrincipal?.userId, order)
      .flatMap { orderService.placeOrder(it) }
      .map { ResponseEntity.created(URI(API_V1_ORDERS_GET.replace("{id}", it.orderId.toString()))).build() }
  }

  override fun getOrder(authPrincipal: AuthPrincipal?, id: String): Mono<ResponseEntity<OrderResponse>> {
    return orderAdapter.orderQuery(authPrincipal?.userId, id)
      .flatMap { orderService.getOrder(it) }
      .map { ResponseEntity.ok(OrderResponse.from(it)) }
  }

  override fun getOrders(authPrincipal: AuthPrincipal?): Mono<ResponseEntity<List<OrderResponse>>> {
    return orderAdapter.ordersQuery(authPrincipal?.userId)
      .flatMap { orderService.getOrders(it) }
      .map { ResponseEntity.ok(it.map { order -> OrderResponse.from(order) }) }
  }

  override fun cancelOrder(authPrincipal: AuthPrincipal?, id: String): Mono<ResponseEntity<Void>> {
    return orderAdapter.cancelOrderCommand(authPrincipal?.userId, id)
      .flatMap { orderService.cancelOrder(it) }
      .then(Mono.fromCallable { ResponseEntity.accepted().build() })
  }
}
