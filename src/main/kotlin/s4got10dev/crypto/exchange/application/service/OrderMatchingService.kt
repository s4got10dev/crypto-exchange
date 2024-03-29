package s4got10dev.crypto.exchange.application.service

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import s4got10dev.crypto.exchange.domain.entity.Currency
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
import s4got10dev.crypto.exchange.domain.usecase.OrderPartialFilledTransactionCreatedEvent
import s4got10dev.crypto.exchange.domain.utils.isZero
import s4got10dev.crypto.exchange.domain.utils.scaled
import s4got10dev.crypto.exchange.infrastructure.api.price.PricingService

@Service
class OrderMatchingService(
  private val orderRepository: OrderRepository,
  private val walletRepository: WalletRepository,
  private val pricingService: PricingService,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

  private val coroutineScope: CoroutineScope = CoroutineScope(IO)
  private val currencyMutexes = ConcurrentHashMap<String, Mutex>()

  @EventListener
  fun handleProcessOrderEvent(event: MatchOrdersEvent) {
    coroutineScope.launch {
      matchOrdersForCurrency(event.baseCurrency, event.quoteCurrency)
    }
  }

  private suspend fun matchOrdersForCurrency(baseCurrency: Currency, quoteCurrency: Currency) {
    val currency = "${baseCurrency.name}-${quoteCurrency.name}"
    val price = pricingService.getPrice(baseCurrency, quoteCurrency)
    currencyMutexes.computeIfAbsent(currency) { Mutex() }.withLock {
      val buyOrders =
        orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(baseCurrency, quoteCurrency, BUY).collectList()
          .awaitSingle()
      val sellOrders =
        orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndType(baseCurrency, quoteCurrency, SELL).collectList()
          .awaitSingle()

      matchOrders(buyOrders, sellOrders, price)
    }
  }

  private suspend fun matchOrders(buyOrders: List<Order>, sellOrders: List<Order>, price: BigDecimal) {
    buyOrders.forEach { buyOrder ->
      sellOrders.forEach { sellOrder ->
        if (buyOrder.status == OPEN && buyOrder.amount > ZERO && sellOrder.status == OPEN && sellOrder.amount > ZERO) {
          matchOrder(buyOrder, sellOrder, price)
        }
      }
    }
  }

  private suspend fun matchOrder(buyOrder: Order, sellOrder: Order, price: BigDecimal) {
    val buyOrderWallet = walletRepository.findById(buyOrder.walletId)
    val sellOrderWallet = walletRepository.findById(sellOrder.walletId)
    val tradeBaseAmount = minOf(buyOrder.amount, sellOrder.amount)
    val tradeQuoteAmount = tradeBaseAmount * price
    if (buyOrderWallet == null || !validateOrder(buyOrder, buyOrderWallet, tradeBaseAmount, tradeQuoteAmount)) {
      buyOrder.status = CANCELED
      orderRepository.save(buyOrder).awaitSingle()
      return
    }
    if (sellOrderWallet == null || !validateOrder(sellOrder, sellOrderWallet, tradeBaseAmount, tradeQuoteAmount)) {
      sellOrder.status = CANCELED
      orderRepository.save(sellOrder).awaitSingle()
      return
    }
    buyOrder.amount -= tradeBaseAmount
    sellOrder.amount -= tradeBaseAmount
    buyOrderWallet.addBalance(buyOrder.baseCurrency, tradeBaseAmount)
    buyOrderWallet.subtractBalance(buyOrder.quoteCurrency, tradeQuoteAmount)
    sellOrderWallet.subtractBalance(sellOrder.baseCurrency, tradeBaseAmount)
    sellOrderWallet.addBalance(sellOrder.quoteCurrency, tradeQuoteAmount)
    saveTrade(buyOrder, buyOrderWallet, tradeBaseAmount)
    saveTrade(sellOrder, sellOrderWallet, tradeBaseAmount)
  }

  private suspend fun validateOrder(
    order: Order,
    wallet: Wallet?,
    baseAmount: BigDecimal,
    quoteAmount: BigDecimal
  ): Boolean {
    return when (order.type) {
      BUY -> wallet?.getBalance(order.quoteCurrency).scaled() >= quoteAmount.scaled()
      SELL -> wallet?.getBalance(order.baseCurrency).scaled() >= baseAmount.scaled()
    }
  }

  private suspend fun saveTrade(order: Order, wallet: Wallet, baseAmount: BigDecimal) {
    if (order.amount.isZero()) {
      order.status = FILLED
    }
    orderRepository.save(order).awaitSingle()
    walletRepository.save(wallet)
    if (order.status == FILLED) {
      applicationEventPublisher.publishEvent(OrderFilledTransactionCreatedEvent(order, baseAmount))
    } else {
      applicationEventPublisher.publishEvent(OrderPartialFilledTransactionCreatedEvent(order, baseAmount))
    }
  }
}
