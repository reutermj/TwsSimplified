package twscomms

import com.ib.client.*

class OrderWrapper internal constructor  (val orderId: Int, val ticker: StockTicker, private var _filled: Int, private var _remaining: Int, val contract: Contract, val order: Order) {
    var filled: Int
        get() = _filled
        internal set(value) {
            _filled = value
        }

    var remaining: Int
        get() = _remaining
        internal set(value) {
            _remaining = value
        }

    internal val isInitialized: Boolean
        get() = _filled != -1 && _remaining != -1
}