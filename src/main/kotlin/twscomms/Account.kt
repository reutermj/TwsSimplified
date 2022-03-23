package twscomms

/**
 * Wraps functionality and data associated with account management.
 */
class Account private constructor(val accountId: String) {
    companion object {
        /**
         * Registers a new account or returns the account if already registered.
         *
         * @param accountId The id of the account.
         * @return The newly registered account or previously registered account.
         */
        fun register(accountId: String) =
            lookup[accountId.lowercase()] ?: run {
                val account = Account(accountId)
                lookup[accountId.lowercase()] = account
                account
            }

        //region Internal Functionality

        private val lookup = mutableMapOf<String, Account>()

        internal fun getAccount(accountId: String) = lookup[accountId.lowercase()]

        internal fun areOrdersInitialized() =
            lookup.values.fold(true) { acc, account -> acc && account.areOrdersInitialized() }

        //endregion Internal Functionality
    }

    /**
     * Gets the list of all positions in the account.
     *
     * @return The list of positions.
     */
    fun getPositions() = positionSize.keys.toList()

    /**
     * Gets a specific account summary value.
     *
     * @param tag The account summary tag.
     * @return The account summary value
     */
    fun getAccountSummary(tag: AccountSummaryTag) =
        accountSummary[tag] ?: throwUninitialized(tag.toString())

    /**
     * Gets the size of a position.
     *
     * @param ticker The position.
     * @return The size of the position.
     */
    fun getPositionSize(ticker: StockTicker) = positionSize[ticker] ?: 0.0

    /**
     * Gets the market value of a position.
     *
     * @param ticker The position.
     * @return The market value of the position.
     */
    fun getMarketValue(ticker: StockTicker) =
        getPositionSize(ticker) * ticker.price

    /**
     * Gets the list of all open orders in the account.
     *
     * @return The list of open orders.
     */
    fun getOpenOrders() =
        openOrders.values.toList()

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

    /**
     * Drawdown before a margin liquidation occurs.
     */
    val survivableDrawdown: Double
        get() {
            val net = getAccountSummary(NetLiquidation)
            val maint = getAccountSummary(MaintMarginReq)
            val gross = getAccountSummary(GrossPositionValue)
            return (net - maint) / (gross - maint)
        }

    /**
     * Leverage ratio as defined by Gross Position Value / Net Liquidation Value.
     */
    val leverageRatio: Double
        get() = getAccountSummary(GrossPositionValue) / getAccountSummary(NetLiquidation)

    //region Internal Functionality

    internal val openOrders = mutableMapOf<Int, OrderWrapper>()
    internal val accountSummary = mutableMapOf<AccountSummaryTag, Double>()
    internal val positionSize = mutableMapOf<StockTicker, Double>()

    internal fun areOrdersInitialized() =
        openOrders.values.fold(true) { acc, order -> acc && order.isInitialized }

    //endregion
}