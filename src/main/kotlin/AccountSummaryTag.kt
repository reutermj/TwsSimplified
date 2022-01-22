class AccountSummaryTag private constructor (private val tagString: String) {
    companion object {
        private val lookup = mutableMapOf<String, AccountSummaryTag>()

        fun register(tagString: String): AccountSummaryTag {
            val tag = AccountSummaryTag(tagString)
            lookup[tagString.toLowerCase()] = tag
            return tag
        }

        operator fun get(tagString: String) = lookup[tagString.toLowerCase()]
    }

    override fun toString() = tagString
}

val AccountType = AccountSummaryTag.register("AccountType")
val NetLiquidation = AccountSummaryTag.register("NetLiquidation")
val TotalCashValue = AccountSummaryTag.register("TotalCashValue")
val SettledCash = AccountSummaryTag.register("SettledCash")
val AccruedCash = AccountSummaryTag.register("AccruedCash")
val BuyingPower = AccountSummaryTag.register("BuyingPower")
val EquityWithLoanValue = AccountSummaryTag.register("EquityWithLoanValue")
val PreviousEquityWithLoanValue = AccountSummaryTag.register("PreviousEquityWithLoanValue")
val GrossPositionValue = AccountSummaryTag.register("GrossPositionValue")
val RegTEquity = AccountSummaryTag.register("RegTEquity")
val RegTMargin = AccountSummaryTag.register("RegTMargin")
val SMA = AccountSummaryTag.register("SMA")
val InitMarginReq = AccountSummaryTag.register("InitMarginReq")
val MaintMarginReq = AccountSummaryTag.register("MaintMarginReq")
val AvailableFunds = AccountSummaryTag.register("AvailableFunds")
val ExcessLiquidity = AccountSummaryTag.register("ExcessLiquidity")
val Cushion = AccountSummaryTag.register("Cushion")
val FullInitMarginReq = AccountSummaryTag.register("FullInitMarginReq")
val FullMaintMarginReq = AccountSummaryTag.register("FullMaintMarginReq")
val FullAvailableFunds = AccountSummaryTag.register("FullAvailableFunds")
val FullExcessLiquidity = AccountSummaryTag.register("FullExcessLiquidity")
val LookAheadInitMarginReq = AccountSummaryTag.register("LookAheadInitMarginReq")
val LookAheadMaintMarginReq = AccountSummaryTag.register("LookAheadMaintMarginReq")
val LookAheadAvailableFunds = AccountSummaryTag.register("LookAheadAvailableFunds")
val LookAheadExcessLiquidity = AccountSummaryTag.register("LookAheadExcessLiquidity")
val HighestSeverity = AccountSummaryTag.register("HighestSeverity")
val DayTradesRemaining = AccountSummaryTag.register("DayTradesRemaining")
val Leverage = AccountSummaryTag.register("Leverage")