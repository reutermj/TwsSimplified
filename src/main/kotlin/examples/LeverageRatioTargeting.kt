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

        if(margin.leverage < 1.5) {
            //this is a simple trick to get the position that is most underweight relative to its target weight
            val sorted = portfolio.keys.sortedBy { (margin.getPositionSize(it) * PriceLookup.getPrice(it)) / (portfolio[it] ?: 0.0) }
            val underweight = sorted.first()
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
            TwsCommManager.submitOrder(margin, PatientBuyOrder, underweight, quantity)
            //todo figure out how to lock up orders?
            //todo probably list open orders in an account
        }

        if(margin.leverage > 1.9) {
            //this is a simple trick to get the position that is most overweight relative to its target weight
            val sorted = portfolio.keys.sortedByDescending { (margin.getPositionSize(it) * PriceLookup.getPrice(it)) / (portfolio[it] ?: 0.0) }
            val underweight = sorted.first()
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / PriceLookup.getPrice(underweight)).toLong()
            TwsCommManager.submitOrder(margin, PatientSellOrder, underweight, quantity)
        }
    }

    TwsCommManager.disconnect()
}