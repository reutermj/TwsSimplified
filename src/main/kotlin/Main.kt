import java.util.concurrent.TimeUnit
import kotlin.math.ceil

fun main() {
    val portfolio = Portfolio(Pair(AVLV, .2), Pair(AVUV, .2), Pair(AVIV, .2), Pair(AVDV, .2), Pair(AVES, .2))
    val margin = Account.register("DU#######")

    val readerThread = TwsCommManager.beginMessageReader(port = 4002)
    val connectionError = TwsCommManager.messageQueue.poll(5, TimeUnit.SECONDS)
    if(connectionError != null && connectionError is ErrorMessage) {
        println(connectionError.message)
    }

    TwsCommManager.requestAccountSummary(NetLiquidation, GrossPositionValue, MaintMarginReq)
    TwsCommManager.requestPositions()
    TwsCommManager.requestMarketData(portfolio)

    var isOrderOpen = false

    while (true) {
        when(val message = TwsCommManager.messageQueue.poll(20, TimeUnit.SECONDS)) {
            null -> continue //message poll timed out
            is AccountSummaryMessage ->
                when(message.tag) {
                    GrossPositionValue -> message.account.gross = message.value
                    NetLiquidation -> message.account.net = message.value
                    MaintMarginReq -> message.account.maint = message.value
                }
            is StockQuantityMessage -> message.account.setPositionSize(message.ticker, message.quantity)
            is StockPriceMessage -> PriceLookup.setLastPrice(message.ticker, message.price)
            is StockOpenMessage -> PriceLookup.setOpenPrice(message.ticker, message.price)
            is OrderFilledMessage -> {
                //The order has been filled but potentially account information (NAV, position size, etc) hasn't been updated.
                //To ensure that automated decisions aren't made based on stale information,
                //mark the account as stale and request all account information again.

                margin.markAccountStale()
                //TODO clean out account messages from the queue?
                TwsCommManager.cancelPositions()
                TwsCommManager.requestPositions()
                TwsCommManager.cancelAccountSummary()
                TwsCommManager.requestAccountSummary(NetLiquidation, GrossPositionValue, MaintMarginReq)
                isOrderOpen = false
            }
            is ErrorMessage -> {
                when(message.code) {

                }
            }
        }

        //make sure everything is initialized, leverage ratio is below the floor, and there are no open orders
        if(margin.isInitialized && PriceLookup.isPortfolioInitialized(portfolio) &&
            margin.leverage < 1.5 && !isOrderOpen) {
            //this is a simple trick to get the position that is most underweight relative to its target weight
            val sorted = portfolio.tickers.sortedBy { (margin.getPositionSize(it) * PriceLookup.getPrice(it)) / portfolio.getWeight(it) }
            val underweight = sorted[0]
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
            TwsCommManager.submitOrder(margin, MarketBuyOrder, underweight, quantity)
            isOrderOpen = true
        }
    }

    TwsCommManager.disconnect()
}
