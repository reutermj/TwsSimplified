class Account private constructor(val accountId: String) {
    companion object {
        private val lookup = mutableMapOf<String, Account>()

        fun register(accountId: String): Account {
            val account = Account(accountId)
            lookup[accountId.lowercase()] = account
            return account
        }

        operator fun get(accountId: String) = lookup[accountId.lowercase()]
    }

    private var isNetInitialized = false
    var net: Double = 0.0
        set(value) {
            isNetInitialized = true
            field = value
        }

    private var isGrossInitialized = false
    var gross: Double = 0.0
        set(value) {
            isGrossInitialized = true
            field = value
        }

    private var isMaintInitialized = false
    var maint: Double = 0.0
        set(value) {
            isMaintInitialized = true
            field = value
        }

    private val positionSize = mutableMapOf<StockTicker, Double>()

    fun setPositionSize(ticker: StockTicker, size: Double) {
        positionSize[ticker] = size
    }

    fun getPositionSize(ticker: StockTicker) = positionSize[ticker] ?: 0.0

    fun markAccountStale() {
        positionSize.clear()
        isNetInitialized = false
        isGrossInitialized = false
        isMaintInitialized = false
    }

    val maxSurvivableDrawdown: Double
        get() =
            if(isInitialized) (net - maint) / (gross - maint)
            else 0.0

    val leverage: Double
        get() =
            if(isInitialized) gross / net
            else 0.0

    val isInitialized: Boolean
        get() = isNetInitialized && isGrossInitialized &&
                isMaintInitialized

    fun isPortfolioInitialized(portfolio: Portfolio): Boolean =
        portfolio.tickers.fold(true) { accum, ticker -> accum && positionSize[ticker] != null }
}