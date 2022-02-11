package twscomms

/**
 * Used to store and lookup price information for [StockTicker] instances.
 */
object PriceLookup {
    private val openPrice = mutableMapOf<StockTicker, Double>()
    private val lastPrice = mutableMapOf<StockTicker, Double>()

    /**
     * Gets the last price of a [StockTicker] or open price if the associated stock has
     * not traded that day.
     *
     * @param ticker The [StockTicker] to get price information for.
     * @return The last price of [ticker] or open price if it has not traded today.
     */
    fun getPrice(ticker: StockTicker): Double {
        val lp = lastPrice[ticker]
        val op = openPrice[ticker] ?: 0.0
        return if(lp == null || lp == 0.0) op
        else lp
    }

    /**
     * Used to determine if price information is available for all positions in a [Portfolio].
     *
     * @param portfolio The [Portfolio].
     * @return Whether all price information is available for [portfolio]
     */
    fun isPortfolioInitialized(portfolio: Portfolio): Boolean =
        portfolio.tickers.fold(true) {accum, ticker -> accum && getPrice(ticker) != 0.0 }

    /**
     * Sets the open price of a [StockTicker].
     * @param ticker The [StockTicker] to associate with [price].
     * @param price The open price of the [StockTicker].
     */
    fun setOpenPrice(ticker: StockTicker, price: Double) {
        openPrice[ticker] = price
    }

    /**
     * Sets the last price of a [StockTicker].
     * @param ticker The [StockTicker] to associate with [price].
     * @param price The last price of the [StockTicker].
     */
    fun setLastPrice(ticker: StockTicker, price: Double) {
        lastPrice[ticker] = price
    }
}