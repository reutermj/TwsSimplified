package twscomms

import com.ib.client.*

 class OrderWrapper internal constructor  (val orderId: Int, val ticker: StockTicker, val contract: Contract, val order: Order) {
    fun cancelOrder() {
        TwsCommManager.cancelOrder(orderId)
    }

     val status: OrderWrapperStatus
        get() = _status ?: throwUninitialized("status")

    val filled: Long
        get() = _filled ?: throwUninitialized("filled")

    val remaining: Long
        get() = _remaining ?: throwUninitialized("remaining")

    //region Internal Functionality

    internal var _filled: Long? = null
    internal var _remaining: Long? = null
    internal var _status: OrderWrapperStatus? = null

    internal val isInitialized: Boolean
        get() = _filled != null && _remaining != null && _status != null

    //endregion Internal Functionality
}