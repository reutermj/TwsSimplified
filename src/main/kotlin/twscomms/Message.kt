package twscomms

internal sealed class Message {
    abstract fun process()
}

/**
 * Contains account information related to a requested [AccountSummaryTag].
 *
 * @param tag The account's attribute being recieved.
 * @param account The account associated with this message.
 * @param value The account's attribute's value.
 */
internal data class AccountSummaryMessage(val tag: String, val account: String, val value: Double) : Message() {
    override fun process() {
        val theAccount = Account[this.account]
        val theTag = AccountSummaryTag[this.tag]

        if(theAccount == null) {
            println("Account $account not registered. Ignoring message")
            return //todo should I return something that tells the loop to continue?
        }

        if(theTag == null) {
            println("Account Summary Tag $tag not registered. This probably shouldn't happen, but ignoring the message")
            return
        }

        theAccount.setAccountSummary(theTag, value)
    }
}

/**
 * Notifies when account summary request is processed.
 */
internal object AccountSummaryEnd : Message() {
    override fun process() { }
}

/**
 * Contains position size information.
 *
 * @param account The account associated with this message.
 * @param ticker The associated ticker.
 * @param quantity The account's attribute's value.
 */
internal data class StockQuantityMessage(val account: String, val ticker: String, val exchange: String, val currency: String, val quantity: Double) : Message() {
    override fun process() {
        val theAccount = Account[account]
        val stockTicker =
            StockTicker.getStockTicker(ticker) ?:
            StockTicker.register(ticker, exchange, currency)

        if(theAccount == null) {
            println("Account $account not registered. Ignoring message")
            return
        }

        theAccount.setPositionSize(stockTicker, quantity)
    }
}

/**
 * Contains last price information for a ticker.
 *
 * @param tickerId The associated tickerId.
 * @param price The last price of the ticker.
 */
internal data class StockPriceMessage(val tickerId: Int, val price: Double) : Message() {
    override fun process() {
        val ticker = TwsCommManager.reqidToStockTicker[tickerId]

        if(ticker == null) {
            println("ReqId ${tickerId} does not correspond to a registered request. This probably shouldn't happen, but ignoring message")
            return
        }

        PriceLookup.setLastPrice(ticker, price)
    }
}

/**
 * Contains open price information for a ticker.
 *
 * @param tickerId The associated tickerId.
 * @param price The open price of the ticker.
 */
internal data class StockOpenMessage(val tickerId: Int, val price: Double) : Message() {
    override fun process() {
        val ticker = TwsCommManager.reqidToStockTicker[tickerId]

        if(ticker == null) {
            println("ReqId ${tickerId} does not correspond to a registered request. This probably shouldn't happen, but ignoring message")
            return
        }

        PriceLookup.setOpenPrice(ticker, price)
    }
}

/**
 * Called when all positions have been processed
 */
internal object PositionEnd : Message() {
    override fun process() { }
}

/**
 * Identifies that an order has been filled.
 *
 * @param orderId The order that has been filled.
 */
internal data class OrderFilledMessage(val orderId: Int) : Message() {
    override fun process() {
        TwsCommManager.awaitingPositions = true
        TwsCommManager.awaitingAccountSummary = true
        TwsCommManager.cancelPositions()
        TwsCommManager.subscribePositions()
        TwsCommManager.cancelAccountSummary()
        TwsCommManager.subscribeAccountSummary()
    }
}

/**
 * Contains information related to an error reported by TWS.
 *
 * @param code The error code.
 * @param message The error message.
 */
internal data class ErrorMessage(val code: Int, val message: String) : Message() {
    override fun process() { }
}