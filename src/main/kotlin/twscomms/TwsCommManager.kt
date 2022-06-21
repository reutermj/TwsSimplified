package twscomms

import com.ib.client.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Used to manage communication between TWS and application logic.
 */
object TwsCommManager {
    /**
     * Connect to the TWS.
     *
     * @param ip The IP address of the computer with TWS running
     * @param port The port TWS is listening to.
     * Default ports: TWS live 7496, TWS paper 7497, IBGateway live 4001, IBGateway paper 4002.
     * @param clientId Identifies the client connection.
     * @throws Exception Throws when the reader has already started
     */
    fun connect(ip: String = "127.0.0.1", port: Int = 4001, clientId: Int = 2) {
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

    /**
     * Waits until account and stock data is initialized and an update has occurred.
     *
     * @return An error or null if none
     */
    fun waitForUpdate(): TwsError? {
        do {
            when(val message = messageQueue.poll(20, TimeUnit.SECONDS)) {
                null -> continue
                AccountSummaryEnd -> isAccountSummaryInitialized = true
                PositionEnd -> arePositionsInitialized = true
                is ErrorMessage -> return TwsError(message.code, message.message)
                else -> message.process()
            }

            //get all tickers that are newly registered and request market data for them
            val unregisteredTickers = StockTicker.getUnregisteredTickers()
            for(ticker in unregisteredTickers)
                subscribeMarketData(ticker)
        } while(arePositionsInitialized &&
            isAccountSummaryInitialized &&
            StockTicker.arePricesInitialized() &&
            Account.areOrdersInitialized())

        return null
    }

    /**
     * Disconnect from the TWS.
     */
    fun disconnect() {
        client.eDisconnect()
        readerThread?.join()
        readerThread = null
    }

    //region Internal Functionality

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

    internal var isAccountSummaryInitialized = false
    internal var arePositionsInitialized = false

    internal val reqidToAccount = mutableMapOf<Int, Account>()
    internal val orderIdToAccount = mutableMapOf<Int, Account>()

    internal fun submitOrder(account: Account, orderKind: OrderKind, ticker: StockTicker, quantity: Long): Int {
        nextOrderId++
        orderIdToAccount[nextOrderId] = account

        //Place the order with TWS
        val contract = ticker.createContract()
        val order = orderKind.createOrder(account, quantity)
        client.placeOrder(nextOrderId, contract, order)

        //Create the order in the account
        val orderWrapper = OrderWrapper(nextOrderId, ticker, contract, order)
        orderWrapper._remaining = quantity
        account.openOrders[nextOrderId] = orderWrapper

        return nextOrderId
    }

    internal fun subscribeAccountSummary() {
        val id = reqid
        reqid++
        val s = AccountSummaryTag.getAllTags()
        client.reqAccountSummary(id, "All", s)
        accountSummaryReqids.add(id)
    }

    internal fun cancelAccountSummary() {
        for (id in accountSummaryReqids)
            client.cancelAccountSummary(id)

        accountSummaryReqids.clear()
    }

    internal fun subscribePositions() {
        client.reqPositions()
    }

    internal fun cancelPositions() {
        client.cancelPositions()
    }

    private fun subscribeMarketData(tickers: List<StockTicker>) {
        tickers.forEach { subscribeMarketData(it) }
    }

    private fun subscribeMarketData(ticker: StockTicker) {
        val id = reqid
        reqid++
        reqidToStockTicker[id] = ticker
        client.reqMarketDataType(3) //3=delayed, 4=delayed-frozen; 1, 2 require live data subscription
        //I dont know what the later 4 arguments do...
        client.reqMktData(id, ticker.createContract(), "", false, false, listOf())
    }

    //endregion Internal Functionality

    //From a design perspective, the methods of this object are called on the message reader
    //thread. To avoid concurrency issues, these methods are simply used to pass information
    //to the application thread. No "real" data processing should be handled here. Instead,
    //all information required to processing the data should be passed using message and
    //handled on the application thread.
    private object TwsMessageReceiver : EWrapperBase() {
        override val signal = EJavaSignal()
        override val client = EClientSocket(this, signal)

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
            messageQueue.add(OrderStatusMessage(orderId, if(filled.isZero) 0 else filled.longValue(), if(remaining.isZero) 0 else remaining.longValue()))
        }

        override fun openOrder(
            orderId: Int, contract: Contract, order: Order,
            orderState: OrderState
        ) {
            messageQueue.add(OpenOrderMessage(orderId, contract, order, orderState))
        }

        override fun tickPrice(tickerId: Int, field: Int, price: Double, attribs: TickAttrib) {
            //if price is stale when requested, 68 reports 0 for price
            when(field) {
                4, 68 -> messageQueue.add(StockPriceMessage(tickerId, price))
                14, 76 -> messageQueue.add(StockOpenMessage(tickerId, price))
                1, 66 -> messageQueue.add(StockBidMessage(tickerId, price))
                2, 67 -> messageQueue.add(StockAskMessage(tickerId, price))
            }
        }

        override fun position(account: String, contract: Contract, pos: Decimal, avgCost: Double) {
            messageQueue.add(StockQuantityMessage(account, contract.symbol(), contract.exchange(), contract.currency(), pos.longValue().toDouble()))
        }

        override fun positionEnd() {
            //called only after initial request
            messageQueue.add(PositionEnd)
        }

        override fun accountSummary(
            reqId: Int, account: String, tag: String,
            value: String, currency: String?
        ) {
            try {
                messageQueue.add(AccountSummaryMessage(tag, account, value.toDouble()))
            } catch (_: Exception) {

            }
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