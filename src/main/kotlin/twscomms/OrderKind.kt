package twscomms

import com.ib.client.Decimal
import com.ib.client.Order
import com.ib.client.TagValue

/**
 * Base order type. Order kinds are composed using additional interfaces.
 */
interface OrderKind {
    val action: String

    /**
     * Creates a market order.
     *
     * @param account The account to place the order on.
     * @param quantity The number of shares to buy.
     * @return The order.
     */
    fun createOrder(account: Account, quantity: Long): Order {
        val order = Order()
        order.account(account.accountId)
        order.action(action)
        order.orderType("MKT")
        order.totalQuantity(Decimal.get(quantity))
        return order
    }
}

interface Buy : OrderKind {
    override val action: String
        get() = "BUY"
}

interface Sell : OrderKind {
    override val action: String
        get() = "SELL"
}

interface AdaptiveAlgo : OrderKind {
    val priority: String

    override fun createOrder(account: Account, quantity: Long): Order {
        val order = super.createOrder(account, quantity)
        order.algoStrategy("Adaptive")
        order.algoParams(mutableListOf(TagValue("adaptivePriority", priority)))
        return order
    }
}

interface AdaptivePatient : AdaptiveAlgo {
    override val priority: String
        get() = "Patient"
}

interface AdaptiveNormal : AdaptiveAlgo {
    override val priority: String
        get() = "Normal"
}

interface AdaptiveUrgent : AdaptiveAlgo {
    override val priority: String
        get() = "Urgent"
}

object UrgentBuyOrder : Buy, AdaptiveUrgent
object NormalBuyOrder : Buy, AdaptiveNormal
object PatientBuyOrder : Buy, AdaptivePatient
object UrgentSellOrder : Sell, AdaptiveUrgent
object NormalSellOrder : Sell, AdaptiveNormal
object PatientSellOrder : Sell, AdaptivePatient

object MarketBuyOrder : Buy
object MarketSellOrder: Sell