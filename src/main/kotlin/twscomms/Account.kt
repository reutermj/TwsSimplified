package twscomms

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

    private val accountSummary = mutableMapOf<AccountSummaryTag, Double>()
    private val positionSize = mutableMapOf<StockTicker, Double>()

    fun setAccountSummary(tag: AccountSummaryTag, value: Double) {
        accountSummary[tag] = value
    }

    fun getAccountSummary(tag: AccountSummaryTag) = accountSummary[tag] ?: 0.0

    fun setPositionSize(ticker: StockTicker, size: Double) {
        positionSize[ticker] = size
    }

    fun getPositionSize(ticker: StockTicker) = positionSize[ticker] ?: 0.0

    fun markAccountStale() {
        positionSize.clear()
        accountSummary.clear()
    }

    val maxSurvivableDrawdown: Double
        get() =
            if(isAccountSummaryInitialized(NetLiquidation, MaintMarginReq, GrossPositionValue)) {
                val net = getAccountSummary(NetLiquidation)
                val maint = getAccountSummary(MaintMarginReq)
                val gross = getAccountSummary(GrossPositionValue)
                (net - maint) / (gross - maint)
            }
            else 0.0

    val leverage: Double
        get() =
            if(isAccountSummaryInitialized(NetLiquidation, GrossPositionValue)) {
                val net = getAccountSummary(NetLiquidation)
                val gross = getAccountSummary(GrossPositionValue)
                gross / net
            }
            else 0.0

    fun isAccountSummaryInitialized(first: AccountSummaryTag, vararg rest: AccountSummaryTag): Boolean =
        rest.fold(accountSummary.contains(first)) { acc, tag -> acc && accountSummary.contains(tag) }


    fun isPortfolioInitialized(portfolio: Portfolio): Boolean =
        portfolio.tickers.fold(true) { acc, ticker -> acc && positionSize.contains(ticker) }
}