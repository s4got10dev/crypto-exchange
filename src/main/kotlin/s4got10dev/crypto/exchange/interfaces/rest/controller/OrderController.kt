package s4got10dev.crypto.exchange.interfaces.rest.controller

import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
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

  override suspend fun placeOrder(authPrincipal: AuthPrincipal?, order: PlaceOrderRequest): ResponseEntity<Unit> {
    val order = orderAdapter.placeOrderCommand(authPrincipal?.userId, order)
      .let { orderService.placeOrder(it) }
    return ResponseEntity.created(URI(API_V1_ORDERS_GET.replace("{id}", order.orderId.toString()))).build()
  }

  override suspend fun getOrder(authPrincipal: AuthPrincipal?, id: String): ResponseEntity<OrderResponse> {
    val order = orderAdapter.orderQuery(authPrincipal?.userId, id)
      .let { orderService.getOrder(it) }
    return ResponseEntity.ok(OrderResponse.from(order))
  }

  override suspend fun getOrders(authPrincipal: AuthPrincipal?): ResponseEntity<List<OrderResponse>> {
    val orders = orderAdapter.ordersQuery(authPrincipal?.userId)
      .let { orderService.getOrders(it) }
    return ResponseEntity.ok(orders.map { order -> OrderResponse.from(order) })
  }

  override suspend fun cancelOrder(authPrincipal: AuthPrincipal?, id: String): ResponseEntity<Unit> {
    orderAdapter.cancelOrderCommand(authPrincipal?.userId, id)
      .let { orderService.cancelOrder(it) }
    return ResponseEntity.accepted().build()
  }
}
