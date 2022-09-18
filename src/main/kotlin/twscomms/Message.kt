package twscomms

import com.ib.client.Contract
import com.ib.client.Order
import com.ib.client.OrderState

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
        val theAccount = Account.getAccount(this.account)
        val theTag = AccountSummaryTag.getTag(this.tag)

        if(theAccount == null) {
            println("Account $account not registered. Ignoring message")
            return //todo should I return something that tells the loop to continue?
        }

        if(theTag == null) {
            println("Account Summary Tag $tag not registered. This probably shouldn't happen, but ignoring the message")
            return
        }

        theAccount.accountSummary[theTag] = value
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
        val theAccount = Account.getAccount(account)
        val stockTicker =
            StockTicker.getTicker(ticker) ?:
            StockTicker.register(ticker, exchange, currency)

        if(theAccount == null) {
            println("Account $account not registered. Ignoring message")
            return
        }

        theAccount.positionSize[stockTicker] = quantity
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

        ticker._price = price
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

        if(ticker._price == null) ticker._price = price
    }
}

internal data class StockBidMessage(val tickerId: Int, val price: Double) : Message() {
    override fun process() {
        val ticker = TwsCommManager.reqidToStockTicker[tickerId]

        if(ticker == null) {
            println("ReqId ${tickerId} does not correspond to a registered request. This probably shouldn't happen, but ignoring message")
            return
        }

        ticker._bid = price
    }
}

internal data class StockAskMessage(val tickerId: Int, val price: Double) : Message() {
    override fun process() {
        val ticker = TwsCommManager.reqidToStockTicker[tickerId]

        if(ticker == null) {
            println("ReqId ${tickerId} does not correspond to a registered request. This probably shouldn't happen, but ignoring message")
            return
        }

        ticker._ask = price
    }
}

/**
 * Called when all positions have been processed
 */
internal object PositionEnd : Message() {
    override fun process() { }
}

/**
 * Identifies the status of an order.
 *
 * @param orderId The order.
 * @param status A string identifying the status of the order
 * @param filled number of shares filled as part of this order
 * @param remaining number of shares left to be filled
 */
internal data class OrderStatusMessage(val orderId: Int, val status: String, val filled: Long, val remaining: Long) : Message() {
    override fun process() {
        val account = TwsCommManager.reqidToAccount[orderId]
        if(account == null) {
            println("Account $account not registered. Ignoring message")
            return
        }

        val order = account.openOrders[orderId]
        if(order == null) {
            println("This is likely an error") //TODO handle
            return
        }

        val orderStatus =
            when(status) {
                "PendingSubmit" -> OrderWrapperStatus.PendingSubmit
                "PendingCancel" -> OrderWrapperStatus.PendingCancel
                "PreSubmitted" -> OrderWrapperStatus.PreSubmitted
                "Submitted" -> OrderWrapperStatus.Submitted
                "ApiCancelled" -> OrderWrapperStatus.ApiCancelled
                "Cancelled" -> OrderWrapperStatus.Cancelled
                "Filled" -> OrderWrapperStatus.Filled
                "Inactive" -> OrderWrapperStatus.Inactive
                else -> throw Exception("Got a weird order status") //TODO fix me
            }

        order._filled = filled
        order._remaining = remaining
        order._status = orderStatus

        if(orderStatus == OrderWrapperStatus.Filled) {
            account.openOrders.remove(orderId)

            //TODO determine order that messages are received to see if these are necessary
            //the purpose of this is that I want to make sure that all position and account
            //summary information is recent and accounts for the newly filled orders before
            //handing control back to the application

            //additional considerations: what about partially filled orders, it seems like
            //those would also need to have these kinds of checks in place

            TwsCommManager.arePositionsInitialized = false
            TwsCommManager.isAccountSummaryInitialized = false
            TwsCommManager.cancelPositions()
            TwsCommManager.subscribePositions()
            TwsCommManager.cancelAccountSummary()
            TwsCommManager.subscribeAccountSummary()
        }
    }
}

/**
 * Called when all open orders have been sent by TWS
 */
internal object OpenOrderEnd : Message() {
    override fun process() {}
}

/**
 *
 */
internal data class OpenOrderMessage(val orderId: Int, val contract: Contract, val order: Order, val state: OrderState) : Message() {
    override fun process() {
        val account = Account.getAccount(order.account())
        if(account == null) {
            println("Account $account not registered. Ignoring message")
            return
        }

        if(!TwsCommManager.reqidToAccount.contains(orderId))
            TwsCommManager.reqidToAccount[orderId] = account

        if(!account.openOrders.contains(orderId)) {
            val ticker = TwsCommManager.reqidToStockTicker[orderId]

            if(ticker != null)
                account.openOrders[orderId] = OrderWrapper(orderId, ticker, contract, order)
            else println("ReqId ${orderId} does not correspond to a registered request. This probably shouldn't happen, but ignoring message")
        }
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