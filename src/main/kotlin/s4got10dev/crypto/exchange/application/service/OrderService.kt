package s4got10dev.crypto.exchange.application.service

import java.math.BigDecimal.ZERO
import kotlinx.coroutines.reactor.mono
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderStatus
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.CANCELED
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.OrderRepository
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.domain.usecase.CancelOrderCommand
import s4got10dev.crypto.exchange.domain.usecase.MatchOrdersEvent
import s4got10dev.crypto.exchange.domain.usecase.OrderCancelledTransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.OrderPlacedEvent
import s4got10dev.crypto.exchange.domain.usecase.OrderPlacedTransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.usecase.OrderQuery
import s4got10dev.crypto.exchange.domain.usecase.OrdersQuery
import s4got10dev.crypto.exchange.domain.usecase.PlaceOrderCommand
import s4got10dev.crypto.exchange.domain.utils.negativeOrZero
import s4got10dev.crypto.exchange.domain.utils.scaled

@Service
class OrderService(
  private val orderRepository: OrderRepository,
  private val walletRepository: WalletRepository,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

  @Transactional
  fun placeOrder(command: PlaceOrderCommand): Mono<OrderPlacedEvent> {
    return mono { walletRepository.findById(command.walletId) }
      .switchIfEmpty(NotFoundError("Wallet not found").toMono())
      .flatMap { wallet ->
        validateOrder(command, wallet)
        orderRepository.save(command.toOrder())
      }
      .flatMap { order ->
        if (order.id == null) {
          return@flatMap InternalError("Order was not saved").toMono()
        }
        applicationEventPublisher.publishEvent(OrderPlacedTransactionCreatedEvent(order))
        applicationEventPublisher.publishEvent(MatchOrdersEvent(order.baseCurrency, order.quoteCurrency))
        OrderPlacedEvent(order.id).toMono()
      }
  }

  private fun validateOrder(command: PlaceOrderCommand, wallet: Wallet) {
    if (wallet.userId != command.userId) {
      throw BadRequestError("Wallet not found")
    }
    if (command.amount.negativeOrZero()) {
      throw BadRequestError("Amount must be greater than 0")
    }
    if (command.baseCurrency == command.quoteCurrency) {
      throw BadRequestError("Base and quote currency must be different")
    }
    if (command.isBuy() && wallet.getBalance(command.quoteCurrency).scaled() <= ZERO.scaled()) {
      throw BadRequestError("Quote currency balance should be positive")
    }
    if (command.isSell() && wallet.getBalance(command.baseCurrency).scaled() <= command.amount.scaled()) {
      throw BadRequestError("Insufficient balance to sell")
    }
  }

  @Transactional(readOnly = true)
  fun getOrder(query: OrderQuery): Mono<Order> {
    return orderRepository.findById(query.orderId)
      .switchIfEmpty(NotFoundError("Order not found").toMono())
      .flatMap { order ->
        if (order.userId != query.userId) {
          NotFoundError("Order not found").toMono()
        } else {
          order.toMono()
        }
      }
  }

  @Transactional(readOnly = true)
  fun getOrders(query: OrdersQuery): Mono<List<Order>> {
    return orderRepository.findAllByUserId(query.userId).collectList()
  }

  @Transactional
  fun cancelOrder(command: CancelOrderCommand): Mono<Void> {
    return orderRepository.findById(command.orderId)
      .switchIfEmpty(NotFoundError("Order not found").toMono())
      .flatMap { order ->
        if (order.status != OrderStatus.OPEN) {
          BadRequestError("Order cannot be canceled").toMono()
        } else {
          order.status = CANCELED
          orderRepository.save(order)
        }
      }
      .map { order ->
        applicationEventPublisher.publishEvent(OrderCancelledTransactionCreatedEvent(order))
        order
      }
      .then()
  }
}
