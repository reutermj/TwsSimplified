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
     * Used to determine if price information is available for all given positions.
     *
     * @param positions The list of [StockTicker]
     * @return Whether all price information is available for [positions]
     */
    fun arePositionsInitialized(positions: List<StockTicker>): Boolean =
        positions.fold(true) {acc, ticker -> acc && getPrice(ticker) != 0.0 }

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