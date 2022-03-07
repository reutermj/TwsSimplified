package examples

import twscomms.*
import java.time.*
import kotlin.math.ceil

fun main() {
    val AVLV = StockTicker.register("AVLV", "ARCA", "USD")
    val AVUV = StockTicker.register("AVUV", "ARCA", "USD")
    val AVIV = StockTicker.register("AVIV", "ARCA", "USD")
    val AVDV = StockTicker.register("AVDV", "ARCA", "USD")
    val AVES = StockTicker.register("AVES", "ARCA", "USD")

    val portfolio = mapOf(
        AVLV to .2,
        AVUV to .2,
        AVIV to .2,
        AVDV to .2,
        AVES to .2
    )

    val margin = Account.register("DU#######")

    TwsCommManager.start(port = 4002)

    while (true) {
        TwsCommManager.waitForUpdate()

        val currentHour = Instant.now().atZone(ZoneId.of("America/New_York")).hour
        //don't make portfolio decisions before 11am EST
        if(currentHour < 10) continue
        //quit running at 2pm EST. 2 hours before trading closes
        if(currentHour >= 13) break

        if(margin.getOpenOrders().any()) continue

        if(margin.leverageRatio < 1.5) {
            //this is a simple trick to get the position that is most underweight relative to its target weight
            val sorted = portfolio.keys.sortedBy { margin.getMarketValue(it) / (portfolio[it] ?: 0.0) }
            val underweight = sorted.first()
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
            margin.submitOrder(PatientBuyOrder, underweight, quantity)
        }

        if(margin.leverageRatio > 1.9) {
            //this is a simple trick to get the position that is most overweight relative to its target weight
            val sorted = portfolio.keys.sortedByDescending { margin.getMarketValue(it) / (portfolio[it] ?: 0.0) }
            val overweight = sorted.first()
            //target selling $2000 worth of shares rounded up to a whole share
            val quantity = Math.max(ceil(2000.0 / PriceLookup.getPrice(overweight)), margin.getPositionSize(overweight)).toLong()
            margin.submitOrder(PatientSellOrder, overweight, quantity)
        }
    }

    TwsCommManager.disconnect()
}