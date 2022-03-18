package twscomms

import com.ib.client.Contract

class StockTicker private constructor(private val tickerString: String, private val primaryExchange: String, private val currency: String) {
    companion object {
        private val lookup = mutableMapOf<String, StockTicker>()
        private val unregisteredTickers = mutableListOf<StockTicker>()

        fun register(tickerString: String, primaryExchange: String, currency: String) =
            lookup[tickerString.lowercase()] ?: run {
                val ticker = StockTicker(tickerString, primaryExchange, currency)
                lookup[tickerString.lowercase()] = ticker
                unregisteredTickers.add(ticker)
                ticker
            }

        fun arePricesInitialized() =
            lookup.values.fold(true) { acc, ticker -> acc && ticker.arePricesInitialized() }

        fun getTicker(tickerString: String) = lookup[tickerString.lowercase()]

        fun getUnregisteredTickers(): List<StockTicker> {
            val tickers = unregisteredTickers.toList()
            unregisteredTickers.clear()
            return tickers
        }
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

    internal var _bid: Double? = null
    internal var _ask: Double? = null
    internal var _price: Double? = null

    private fun arePricesInitialized() =
        _bid != null && _ask != null && _price != null

    val bid: Double
        get() = _bid!!

    val ask: Double
        get() = _ask!!

    val price: Double
        get() = _price!!
}
