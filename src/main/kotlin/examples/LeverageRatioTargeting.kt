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

    val margin = Account.register("U")

    TwsCommManager.connect()

    while (true) {
        val error = TwsCommManager.waitForUpdate()

        if(error != null) {
            println(error.message)
            continue
        }

        val currentHour = Instant.now().atZone(ZoneId.of("America/New_York")).hour
        //don't make portfolio decisions before 11am EST
        if(currentHour < 10) continue
        //quit running at 2pm EST. 2 hours before trading closes
        if(currentHour >= 13) break

        if(margin.getOpenOrders().any()) continue

        if(margin.leverage < 1.5) {
            //this is a simple trick to get the position that is most underweight relative to its target weight
            val sorted = portfolio.entries.sortedBy { margin.getMarketValue(it.key) / it.value }
            val underweight = sorted.first().key
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / underweight.price).toLong()
            //margin.submitOrder(PatientBuyOrder, underweight, quantity)
        }

        if(margin.leverage > 1.9) {
            //this is a simple trick to get the position that is most overweight relative to its target weight
            val sorted =
                margin.getPositions().sortedByDescending {
                    val weight = portfolio[it]
                    if(weight == null) Double.MAX_VALUE // sell anything that isn't in the portfolio first
                    else margin.getMarketValue(it) / weight // then sell what is overweight
                }
            val overweight = sorted.first()
            //target selling $2000 worth of shares rounded up to a whole share
            val quantity = Math.max(ceil(2000.0 / overweight.price), margin.getPositionSize(overweight)).toLong()
            //margin.submitOrder(PatientSellOrder, overweight, quantity)
        }
    }

    TwsCommManager.disconnect()
}