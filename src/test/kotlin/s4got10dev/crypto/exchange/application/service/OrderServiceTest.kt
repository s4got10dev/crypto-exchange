package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono.empty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.FILLED
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.OPEN
import s4got10dev.crypto.exchange.domain.entity.OrderType.BUY
import s4got10dev.crypto.exchange.domain.entity.OrderType.SELL
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.error.BadRequestError
import s4got10dev.crypto.exchange.domain.error.NotFoundError
import s4got10dev.crypto.exchange.domain.repository.OrderRepository
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.domain.usecase.CancelOrderCommand
import s4got10dev.crypto.exchange.domain.usecase.Event
import s4got10dev.crypto.exchange.domain.usecase.OrderQuery
import s4got10dev.crypto.exchange.domain.usecase.OrdersQuery
import s4got10dev.crypto.exchange.domain.usecase.PlaceOrderCommand
import s4got10dev.crypto.exchange.domain.usecase.TransactionCreatedEvent

class OrderServiceTest {

  private val orderRepository = mockk<OrderRepository>()
  private val walletRepository = mockk<WalletRepository>()
  private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

  private val orderService = OrderService(orderRepository, walletRepository, applicationEventPublisher)

  @Test
  fun `placeOrder buy should return OrderPlacedEvent when order is placed`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val orderId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 2.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(USD to 3000.toBigDecimal()))
    val order = Order(orderId, userId, walletId, BUY, 2.toBigDecimal(), BTC, USD, OPEN)

    coEvery { walletRepository.findById(walletId) } returns wallet
    every { orderRepository.save(any()) } returns order.toMono()

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.orderId).isEqualTo(orderId)
        true
      }
      .verifyComplete()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) { orderRepository.save(any()) }
    verify(exactly = 2) { applicationEventPublisher.publishEvent(any<Event>()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder sell should return OrderPlacedEvent when order is placed`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val orderId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, SELL, 2.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(BTC to 4.toBigDecimal()))
    val order = Order(orderId, userId, walletId, SELL, 2.toBigDecimal(), BTC, USD, OPEN)

    coEvery { walletRepository.findById(walletId) } returns wallet
    every { orderRepository.save(any()) } returns order.toMono()

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectNextMatches {
        assertThat(it.orderId).isEqualTo(orderId)
        true
      }
      .verifyComplete()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    verify(exactly = 1) { orderRepository.save(any()) }
    verify(exactly = 2) { applicationEventPublisher.publishEvent(any<Event>()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder buy should return NotFoundError when wallet not found`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 2.toBigDecimal(), BTC, USD)

    coEvery { walletRepository.findById(walletId) } returns null

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(NotFoundError::class.java)
        assertThat(it.message).isEqualTo("Wallet not found")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder buy should return BadRequestError when wallet userId does not match`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 2.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, randomUUID(), "test", mutableMapOf(USD to 3000.toBigDecimal()))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Wallet not found")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder buy should return BadRequestError when amount is zero or negative`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 0.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(USD to 3000.toBigDecimal()))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Amount must be greater than 0")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder buy should return BadRequestError when base and quote currency are the same`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 2.toBigDecimal(), BTC, BTC)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(USD to 3000.toBigDecimal()))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Base and quote currency must be different")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder buy should return BadRequestError when insufficient balance to buy`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, BUY, 2.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(EUR to 1000.toBigDecimal()))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Quote currency balance should be positive")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `placeOrder sell should return BadRequestError when insufficient balance to sell`() {
    val walletId = randomUUID()
    val userId = randomUUID()
    val command = PlaceOrderCommand(userId, walletId, SELL, 2.toBigDecimal(), BTC, USD)
    val wallet = Wallet(walletId, userId, "test", mutableMapOf(USD to 3000.toBigDecimal()))

    coEvery { walletRepository.findById(walletId) } returns wallet

    val result = orderService.placeOrder(command)

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Insufficient balance to sell")
        true
      }
      .verify()

    coVerify(exactly = 1) { walletRepository.findById(any()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `getOrder should return Order when order is found`() {
    val orderId = randomUUID()
    val userId = randomUUID()
    val order = Order(orderId, userId, randomUUID(), BUY, 2.toBigDecimal(), BTC, USD, OPEN)

    every { orderRepository.findById(orderId) } returns order.toMono()

    val result = orderService.getOrder(OrderQuery(userId, orderId))

    StepVerifier.create(result)
      .expectNext(order)
      .verifyComplete()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `getOrder should return NotFoundError when order is not found`() {
    val orderId = randomUUID()
    val userId = randomUUID()

    every { orderRepository.findById(orderId) } returns empty()

    val result = orderService.getOrder(OrderQuery(userId, orderId))

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(NotFoundError::class.java)
        assertThat(it.message).isEqualTo("Order not found")
        true
      }
      .verify()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `getOrder should return BadRequestError when order is for another user`() {
    val orderId = randomUUID()
    val userId = randomUUID()
    val order = Order(orderId, randomUUID(), randomUUID(), BUY, 2.toBigDecimal(), BTC, USD, OPEN)

    every { orderRepository.findById(orderId) } returns order.toMono()

    val result = orderService.getOrder(OrderQuery(userId, orderId))

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(NotFoundError::class.java)
        assertThat(it.message).isEqualTo("Order not found")
        true
      }
      .verify()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `getOrders should return list of Orders when orders are found`() {
    val userId = randomUUID()
    val orders = listOf(
      Order(randomUUID(), userId, randomUUID(), BUY, 2.toBigDecimal(), BTC, USD, OPEN),
      Order(randomUUID(), userId, randomUUID(), BUY, 3.toBigDecimal(), BTC, USD, OPEN)
    )

    every { orderRepository.findAllByUserId(userId) } returns orders.toFlux()

    val result = orderService.getOrders(OrdersQuery(userId))

    StepVerifier.create(result)
      .expectNext(orders)
      .verifyComplete()

    verify(exactly = 1) { orderRepository.findAllByUserId(userId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `getOrders should return list of Orders when orders are not found`() {
    val userId = randomUUID()
    val orders = Flux.empty<Order>()

    every { orderRepository.findAllByUserId(userId) } returns orders

    val result = orderService.getOrders(OrdersQuery(userId))

    StepVerifier.create(result)
      .expectNext(emptyList())
      .verifyComplete()

    verify(exactly = 1) { orderRepository.findAllByUserId(userId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `cancelOrder should return empty Mono when order is canceled`() {
    val orderId = randomUUID()
    val userId = randomUUID()
    val order = Order(orderId, userId, randomUUID(), BUY, 2.toBigDecimal(), BTC, USD, OPEN)

    every { orderRepository.findById(orderId) } returns order.toMono()
    every { orderRepository.save(order) } returns order.toMono()

    val result = orderService.cancelOrder(CancelOrderCommand(userId, orderId))

    StepVerifier.create(result)
      .verifyComplete()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    verify(exactly = 1) { orderRepository.save(order) }
    verify(exactly = 1) { applicationEventPublisher.publishEvent(any<TransactionCreatedEvent>()) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `cancelOrder should return NotFoundError when order is not found`() {
    val orderId = randomUUID()
    val userId = randomUUID()

    every { orderRepository.findById(orderId) } returns empty()

    val result = orderService.cancelOrder(CancelOrderCommand(userId, orderId))

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(NotFoundError::class.java)
        assertThat(it.message).isEqualTo("Order not found")
        true
      }
      .verify()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }

  @Test
  fun `cancelOrder should return BadRequestError when order is not open`() {
    val orderId = randomUUID()
    val userId = randomUUID()
    val order = Order(orderId, userId, randomUUID(), BUY, 2.toBigDecimal(), BTC, USD, OPEN)

    every { orderRepository.findById(orderId) } returns order.copy(status = FILLED).toMono()

    val result = orderService.cancelOrder(CancelOrderCommand(userId, orderId))

    StepVerifier.create(result)
      .expectErrorMatches {
        assertThat(it).isInstanceOf(BadRequestError::class.java)
        assertThat(it.message).isEqualTo("Order cannot be canceled")
        true
      }
      .verify()

    verify(exactly = 1) { orderRepository.findById(orderId) }
    confirmVerified(walletRepository, orderRepository, applicationEventPublisher)
  }
}
