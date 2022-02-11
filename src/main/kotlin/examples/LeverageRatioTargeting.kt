import twscomms.*
import java.time.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

fun main() {
    val portfolio = Portfolio(Pair(AVLV, .2), Pair(AVUV, .2), Pair(AVIV, .2), Pair(AVDV, .2), Pair(AVES, .2))
    val margin = Account.register("DU#######")

    TwsCommManager.beginMessageReader(port = 4002)

    TwsCommManager.subscribeAccountSummary(NetLiquidation, GrossPositionValue, MaintMarginReq)
    TwsCommManager.subscribePositions()
    TwsCommManager.subscribeMarketData(portfolio)

    var isOrderOpen = false

    while (true) {
        when(val message = TwsCommManager.messageQueue.poll(20, TimeUnit.SECONDS)) {
            null -> {} //message poll timed out
            is AccountSummaryMessage -> message.account.setAccountSummary(message.tag, message.value)
            is StockQuantityMessage -> message.account.setPositionSize(message.ticker, message.quantity)
            is StockPriceMessage -> PriceLookup.setLastPrice(message.ticker, message.price)
            is StockOpenMessage -> PriceLookup.setOpenPrice(message.ticker, message.price)
            is OrderFilledMessage -> {
                //The order has been filled but potentially account information (NAV, position size, etc) hasn't been updated.
                //To ensure that automated decisions aren't made based on stale information,
                //mark the account as stale and request all account information again.
                margin.markAccountStale()
                TwsCommManager.cancelPositions()
                TwsCommManager.subscribePositions()
                TwsCommManager.cancelAccountSummary()
                TwsCommManager.subscribeAccountSummary(NetLiquidation, GrossPositionValue, MaintMarginReq)
                isOrderOpen = false
            }
        }

        val currentHour = Instant.now().atZone(ZoneId.of("America/New_York")).hour
        //don't make portfolio decisions before 11am EST
        if(currentHour < 10) continue
        //quit running at 2pm EST. 2 hours before trading closes
        if(currentHour >= 13) break

        //make sure everything is initialized and there are no open orders
        if(margin.isAccountSummaryInitialized(NetLiquidation, GrossPositionValue, MaintMarginReq) &&
            margin.isPortfolioInitialized(portfolio) &&
            PriceLookup.isPortfolioInitialized(portfolio) &&
            !isOrderOpen) {
            if(margin.leverage < 1.5) {
                //this is a simple trick to get the position that is most underweight relative to its target weight
                val sorted = portfolio.tickers.sortedBy { (margin.getPositionSize(it) * PriceLookup.getPrice(it)) / portfolio.getWeight(it) }
                val underweight = sorted.first()
                //target purchasing $2000 worth of shares rounded up to a whole share
                val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
                TwsCommManager.submitOrder(margin, PatientBuyOrder, underweight, quantity)
                isOrderOpen = true
            }

            if(margin.leverage > 1.8) {
                //this is a simple trick to get the position that is most overweight relative to its target weight
                val sorted = portfolio.tickers.sortedByDescending { (margin.getPositionSize(it) * PriceLookup.getPrice(it)) / portfolio.getWeight(it) }
                val underweight = sorted.first()
                //target purchasing $2000 worth of shares rounded up to a whole share
                val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
                TwsCommManager.submitOrder(margin, PatientSellOrder, underweight, quantity)
                isOrderOpen = true
            }
        }
    }

    TwsCommManager.disconnect()
}