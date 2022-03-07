package twscomms

class Account private constructor(val accountId: String) {
    companion object {
        private val lookup = mutableMapOf<String, Account>()

        fun register(accountId: String): Account {
            val account = Account(accountId)
            lookup[accountId.lowercase()] = account
            return account
        }

        fun getAccount(accountId: String) = lookup[accountId.lowercase()]

        internal fun anyUninitaziledOrders() =
            lookup.values.fold(false) { acc, account -> acc || account.hasUninitializedOrders() }
    }

    internal val openOrders = mutableMapOf<Int, OrderWrapper>()
    private val accountSummary = mutableMapOf<AccountSummaryTag, Double>()
    private val positionSize = mutableMapOf<StockTicker, Double>()

    internal fun setAccountSummary(tag: AccountSummaryTag, value: Double) {
        accountSummary[tag] = value
    }

    fun getAccountSummary(tag: AccountSummaryTag) = accountSummary[tag] ?: 0.0

    internal fun setPositionSize(ticker: StockTicker, size: Double) {
        positionSize[ticker] = size
    }

    fun getPositionSize(ticker: StockTicker) = positionSize[ticker] ?: 0.0

    fun getMarketValue(ticker: StockTicker) =
        getPositionSize(ticker) * PriceLookup.getPrice(ticker)

    /**
     * Submit an order.
     *
     * @param orderKind The kind of order to submit.
     * @param ticker The [StockTicker] of the stock to submit an order for.
     * @param quantity How many units of [ticker] to submit an order for.
     * @return The ID associated with the newly submitted order.
     */
    fun submitOrder(orderKind: OrderKind, ticker: StockTicker, quantity: Long): Int =
        TwsCommManager.submitOrder(this, orderKind, ticker, quantity)

    fun getOpenOrders() =
        openOrders.values.toList()

    internal fun hasUninitializedOrders() =
        openOrders.values.fold(false) { acc, order -> acc || order.isUninitialized }

    val maxSurvivableDrawdown: Double
        get() {
            val net = getAccountSummary(NetLiquidation)
            val maint = getAccountSummary(MaintMarginReq)
            val gross = getAccountSummary(GrossPositionValue)
            return (net - maint) / (gross - maint)
        }

    val leverageRatio: Double
        get() {
            val gross = getAccountSummary(GrossPositionValue)
            val net = getAccountSummary(NetLiquidation)
            return gross / net
        }
}