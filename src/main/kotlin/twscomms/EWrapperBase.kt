package twscomms

import com.ib.client.*

/**
 * Provides a default implementation for all EWrapper receiver functions.
 */
abstract class EWrapperBase : EWrapper {
    internal abstract val signal: EReaderSignal
    internal abstract val client: EClientSocket

    override fun tickPrice(tickerId: Int, field: Int, price: Double, attribs: TickAttrib) {
        println("Unimplemented API call: tickPrice")
    }

    override fun tickSize(tickerId: Int, field: Int, p2: Decimal) {
        println("Unimplemented API call: tickSize")
    }

    override fun tickOptionComputation(
        tickerId: Int, field: Int, tickAttrib: Int,
        impliedVol: Double, delta: Double, optPrice: Double,
        pvDividend: Double, gamma: Double, vega: Double, theta: Double,
        undPrice: Double
    ) {
        println("Unimplemented API call: tickOptionComputation")
    }

    override fun tickGeneric(tickerId: Int, tickType: Int, value: Double) {
        println("Unimplemented API call: tickGeneric")
    }

    override fun tickString(tickerId: Int, tickType: Int, value: String) {
        println("Unimplemented API call: tickString")
    }

    override fun tickEFP(
        tickerId: Int, tickType: Int, basisPoints: Double,
        formattedBasisPoints: String, impliedFuture: Double, holdDays: Int,
        futureLastTradeDate: String, dividendImpact: Double,
        dividendsToLastTradeDate: Double
    ) {
        println("Unimplemented API call: tickEFP")
    }

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
        println("Unimplemented API call: orderStatus")
    }

    override fun openOrder(
        orderId: Int, contract: Contract, order: Order,
        orderState: OrderState
    ) {
        println("Unimplemented API call: openOrder")
    }

    override fun openOrderEnd() {
        println("Unimplemented API call: openOrderEnd")
    }

    override fun updateAccountValue(
        key: String, value: String, currency: String,
        accountName: String
    ) {
        println("Unimplemented API call: updateAccountValue")
    }

    override fun updatePortfolio(
        contract: Contract,
        p1: Decimal,
        position: Double,
        marketPrice: Double,
        marketValue: Double,
        averageCost: Double,
        unrealizedPNL: Double,
        accountName: String
    ) {
        println("Unimplemented API call: updatePortfolio")
    }

    override fun updateAccountTime(timeStamp: String) {
        println("Unimplemented API call: updateAccountTime")
    }

    override fun accountDownloadEnd(accountName: String) {
        println("Unimplemented API call: accountDownloadEnd")
    }

    override fun nextValidId(orderId: Int) {
        println("Unimplemented API call: nextValidId")
    }

    override fun contractDetails(reqId: Int, contractDetails: ContractDetails) {
        println("Unimplemented API call: contractDetails")
    }

    override fun bondContractDetails(reqId: Int, contractDetails: ContractDetails) {
        println("Unimplemented API call: bondContractDetails")
    }

    override fun contractDetailsEnd(reqId: Int) {
        println("Unimplemented API call: contractDetailsEnd")
    }

    override fun execDetails(reqId: Int, contract: Contract, execution: Execution) {
        println("Unimplemented API call: execDetails")
    }

    override fun execDetailsEnd(reqId: Int) {
        println("Unimplemented API call: execDetailsEnd")
    }

    override fun updateMktDepth(tickerId: Int, position: Int, operation: Int, side: Int, price: Double, p5: Decimal) {
        println("Unimplemented API call: updateMktDepth")
    }

    override fun updateMktDepthL2(
        tickerId: Int,
        position: Int,
        marketMaker: String,
        operation: Int,
        side: Int,
        price: Double,
        p6: Decimal,
        isSmartDepth: Boolean
    ) {
        println("Unimplemented API call: updateMktDepthL2")
    }

    override fun updateNewsBulletin(
        msgId: Int, msgType: Int, message: String,
        origExchange: String
    ) {
        println("Unimplemented API call: updateNewsBulletin")
    }

    override fun managedAccounts(accountsList: String) {
        println("Unimplemented API call: managedAccounts")
    }

    override fun receiveFA(faDataType: Int, xml: String) {
        println("Unimplemented API call: receiveFA")
    }

    override fun historicalData(reqId: Int, bar: Bar) {
        println("Unimplemented API call: historicalData")
    }

    override fun historicalDataEnd(reqId: Int, startDateStr: String, endDateStr: String) {
        println("Unimplemented API call: historicalDataEnd")
    }

    override fun scannerParameters(xml: String) {
        println("Unimplemented API call: scannerParameters")
    }

    override fun scannerData(
        reqId: Int, rank: Int,
        contractDetails: ContractDetails, distance: String, benchmark: String,
        projection: String, legsStr: String
    ) {
        println("Unimplemented API call: scannerData")
    }

    override fun scannerDataEnd(reqId: Int) {
        println("Unimplemented API call: scannerDataEnd")
    }

    override fun realtimeBar(
        reqId: Int,
        time: Long,
        open: Double,
        high: Double,
        low: Double,
        close: Double,
        p6: Decimal,
        p7: Decimal,
        count: Int
    ) {
        println("Unimplemented API call: realtimeBar")
    }

    override fun currentTime(time: Long) {
        println("Unimplemented API call: currentTime")
    }

    override fun fundamentalData(reqId: Int, data: String) {
        println("Unimplemented API call: fundamentalData")
    }

    override fun deltaNeutralValidation(reqId: Int, deltaNeutralContract: DeltaNeutralContract) {
        println("Unimplemented API call: deltaNeutralValidation")
    }

    override fun tickSnapshotEnd(reqId: Int) {
        println("Unimplemented API call: tickSnapshotEnd")
    }

    override fun marketDataType(reqId: Int, marketDataType: Int) {
        println("Unimplemented API call: marketDataType")
    }

    override fun commissionReport(commissionReport: CommissionReport) {
        println("Unimplemented API call: commissionReport")
    }

    override fun position(account: String, contract: Contract, pos: Decimal, avgCost: Double) {
        println("Unimplemented API call: position")
    }

    override fun positionEnd() {
        println("Unimplemented API call: positionEnd")
    }

    override fun accountSummary(
        reqId: Int, account: String, tag: String,
        value: String, currency: String?
    ) {
        println("Unimplemented API call: accountSummary")
    }

    override fun accountSummaryEnd(reqId: Int) {
        println("Unimplemented API call: accountSummaryEnd")
    }

    override fun verifyMessageAPI(apiData: String) {
        println("Unimplemented API call: verifyMessageAPI")
    }

    override fun verifyCompleted(isSuccessful: Boolean, errorText: String) {
        println("Unimplemented API call: verifyCompleted")
    }

    override fun verifyAndAuthMessageAPI(apiData: String, xyzChallenge: String) {
        println("Unimplemented API call: verifyAndAuthMessageAPI")
    }

    override fun verifyAndAuthCompleted(isSuccessful: Boolean, errorText: String) {
        println("Unimplemented API call: verifyAndAuthCompleted")
    }

    override fun displayGroupList(reqId: Int, groups: String) {
        println("Unimplemented API call: displayGroupList")
    }

    override fun displayGroupUpdated(reqId: Int, contractInfo: String) {
        println("Unimplemented API call: displayGroupUpdated")
    }

    override fun error(e: Exception) {
        println("Unimplemented API call: error 1; ${e.message}")
    }

    override fun error(str: String) {
        println("Unimplemented API call: error 2; $str")
    }

    override fun error(id: Int, errorCode: Int, errorMsg: String) {
        println("Unimplemented API call: error 3; $errorMsg")
    }

    override fun connectionClosed() {
        println("Unimplemented API call: connectionClosed")
    }

    override fun connectAck() {
        if (client.isAsyncEConnect) {
            println("Acknowledging connection")
            client.startAPI()
        }
    }

    override fun positionMulti(
        reqId: Int,
        account: String,
        modelCode: String,
        contract: Contract,
        p4: Decimal,
        pos: Double
    ) {
        println("Unimplemented API call: positionMulti")
    }

    override fun positionMultiEnd(reqId: Int) {
        println("Unimplemented API call: positionMultiEnd")
    }

    override fun accountUpdateMulti(
        reqId: Int, account: String, modelCode: String,
        key: String, value: String, currency: String
    ) {
        println("Unimplemented API call: accountUpdateMulti")
    }

    override fun accountUpdateMultiEnd(reqId: Int) {
        println("Unimplemented API call: accountUpdateMultiEnd")
    }

    override fun securityDefinitionOptionalParameter(
        reqId: Int, exchange: String,
        underlyingConId: Int, tradingClass: String, multiplier: String,
        expirations: Set<String>, strikes: Set<Double>
    ) {
        println("Unimplemented API call: securityDefinitionOptionalParameter")
    }

    override fun securityDefinitionOptionalParameterEnd(reqId: Int) {
        println("Unimplemented API call: securityDefinitionOptionalParameterEnd")
    }

    override fun softDollarTiers(reqId: Int, tiers: Array<SoftDollarTier>) {
        println("Unimplemented API call: softDollarTiers")
    }

    override fun familyCodes(familyCodes: Array<FamilyCode>) {
        println("Unimplemented API call: familyCodes")
    }

    override fun symbolSamples(reqId: Int, contractDescriptions: Array<ContractDescription>) {
        println("Unimplemented API call: symbolSamples")
    }

    override fun mktDepthExchanges(depthMktDataDescriptions: Array<DepthMktDataDescription>) {
        println("Unimplemented API call: mktDepthExchanges")
    }

    override fun tickNews(
        tickerId: Int,
        timeStamp: Long,
        providerCode: String,
        articleId: String,
        headline: String,
        extraData: String
    ) {
        println("Unimplemented API call: tickNews")
    }

    override fun smartComponents(reqId: Int, theMap: Map<Int, Map.Entry<String, Char>>) {
        println("Unimplemented API call: smartComponents")
    }

    override fun tickReqParams(tickerId: Int, minTick: Double, bboExchange: String, snapshotPermissions: Int) {
        println("Unimplemented API call: tickReqParams")
    }

    override fun newsProviders(newsProviders: Array<NewsProvider>) {
        println("Unimplemented API call: newsProviders")
    }

    override fun newsArticle(requestId: Int, articleType: Int, articleText: String) {
        println("Unimplemented API call: newsArticle")
    }

    override fun historicalNews(
        requestId: Int,
        time: String,
        providerCode: String,
        articleId: String,
        headline: String
    ) {
        println("Unimplemented API call: historicalNews")
    }

    override fun historicalNewsEnd(requestId: Int, hasMore: Boolean) {
        println("Unimplemented API call: historicalNewsEnd")
    }

    override fun headTimestamp(reqId: Int, headTimestamp: String) {
        println("Unimplemented API call: headTimestamp")
    }

    override fun histogramData(reqId: Int, items: List<HistogramEntry>) {
        println("Unimplemented API call: histogramData")
    }

    override fun historicalDataUpdate(reqId: Int, bar: Bar) {
        println("Unimplemented API call: historicalDataUpdate")
    }

    override fun rerouteMktDataReq(reqId: Int, conId: Int, exchange: String) {
        println("Unimplemented API call: rerouteMktDataReq")
    }

    override fun rerouteMktDepthReq(reqId: Int, conId: Int, exchange: String) {
        println("Unimplemented API call: rerouteMktDepthReq")
    }

    override fun marketRule(marketRuleId: Int, priceIncrements: Array<PriceIncrement>) {
        println("Unimplemented API call: marketRule")
    }

    override fun pnl(reqId: Int, dailyPnL: Double, unrealizedPnL: Double, realizedPnL: Double) {
        println("Unimplemented API call: pnl")
    }

    override fun pnlSingle(
        reqId: Int,
        p1: Decimal,
        dailyPnL: Double,
        unrealizedPnL: Double,
        realizedPnL: Double,
        value: Double
    ) {
        println("Unimplemented API call: pnlSingle")
    }

    override fun historicalTicks(reqId: Int, ticks: List<HistoricalTick>, done: Boolean) {
        println("Unimplemented API call: historicalTicks")
    }

    override fun historicalTicksBidAsk(reqId: Int, ticks: List<HistoricalTickBidAsk>, done: Boolean) {
        println("Unimplemented API call: historicalTicksBidAsk")
    }

    override fun historicalTicksLast(reqId: Int, ticks: List<HistoricalTickLast>, done: Boolean) {
        println("Unimplemented API call: historicalTicksLast")
    }

    override fun tickByTickAllLast(
        reqId: Int,
        tickType: Int,
        time: Long,
        price: Double,
        p4: Decimal,
        tickAttribLast: TickAttribLast,
        exchange: String,
        specialConditions: String
    ) {
        println("Unimplemented API call: tickByTickAllLast")
    }

    override fun tickByTickBidAsk(
        reqId: Int,
        time: Long,
        bidPrice: Double,
        askPrice: Double,
        p4: Decimal,
        p5: Decimal,
        tickAttribBidAsk: TickAttribBidAsk
    ) {
        println("Unimplemented API call: tickByTickBidAsk")
    }

    override fun tickByTickMidPoint(reqId: Int, time: Long, midPoint: Double) {
        println("Unimplemented API call: tickByTickMidPoint")
    }

    override fun orderBound(orderId: Long, apiClientId: Int, apiOrderId: Int) {
        println("Unimplemented API call: orderBound")
    }

    override fun completedOrder(contract: Contract, order: Order, orderState: OrderState) {
        println("Unimplemented API call: completedOrder")
    }

    override fun completedOrdersEnd() {
        println("Unimplemented API call: completedOrdersEnd")
    }

    override fun replaceFAEnd(reqId: Int, text: String) {
        println("Unimplemented API call: replaceFAEnd")
    }

    override fun wshMetaData(p0: Int, p1: String?) {
        println("Unimplemented API call: wshMetaData")
    }

    override fun wshEventData(p0: Int, p1: String?) {
        println("Unimplemented API call: wshEventData")
    }
}