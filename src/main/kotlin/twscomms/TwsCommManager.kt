package twscomms

import com.ib.client.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Used to manage communication between TWS and application logic.
 *
 * Communication with TWS uses a two thread architecture. A thread started by [beginMessageReader] waits for TWS to
 * send messages, parses the message into a [Message] object, and communicates the [Message] object to the application
 * thread via [messageQueue]. Messages sent to TWS are sent on the application thread.
 */
object TwsCommManager {
    /**
     * Used to communicate parsed [Message] objects from the reader thread to the application thread.
     *
     * The message reader thread parses messages sent from TWS into [Message] objects, adds them to this queue, and
     * then the application thread can read the messages from this queue.
     */
    //Design Goal: This should be the only place where concurrency is needed to be managed between the appplication
    //thread and the reader thread. Generally, the reader thread should only put information into a message object
    //and pass that message object onto the application thread for the data to be processed.
    internal val messageQueue = ArrayBlockingQueue<Message>(1000)

    //Used for communication with TWS
    private val signal: EJavaSignal
        get() = TwsMessageReceiver.signal
    private val client: EClientSocket
        get() = TwsMessageReceiver.client

    //Store the requests
    private var reqid: Int = 1
    internal val reqidToStockTicker = mutableMapOf<Int, StockTicker>()

    //Keeps track of the order number.
    //TWS requires that the sequence of order numbers is ascending, and
    //this ascending sequence must be maintained between TWS sessions.
    //TWS does send the most recent order id at initial connection.
    private var nextOrderId = 1

    //The reqids of open account summary subscriptions.
    private val accountSummaryReqids = mutableListOf<Int>()

    private var readerThread: Thread? = null

    internal var awaitingAccountSummary = true
    internal var awaitingPositions = true

    /**
     * Connect to the TWS.
     *
     * @param ip The IP address of the computer with TWS running
     * @param port The port TWS is listening to.
     * Default ports: TWS live 7496, TWS paper 7497, IBGateway live 4001, IBGateway paper 4002.
     * @param clientId Identifies the client connection.
     * @throws Exception Throws when the reader has already started
     */
    fun start(ip: String = "127.0.0.1", port: Int = 4001, clientId: Int = 2) {
        if(readerThread != null) throw Exception("Reader already started")

        client.eConnect(ip, port, clientId)

        val reader = EReader(client, signal)
        reader.start()

        readerThread = thread {
            while (client.isConnected) {
                signal.waitForSignal()
                try {
                    reader.processMsgs()
                } catch (e: Exception) {
                    println("Exception ${e.message}")
                }
            }
        }

        subscribeAccountSummary()
        subscribeMarketData(StockTicker.getUnregisteredTickers())
        subscribePositions()
    }

    fun waitForUpdate() {
        do {
            when(val message = messageQueue.poll(20, TimeUnit.SECONDS)) {
                null -> continue
                AccountSummaryEnd -> awaitingAccountSummary = false
                PositionEnd -> awaitingPositions = false
                is ErrorMessage -> println(message.message)
                else -> message.process()
            }

            //get all tickers that are newly registered and request market data for them
            val unregisteredTickers = StockTicker.getUnregisteredTickers()
            for(ticker in unregisteredTickers)
                subscribeMarketData(ticker)
        } while(awaitingPositions || awaitingAccountSummary || !PriceLookup.arePositionsInitialized(StockTicker.getRegisteredTickers()))
    }

    /**
     * Disconnect from the TWS.
     */
    fun disconnect() {
        client.eDisconnect()
        readerThread?.join()
        readerThread = null
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

    /**
     * Subscribes to all account summary information for all accounts.
     */
    internal fun subscribeAccountSummary() {
        val id = reqid
        reqid++
        client.reqAccountSummary(id, "All", AccountSummaryTag.getAllTags())
        accountSummaryReqids.add(id)
    }

    /**
     * Cancel all open account summary subscriptions.
     */
    internal fun cancelAccountSummary() {
        for (id in accountSummaryReqids)
            client.cancelAccountSummary(id)

        accountSummaryReqids.clear()
    }

    /**
     * Subscribe to position size for all accounts.
     */
    internal fun subscribePositions() {
        client.reqPositions()
    }

    /**
     * Cancel position size subscription for all accounts.
     */
    internal fun cancelPositions() {
        client.cancelPositions()
    }

    /**
     * Subscribe to market data for each [StockTicker] in a [List].
     *
     * @param portfolio The [List] of [StockTicker] to request market data for.
     */
    private fun subscribeMarketData(tickers: List<StockTicker>) {
        tickers.forEach { subscribeMarketData(it) }
    }


    /**
     * Subscribe to market data for a specific [StockTicker].
     *
     * @param ticker The [StockTicker] to request market data for.
     */
    private fun subscribeMarketData(ticker: StockTicker) {
        val id = reqid
        reqid++
        reqidToStockTicker[id] = ticker
        client.reqMarketDataType(3) //3=delayed, 4=delayed-frozen; 1, 2 require live data subscription
        //I dont know what the later 4 arguments do...
        client.reqMktData(id, ticker.createContract(), "", false, false, listOf())
    }

    private object TwsMessageReceiver : EWrapperBase() {
        override val signal = EJavaSignal()
        override val client = EClientSocket(this, signal)

        //From a design perspective, these methods are called on the message reader thread.
        //To avoid concurrency issues, these methods are simply used to pass information to
        //the application thread. No "real" data processing should be handled here. Instead,
        //all information required to processing the data should be passed using message and
        //handled on the application thread.

        private val isOrderedFilled = mutableMapOf<Int, Boolean>()
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

        override fun openOrder(
            orderId: Int, contract: Contract, order: Order,
            orderState: OrderState
        ) {
            val ticker = StockTicker.getStockTicker(contract.symbol())
        }

        override fun tickPrice(tickerId: Int, field: Int, price: Double, attribs: TickAttrib) {
            //tick type 68 = Delayed last traded price
            //75 = The prior day's closing price
            //76 = Today's opening price
            //if price is stale when requested, 68 reports 0 for price
            if(field == 68) messageQueue.add(StockPriceMessage(tickerId, price))

            //used as a fallback if 68 returns 0
            else if(field == 76) messageQueue.add(StockOpenMessage(tickerId, price))
        }

        override fun position(account: String, contract: Contract, pos: Decimal, avgCost: Double) {
            messageQueue.add(StockQuantityMessage(account, contract.symbol(), contract.primaryExch(), contract.currency(), pos.longValue().toDouble()))
        }

        override fun positionEnd() {
            //called only after initial request
            messageQueue.add(PositionEnd)
        }

        override fun accountSummary(
            reqId: Int, account: String, tag: String,
            value: String, currency: String?
        ) {
            messageQueue.add(AccountSummaryMessage(tag, account, value.toDouble()))
        }

        override fun accountSummaryEnd(reqId: Int) {
            //called only after initial request
            messageQueue.add(AccountSummaryEnd)
        }

        override fun nextValidId(orderId: Int) {
            nextOrderId = orderId
            println("Next order id: $orderId")
        }

        override fun error(id: Int, errorCode: Int, errorMsg: String) {
            messageQueue.add(ErrorMessage(errorCode, errorMsg))
        }
    }


}