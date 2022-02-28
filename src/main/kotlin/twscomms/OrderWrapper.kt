package twscomms

import com.ib.client.*

class OrderWrapper internal constructor  (val orderId: Int, val ticker: StockTicker, val quantity: Int, val contract: Contract, val order: Order)