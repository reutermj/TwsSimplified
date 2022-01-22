import com.ib.client.Contract
import java.util.*

class StockTicker private constructor(private val tickerString: String, private val primaryExchange: String) {
    companion object {
        private val lookup = mutableMapOf<String, StockTicker>()

        fun register(tickerString: String, primaryExchange: String): StockTicker {
            val ticker = StockTicker(tickerString, primaryExchange)
            lookup[tickerString.lowercase()] = ticker
            return ticker
        }

        operator fun get(tickerString: String) = lookup[tickerString.lowercase()]
    }

    fun createContract(): Contract {
        val contract = Contract()
        contract.symbol(tickerString)
        contract.secType("STK")
        contract.currency("USD")
        contract.exchange("SMART")
        contract.primaryExch(primaryExchange)
        return contract
    }

    override fun toString() = tickerString
}

val AVUS = StockTicker.register("AVUS", "ARCA")
val AVLV = StockTicker.register("AVLV", "ARCA")
val AVUV = StockTicker.register("AVUV", "ARCA")
val AVDE = StockTicker.register("AVDE", "ARCA")
val AVIV = StockTicker.register("AVIV", "ARCA")
val AVDV = StockTicker.register("AVDV", "ARCA")
val AVEM = StockTicker.register("AVEM", "ARCA")
val AVES = StockTicker.register("AVES", "ARCA")
val AVIG = StockTicker.register("AVIG", "ARCA")
