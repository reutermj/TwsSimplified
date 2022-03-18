package twscomms

class AccountSummaryTag internal constructor (private val tagString: String) {
    internal companion object {
        private val lookup = mapOf(
            "AccountType".lowercase() to AccountType,
            "NetLiquidation".lowercase() to NetLiquidation,
            "TotalCashValue".lowercase() to TotalCashValue,
            "SettledCash".lowercase() to SettledCash,
            "AccruedCash".lowercase() to AccruedCash,
            "BuyingPower".lowercase() to BuyingPower,
            "EquityWithLoanValue".lowercase() to EquityWithLoanValue,
            "PreviousEquityWithLoanValue".lowercase() to PreviousEquityWithLoanValue,
            "GrossPositionValue".lowercase() to GrossPositionValue,
            "RegTEquity".lowercase() to RegTEquity,
            "RegTMargin".lowercase() to RegTMargin,
            "SMA".lowercase() to SMA,
            "InitMarginReq".lowercase() to InitMarginReq,
            "MaintMarginReq".lowercase() to MaintMarginReq,
            "AvailableFunds".lowercase() to AvailableFunds,
            "ExcessLiquidity".lowercase() to ExcessLiquidity,
            "Cushion".lowercase() to Cushion,
            "FullInitMarginReq".lowercase() to FullInitMarginReq,
            "FullMaintMarginReq".lowercase() to FullMaintMarginReq,
            "FullAvailableFunds".lowercase() to FullAvailableFunds,
            "FullExcessLiquidity".lowercase() to FullExcessLiquidity,
            "LookAheadInitMarginReq".lowercase() to LookAheadInitMarginReq,
            "LookAheadMaintMarginReq".lowercase() to LookAheadMaintMarginReq,
            "LookAheadAvailableFunds".lowercase() to LookAheadAvailableFunds,
            "LookAheadExcessLiquidity".lowercase() to LookAheadExcessLiquidity,
            "HighestSeverity".lowercase() to HighestSeverity,
            "DayTradesRemaining".lowercase() to DayTradesRemaining,
        )

        fun getTag(tagString: String) = lookup[tagString.lowercase()]

        fun getAllTags() = lookup.values.fold("") { acc, tag -> "$acc,$tag" }.substring(1)
    }

    override fun toString() = tagString
}

val AccountType = AccountSummaryTag("AccountType")
val NetLiquidation = AccountSummaryTag("NetLiquidation")
val TotalCashValue = AccountSummaryTag("TotalCashValue")
val SettledCash = AccountSummaryTag("SettledCash")
val AccruedCash = AccountSummaryTag("AccruedCash")
val BuyingPower = AccountSummaryTag("BuyingPower")
val EquityWithLoanValue = AccountSummaryTag("EquityWithLoanValue")
val PreviousEquityWithLoanValue = AccountSummaryTag("PreviousEquityWithLoanValue")
val GrossPositionValue = AccountSummaryTag("GrossPositionValue")
val RegTEquity = AccountSummaryTag("RegTEquity")
val RegTMargin = AccountSummaryTag("RegTMargin")
val SMA = AccountSummaryTag("SMA")
val InitMarginReq = AccountSummaryTag("InitMarginReq")
val MaintMarginReq = AccountSummaryTag("MaintMarginReq")
val AvailableFunds = AccountSummaryTag("AvailableFunds")
val ExcessLiquidity = AccountSummaryTag("ExcessLiquidity")
val Cushion = AccountSummaryTag("Cushion")
val FullInitMarginReq = AccountSummaryTag("FullInitMarginReq")
val FullMaintMarginReq = AccountSummaryTag("FullMaintMarginReq")
val FullAvailableFunds = AccountSummaryTag("FullAvailableFunds")
val FullExcessLiquidity = AccountSummaryTag("FullExcessLiquidity")
val LookAheadInitMarginReq = AccountSummaryTag("LookAheadInitMarginReq")
val LookAheadMaintMarginReq = AccountSummaryTag("LookAheadMaintMarginReq")
val LookAheadAvailableFunds = AccountSummaryTag("LookAheadAvailableFunds")
val LookAheadExcessLiquidity = AccountSummaryTag("LookAheadExcessLiquidity")
val HighestSeverity = AccountSummaryTag("HighestSeverity")
val DayTradesRemaining = AccountSummaryTag("DayTradesRemaining")
