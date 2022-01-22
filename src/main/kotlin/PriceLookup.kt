object PriceLookup {
    private val openPrice = mutableMapOf<StockTicker, Double>()
    private val lastPrice = mutableMapOf<StockTicker, Double>()

    fun getPrice(ticker: StockTicker): Double {
        val lp = lastPrice[ticker]
        val op = openPrice[ticker] ?: 0.0
        return if(lp == null || lp == 0.0) op
        else lp
    }

    fun isPortfolioInitialized(portfolio: Portfolio): Boolean =
        portfolio.tickers.fold(true) {l, r -> l && (getPrice(r) != 0.0) }

    fun setOpenPrice(ticker: StockTicker, price: Double) {
        openPrice[ticker] = price
    }

    fun setLastPrice(ticker: StockTicker, price: Double) {
        lastPrice[ticker] = price
    }
}