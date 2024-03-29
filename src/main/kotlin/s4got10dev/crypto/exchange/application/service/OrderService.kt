package s4got10dev.crypto.exchange.application.service

import java.math.BigDecimal.ZERO
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
  suspend fun placeOrder(command: PlaceOrderCommand): OrderPlacedEvent {
    val wallet = walletRepository.findById(command.walletId)
      ?: throw NotFoundError("Wallet not found")
    validateOrder(command, wallet)
    val order = orderRepository.save(command.toOrder())
    if (order.id == null) {
      throw InternalError("Order was not saved")
    }
    applicationEventPublisher.publishEvent(OrderPlacedTransactionCreatedEvent(order))
    applicationEventPublisher.publishEvent(MatchOrdersEvent(order.baseCurrency, order.quoteCurrency))
    return OrderPlacedEvent(order.id)
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
  suspend fun getOrder(query: OrderQuery): Order {
    val order = orderRepository.findById(query.orderId)
    if (order == null || order.userId != query.userId) {
      throw NotFoundError("Order not found")
    }
    return order
  }

  @Transactional(readOnly = true)
  suspend fun getOrders(query: OrdersQuery): List<Order> {
    return orderRepository.findAllByUserId(query.userId)
  }

  @Transactional
  suspend fun cancelOrder(command: CancelOrderCommand) {
    val order = orderRepository.findById(command.orderId)
      ?: throw NotFoundError("Order not found")
    if (order.status != OrderStatus.OPEN) {
      throw BadRequestError("Order cannot be canceled")
    }
    order.status = CANCELED
    val savedOder = orderRepository.save(order)
    applicationEventPublisher.publishEvent(OrderCancelledTransactionCreatedEvent(savedOder))
  }
}
