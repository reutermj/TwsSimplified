sealed class Message

class AccountSummaryMessage(val tag: AccountSummaryTag, val account: Account, val value: Double) : Message()
class StockQuantityMessage(val account: Account, val ticker: StockTicker, val quantity: Double) : Message()
class StockPriceMessage(val ticker: StockTicker, val price: Double) : Message()
class StockOpenMessage(val ticker: StockTicker, val price: Double) : Message()
class OrderFilledMessage(val orderId: Int) : Message()
class ErrorMessage(val code: Int, val message: String) : Message()