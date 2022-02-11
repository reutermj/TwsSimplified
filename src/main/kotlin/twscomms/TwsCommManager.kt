package twscomms

import com.ib.client.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Used to manage communication between TWS and application logic.
 *
 * Communication with TWS uses a two thread architecture. A thread started by [beginMessageReader] waits for TWS to
 * send messages, parses the message into a [Message] object, and communicates the [Message] object to the application
 * thread via [messageQueue]. Messages sent to TWS are sent on the application thread.
 */
object TwsCommManager : EWrapperBase() {
    /**
     * Used to communicate parsed [Message] objects from the reader thread to the application thread.
     *
     * The message reader thread parses messages sent from TWS into [Message] objects, adds them to this queue, and
     * then the application thread can read the messages from this queue.
     */
    val messageQueue = ArrayBlockingQueue<Message>(1000)

    //Used for communication with TWS
    override val signal = EJavaSignal()
    override val client = EClientSocket(this, signal)

    //Store the requests
    private var reqid: Int = 1
    private val reqidToStockTicker = ConcurrentHashMap<Int, StockTicker>()

    //Keeps track of the order number.
    //TWS requires that the sequence of order numbers is ascending, and
    //this ascending sequence must be maintained between TWS sessions.
    //TWS does send the most recent order id at initial connection.
    private var nextOrderId = 1
    private val isOrderedFilled = mutableMapOf<Int, Boolean>()

    //The reqids of open account summary subscriptions.
    private val accountSummaryReqids = mutableListOf<Int>()

    /**
     * Connect to the TWS.
     *
     * @param ip The IP address of the computer with TWS running
     * @param port The port TWS is listening to.
     * Default ports: TWS live 7496, TWS paper 7497, IBGateway live 4001, IBGateway paper 4002.
     * @param clientId Identifies the client connection.
     * @return The [Thread] that the message reader is running on.
     */
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

    /**
     * Disconnect from the TWS.
     */
    fun disconnect() {
        client.eDisconnect()
    }

    /**
     * Subscribes to account summary information for all accounts.
     *
     * @param first Desired account summary information
     * @param rest Desired account summary information
     */
    fun subscribeAccountSummary(first: AccountSummaryTag, vararg rest: AccountSummaryTag) {
        val id = reqid
        reqid++
        val tags = rest.fold(first.toString()) { acc, tag -> "$acc,$tag" }
        client.reqAccountSummary(id, "All", tags)
        accountSummaryReqids.add(id)
    }

    /**
     * Cancel all open account summary subscriptions.
     */
    fun cancelAccountSummary() {
        for (id in accountSummaryReqids)
            client.cancelAccountSummary(id)

        accountSummaryReqids.clear()
    }

    /**
     * Subscribe to position size for all accounts.
     */
    fun subscribePositions() {
        client.reqPositions()
    }

    /**
     * Cancel position size subscription for all accounts.
     */
    fun cancelPositions() {
        client.cancelPositions()
    }

    /**
     * Subscribe to market data for each position in a [Portfolio].
     *
     * @param portfolio The [Portfolio] to request market data for.
     */
    fun subscribeMarketData(portfolio: Portfolio) {
        portfolio.tickers.forEach { subscribeMarketData(it) }
    }

    /**
     * Subscribe to market data for a specific [StockTicker].
     *
     * @param ticker The [StockTicker] to request market data for.
     */
    fun subscribeMarketData(ticker: StockTicker) {
        val id = reqid
        reqid++
        reqidToStockTicker[id] = ticker
        client.reqMarketDataType(3) //3=delayed, 4=delayed-frozen; 1, 2 require live data subscription
        //I dont know what the later 4 arguments do...
        client.reqMktData(id, ticker.createContract(), "", false, false, listOf())
    }

    /**
     * Submit an order.
     *
     * @param account The [Account] to submit an order for.
     * @param orderKind The kind of order to submit.
     * @param ticker The [StockTicker] of the stock to submit an order for.
     * @param quantity How many units of [ticker] to submit an order for.
     * @return The ID associated with the newly submitted order.
     */
    fun submitOrder(account: Account, orderKind: OrderKind, ticker: StockTicker, quantity: Long): Int {
        nextOrderId++
        client.placeOrder(nextOrderId, ticker.createContract(), orderKind.createOrder(account, quantity))
        return nextOrderId
    }

    //#region Incoming message handling
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

            if(ticker == null) println("TickerId $tickerId not registered. Ignoring message")

            //tick type 68 = Delayed last traded price
            //75 = The prior day's closing price
            //76 = Today's opening price
            //if price is stale when requested, 68 reports 0 for price
            else if(tickType == 68) messageQueue.add(StockPriceMessage(ticker, price))

            //used as a fallback if 68 returns 0
            else if(tickType == 76) messageQueue.add(StockOpenMessage(ticker, price))
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    override fun position(account: String, contract: Contract, pos: Decimal, avgCost: Double) {
        try {
            val theAccount = Account[account]
            val stockTicker = StockTicker[contract.symbol()]

            if(theAccount == null) println("Account $account not registered. Ignoring message")
            else if(stockTicker == null) println("Ticker ${contract.symbol()} not registered. Ignoring message")
            else messageQueue.add(StockQuantityMessage(theAccount, stockTicker, pos.longValue().toDouble()))
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

            if(theAccount == null) println("Account $account not registered. Ignoring message")
            else if(accountTag == null) println("Tag $tag not registered. Ignoring message")
            else messageQueue.add(AccountSummaryMessage(accountTag, theAccount, value.toDouble()))
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
    //#endregion
}