package twscomms

import com.ib.client.Contract

/**
 * Wraps functionality and data associated with a stock.
 */
class StockTicker private constructor(private val ticker: String, private val primaryExchange: String, private val currency: String) {
    companion object {
        /**
         * Registers a new stock ticker or returns the stock ticker if already registered.
         *
         * @param ticker The ticker.
         * @param primaryExchange The primary exchange the stock trades on.
         * @param currency The currency the stock is denominated in.
         * @return The newly registered account or previously registered account.
         */
        fun register(ticker: String, primaryExchange: String, currency: String) =
            lookup[ticker.lowercase()] ?: run {
                val stock = StockTicker(ticker, primaryExchange, currency)
                lookup[ticker.lowercase()] = stock
                unregisteredTickers.add(stock)
                stock
            }

        //region Internal Functionality

        private val lookup = mutableMapOf<String, StockTicker>()
        private val unregisteredTickers = mutableListOf<StockTicker>()

        internal fun arePricesInitialized() =
            lookup.values.fold(true) { acc, ticker -> acc && ticker.arePricesInitialized() }

        internal fun getTicker(ticker: String) = lookup[ticker.lowercase()]

        internal fun getUnregisteredTickers(): List<StockTicker> {
            val tickers = unregisteredTickers.toList()
            unregisteredTickers.clear()
            return tickers
        }

        //endregion Internal Functionality
    }

    /**
     * The most recently received bid.
     */
    val bid: Double
        get() = _bid ?: throwUninitialized("bid")

    /**
     * The most recently received ask.
     */
    val ask: Double
        get() = _ask ?: throwUninitialized("ask")

    /**
     * The most recently received price: either the last trade price if one has occurred or the open price.
     */
    val price: Double
        get() = _price ?: throwUninitialized("price")

    override fun toString() = ticker

    //region Internal Functionality

    internal var _bid: Double? = null
    internal var _ask: Double? = null
    internal var _price: Double? = null

    internal fun createContract(): Contract {
        val contract = Contract()
        contract.symbol(ticker)
        contract.secType("STK")
        contract.currency(currency)
        contract.exchange("SMART")
        contract.primaryExch(primaryExchange)
        return contract
    }

    private fun arePricesInitialized() =
        _bid != null && _ask != null && _price != null

    //endregion Internal Functionality
}
