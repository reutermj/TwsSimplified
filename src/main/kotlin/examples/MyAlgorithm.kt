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
    val DFCF = StockTicker.register("DFCF", "ARCA", "USD")

    val portfolio = mapOf(
        AVLV to .1,
        AVUV to .1,
        AVIV to .1,
        AVDV to .1,
        AVES to .1,
        DFCF to .5,
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

        val maint = margin.getAccountSummary(MaintMarginReq)
        val maint175 = maint * 1.75
        val net = margin.getAccountSummary(NetLiquidation)
        val gross = margin.getAccountSummary(GrossPositionValue)

        val survivableDrawdown = (net - maint) / (gross - maint)
        val survivableDrawdown175 = (net - maint175) / (gross - maint175)

        if(survivableDrawdown175 > .3) {
            //this is a simple trick to get the position that is most underweight relative to its target weight
            val sorted = portfolio.entries.sortedBy { margin.getMarketValue(it.key) / it.value }
            val underweight = sorted.first().key
            //target purchasing $2000 worth of shares rounded up to a whole share
            val quantity = ceil(2000.0 / underweight.price).toLong()
            margin.submitOrder(PatientBuyOrder, underweight, quantity)
        }

        if(survivableDrawdown < .1) {
            val quantity = (margin.getPositionSize(DFCF) / 10).toLong()
            margin.submitOrder(PatientSellOrder, DFCF, quantity)
        }
    }

    TwsCommManager.disconnect()
}