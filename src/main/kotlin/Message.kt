sealed class Message

class GrossMessage(val account: Account, val value: Double) : Message()
class NetMessage(val account: Account, val value: Double) : Message()
class MaintMessage(val account: Account, val value: Double) : Message()
class CashMessage(val account: Account, val value: Double) : Message()

class StockQuantityMessage(val account: Account, val ticker: StockTicker, val quantity: Double) : Message()
class StockPriceMessage(val ticker: StockTicker, val price: Double) : Message()
class StockOpenMessage(val ticker: StockTicker, val price: Double) : Message()

class OrderFilledMessage(val orderId: Int) : Message()

class ErrorMessage(val code: Int, val message: String) : Message()