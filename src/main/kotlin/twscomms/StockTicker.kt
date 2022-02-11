package twscomms

import com.ib.client.Contract

class StockTicker private constructor(private val tickerString: String, private val primaryExchange: String, private val currency: String) {
    companion object {
        private val lookup = mutableMapOf<String, StockTicker>()

        fun register(tickerString: String, primaryExchange: String, currency: String): StockTicker {
            val ticker = StockTicker(tickerString, primaryExchange, currency)
            lookup[tickerString.lowercase()] = ticker
            return ticker
        }

        operator fun get(tickerString: String) = lookup[tickerString.lowercase()]
    }

    fun createContract(): Contract {
        val contract = Contract()
        contract.symbol(tickerString)
        contract.secType("STK")
        contract.currency(currency)
        contract.exchange("SMART")
        contract.primaryExch(primaryExchange)
        return contract
    }

    override fun toString() = tickerString
}

val AVLV = StockTicker.register("AVLV", "ARCA", "USD")
val AVUV = StockTicker.register("AVUV", "ARCA", "USD")
val AVIV = StockTicker.register("AVIV", "ARCA", "USD")
val AVDV = StockTicker.register("AVDV", "ARCA", "USD")
val AVES = StockTicker.register("AVES", "ARCA", "USD")
