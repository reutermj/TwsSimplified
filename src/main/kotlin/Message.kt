sealed class Message

/**
 * Contains account information related to a requested [AccountSummaryTag].
 *
 * @param tag The account's attribute being recieved.
 * @param account The account associated with this message.
 * @param value The account's attribute's value.
 */
data class AccountSummaryMessage(val tag: AccountSummaryTag, val account: Account, val value: Double) : Message()

/**
 * Contains position size information.
 *
 * @param account The account associated with this message.
 * @param ticker The associated ticker.
 * @param quantity The account's attribute's value.
 */
data class StockQuantityMessage(val account: Account, val ticker: StockTicker, val quantity: Double) : Message()

/**
 * Contains last price information for a ticker.
 *
 * @param ticker The associated ticker.
 * @param price The last price of the ticker.
 */
data class StockPriceMessage(val ticker: StockTicker, val price: Double) : Message()

/**
 * Contains open price information for a ticker.
 *
 * @param ticker The associated ticker.
 * @param price The open price of the ticker.
 */
data class StockOpenMessage(val ticker: StockTicker, val price: Double) : Message()

/**
 * Identifies that an order has been filled.
 *
 * @param orderId The order that has been filled.
 */
data class OrderFilledMessage(val orderId: Int) : Message()

/**
 * Contains information related to an error reported by TWS.
 *
 * @param code The error code.
 * @param message The error message.
 */
data class ErrorMessage(val code: Int, val message: String) : Message()