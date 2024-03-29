package s4got10dev.crypto.exchange.application.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.withPollDelay
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import s4got10dev.crypto.exchange.domain.entity.Currency.BTC
import s4got10dev.crypto.exchange.domain.entity.Currency.ETH
import s4got10dev.crypto.exchange.domain.entity.Currency.EUR
import s4got10dev.crypto.exchange.domain.entity.Currency.USD
import s4got10dev.crypto.exchange.domain.entity.Order
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.CANCELED
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.FILLED
import s4got10dev.crypto.exchange.domain.entity.OrderStatus.OPEN
import s4got10dev.crypto.exchange.domain.entity.OrderType.BUY
import s4got10dev.crypto.exchange.domain.entity.OrderType.SELL
import s4got10dev.crypto.exchange.domain.entity.Wallet
import s4got10dev.crypto.exchange.domain.repository.OrderRepository
import s4got10dev.crypto.exchange.domain.repository.WalletRepository
import s4got10dev.crypto.exchange.domain.usecase.MatchOrdersEvent
import s4got10dev.crypto.exchange.domain.usecase.OrderFilledTransactionCreatedEvent
import s4got10dev.crypto.exchange.infrastructure.api.price.PricingService

class OrderMatchingServiceTest {

  private val orderRepository = mockk<OrderRepository>()
  private val walletRepository = mockk<WalletRepository>()
  private val pricingService = mockk<PricingService>()
  private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

  private val orderMatchingService =
    OrderMatchingService(orderRepository, walletRepository, pricingService, applicationEventPublisher)

  @Test
  fun `full order matching`() {
    val base = BTC
    val quote = USD
    val buyWallet = Wallet(randomUUID(), randomUUID(), "buy wallet", mutableMapOf(quote to 1_000_000.toBigDecimal()))
    val sellWallet = Wallet(randomUUID(), randomUUID(), "sell wallet", mutableMapOf(base to 10.toBigDecimal()))

    val buyOrder = Order(randomUUID(), buyWallet.userId, buyWallet.id!!, BUY, 2.toBigDecimal(), base, quote, OPEN)
    val sellOrderCanceled =
      Order(randomUUID(), sellWallet.userId, sellWallet.id!!, SELL, 1.toBigDecimal(), base, quote, CANCELED)
    val sellOrder = Order(randomUUID(), sellWallet.userId, sellWallet.id!!, SELL, 2.toBigDecimal(), base, quote, OPEN)

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY)
    } returns listOf(buyOrder).toFlux()

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL)
    } returns listOf(sellOrderCanceled, sellOrder).toFlux()

    every { orderRepository.save(any()) } returns Mono.just(mockk<Order>())

    coEvery { walletRepository.findById(buyWallet.id!!) } returns buyWallet
    coEvery { walletRepository.findById(sellWallet.id!!) } returns sellWallet
    coEvery { walletRepository.save(any()) } returns mockk<Wallet>()

    every { pricingService.getPrice(base, quote) } returns 50_000.toBigDecimal()

    orderMatchingService.handleProcessOrderEvent(MatchOrdersEvent(base, quote))

    await withPollInterval ofMillis(100L) withPollDelay ofMillis(20L) atMost ofSeconds(30L) untilAsserted {
      verify(exactly = 1) {
        orderRepository.save(
          withArg {
            assertThat(it).hasFieldOrPropertyWithValue("id", buyOrder.id).hasFieldOrPropertyWithValue("status", FILLED)
          }
        )
      }
      verify(exactly = 1) {
        orderRepository.save(
          withArg {
            assertThat(it).hasFieldOrPropertyWithValue("id", sellOrder.id).hasFieldOrPropertyWithValue("status", FILLED)
          }
        )
      }
    }

    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(buyWallet.id)
          assertThat(it.getBalance(quote)).isEqualTo(900_000.toBigDecimal())
          assertThat(it.getBalance(base)).isEqualTo(2.toBigDecimal())
        }
      )
    }
    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(sellWallet.id)
          assertThat(it.getBalance(quote)).isEqualTo(100_000.toBigDecimal())
          assertThat(it.getBalance(base)).isEqualTo(8.toBigDecimal())
        }
      )
    }

    verify(exactly = 1) { pricingService.getPrice(base, quote) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL) }
    coVerify(exactly = 2) { walletRepository.findById(any()) }

    verify(exactly = 2) { applicationEventPublisher.publishEvent(any<OrderFilledTransactionCreatedEvent>()) }
    confirmVerified(orderRepository, walletRepository, pricingService, applicationEventPublisher)
  }

  @Test
  fun `partial order matching`() {
    val base = BTC
    val quote = EUR
    val buyWallet = Wallet(randomUUID(), randomUUID(), "buy wallet", mutableMapOf(quote to 1_000_000.toBigDecimal()))
    val sellWallet = Wallet(randomUUID(), randomUUID(), "sell wallet", mutableMapOf(base to 10.toBigDecimal()))

    val buyOrder = Order(randomUUID(), buyWallet.userId, buyWallet.id!!, BUY, 5.toBigDecimal(), base, quote, OPEN)
    val sellOrder = Order(randomUUID(), sellWallet.userId, sellWallet.id!!, SELL, 3.toBigDecimal(), base, quote, OPEN)

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY)
    } returns listOf(buyOrder).toFlux()

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL)
    } returns listOf(sellOrder).toFlux()

    every { orderRepository.save(any()) } returns Mono.just(mockk<Order>())

    coEvery { walletRepository.findById(buyWallet.id!!) } returns buyWallet
    coEvery { walletRepository.findById(sellWallet.id!!) } returns sellWallet
    coEvery { walletRepository.save(any()) } returns mockk<Wallet>()

    every { pricingService.getPrice(base, quote) } returns 50_000.toBigDecimal()

    orderMatchingService.handleProcessOrderEvent(MatchOrdersEvent(base, quote))

    await withPollInterval ofMillis(100L) withPollDelay ofMillis(20L) atMost ofSeconds(30L) untilAsserted {
      verify(exactly = 1) {
        orderRepository.save(
          withArg {
            assertThat(it).hasFieldOrPropertyWithValue("id", buyOrder.id).hasFieldOrPropertyWithValue("status", OPEN)
          }
        )
      }
      verify(exactly = 1) {
        orderRepository.save(
          withArg {
            assertThat(it).hasFieldOrPropertyWithValue("id", sellOrder.id).hasFieldOrPropertyWithValue("status", FILLED)
          }
        )
      }
    }

    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(buyWallet.id)
          assertThat(it.getBalance(quote)).isEqualTo(850_000.toBigDecimal())
          assertThat(it.getBalance(base)).isEqualTo(3.toBigDecimal())
        }
      )
    }
    coVerify(exactly = 1) {
      walletRepository.save(
        withArg {
          assertThat(it.id).isEqualTo(sellWallet.id)
          assertThat(it.getBalance(quote)).isEqualTo(150_000.toBigDecimal())
          assertThat(it.getBalance(base)).isEqualTo(7.toBigDecimal())
        }
      )
    }

    verify(exactly = 1) { pricingService.getPrice(base, quote) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL) }
    coVerify(exactly = 2) { walletRepository.findById(any()) }
    verify(exactly = 2) { applicationEventPublisher.publishEvent(any<OrderFilledTransactionCreatedEvent>()) }
    confirmVerified(orderRepository, walletRepository, pricingService, applicationEventPublisher)
  }

  @Test
  fun `order matching cancel order if no balance`() {
    val base = ETH
    val quote = EUR
    val buyWallet = Wallet(randomUUID(), randomUUID(), "buy wallet", mutableMapOf(quote to 1_000_000.toBigDecimal()))
    val sellWallet = Wallet(randomUUID(), randomUUID(), "sell wallet", mutableMapOf(base to 2.toBigDecimal()))

    val buyOrder = Order(randomUUID(), buyWallet.userId, buyWallet.id!!, BUY, 5.toBigDecimal(), base, quote, OPEN)
    val sellOrder = Order(randomUUID(), sellWallet.userId, sellWallet.id!!, SELL, 3.toBigDecimal(), base, quote, OPEN)

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY)
    } returns listOf(buyOrder).toFlux()

    every {
      orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL)
    } returns listOf(sellOrder).toFlux()

    every { orderRepository.save(any()) } returns Mono.just(mockk<Order>())

    coEvery { walletRepository.findById(buyWallet.id!!) } returns buyWallet
    coEvery { walletRepository.findById(sellWallet.id!!) } returns sellWallet
    coEvery { walletRepository.save(any()) } returns mockk<Wallet>()

    every { pricingService.getPrice(base, quote) } returns 50_000.toBigDecimal()

    orderMatchingService.handleProcessOrderEvent(MatchOrdersEvent(base, quote))

    await withPollInterval ofMillis(100L) withPollDelay ofMillis(20L) atMost ofSeconds(30L) untilAsserted {
      verify(exactly = 1) {
        orderRepository.save(
          withArg {
            assertThat(
              it
            ).hasFieldOrPropertyWithValue("id", sellOrder.id).hasFieldOrPropertyWithValue("status", CANCELED)
          }
        )
      }
    }

    verify(exactly = 1) { pricingService.getPrice(base, quote) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, BUY) }
    verify(exactly = 1) { orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(base, quote, SELL) }
    coVerify(exactly = 2) { walletRepository.findById(any()) }

    confirmVerified(orderRepository, walletRepository, pricingService, applicationEventPublisher)
  }
}
