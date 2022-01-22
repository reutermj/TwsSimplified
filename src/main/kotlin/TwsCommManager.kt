import com.ib.client.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

object TwsCommManager : EWrapperBase() {
    val messageQueue = ArrayBlockingQueue<Message>(1000)
    override val signal = EJavaSignal()
    override val client = EClientSocket(this, signal)

    private var reqid: Int = 1
    private val reqidToStockTicker = ConcurrentHashMap<Int, StockTicker>()

    private var nextOrderId = 1
    private val isOrderedFilled = mutableMapOf<Int, Boolean>()

    private val accountSummaryReqids = mutableListOf<Int>()

    override fun orderStatus(
        orderId: Int,
        status: String,
        filled: Decimal,
        remaining: Decimal,
        avgFillPrice: Double,
        permId: Int,
        parentId: Int,
        lastFillPrice: Double,
        clientId: Int,
        whyHeld: String?,
        mktCapPrice: Double
    ) {
        if(isOrderedFilled[orderId] != true && remaining.isZero) {
            isOrderedFilled[orderId] = true
            messageQueue.add(OrderFilledMessage(orderId))
        }
    }

    override fun tickPrice(tickerId: Int, tickType: Int, price: Double, attribs: TickAttrib) {
        try {
            val ticker = reqidToStockTicker[tickerId]
            //tick type 68 = Delayed last traded price
            //75 = The prior day's closing price
            //76 = Today's opening price
            //if price is stale when requested, 68 reports 0 for price
            if(tickType == 68)
                if(ticker != null) messageQueue.add(StockPriceMessage(ticker, price))
                else println("TickerId $tickerId not registered. Ignoring message")

            //used as a fallback if 68 returns 0
            else if(tickType == 76)
                if(ticker != null) messageQueue.add(StockOpenMessage(ticker, price))
                else println("TickerId $tickerId not registered. Ignoring message")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    override fun position(account: String, contract: Contract, pos: Decimal, avgCost: Double) {
        try {
            val theAccount = Account[account]
            val stockTicker = StockTicker[contract.symbol()]

            if(theAccount != null)
                if(stockTicker != null) messageQueue.add(StockQuantityMessage(theAccount, stockTicker, pos.longValue().toDouble()))
                else println("Ticker ${contract.symbol()} not registered. Ignoring message")
            else println("Account $account not registered. Ignoring message")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    override fun accountSummary(
        reqId: Int, account: String, tag: String,
        value: String, currency: String?
    ) {
        try {
            val theAccount = Account[account]
            val accountTag = AccountSummaryTag[tag]

            if(theAccount != null)
                if(accountTag != null) messageQueue.add(AccountSummaryMessage(accountTag, theAccount, value.toDouble()))
                else println("Tag $tag not registered. Ignoring message")
            else println("Account $account not registered. Ignoring message")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    override fun nextValidId(orderId: Int) {
        nextOrderId = orderId
        println("Next order id: $orderId")
    }

    override fun error(id: Int, errorCode: Int, errorMsg: String) {
        println(id)
        messageQueue.add(ErrorMessage(errorCode, errorMsg))
    }

    fun beginMessageReader(ip: String = "127.0.0.1", port: Int = 4001, clientId: Int = 2): Thread {
        client.eConnect(ip, port, clientId)

        val reader = EReader(client, signal)
        reader.start()

        return thread {
            while (client.isConnected) {
                signal.waitForSignal()
                try {
                    reader.processMsgs()
                } catch (e: Exception) {
                    println("Exception ${e.message}")
                }
            }
        }
    }

    fun requestNextOrderId() {
        client.reqIds(-1)
    }

    fun disconnect() {
        client.eDisconnect()
    }

    fun requestAccountSummary(first: AccountSummaryTag, vararg rest: AccountSummaryTag) {
        val id = reqid
        reqid++
        val tags = rest.fold(first.toString()) { l, r -> "$l,$r" }
        client.reqAccountSummary(id, "All", tags)
        accountSummaryReqids.add(id)
    }

    fun cancelAccountSummary() {
        for (id in accountSummaryReqids)
            client.cancelAccountSummary(id)

        accountSummaryReqids.clear()
    }

    fun requestPositions() {
        client.reqPositions()
    }

    fun cancelPositions() {
        client.cancelPositions()
    }

    fun requestMarketData(portfolio: Portfolio) {
        portfolio.tickers.forEach { requestMarketData(it) }
    }

    fun requestMarketData(ticker: StockTicker) {
        val id = reqid
        reqid++
        reqidToStockTicker[id] = ticker
        client.reqMarketDataType(3) //3=delayed, 4=delayed-frozen; 1, 2 require live data subscription
        //I dont know what the later 4 arguments do...
        client.reqMktData(id, ticker.createContract(), "", false, false, listOf())
    }

    fun submitOrder(account: Account, orderKind: OrderKind, ticker: StockTicker, quantity: Long): Int {
        nextOrderId++
        client.placeOrder(nextOrderId, ticker.createContract(), orderKind.createOrder(account, quantity))
        return nextOrderId
    }
}