/**
 * Represents a portfolio of [StockTicker] with weights.
 */
class Portfolio private constructor (private val pairs: Map<StockTicker, Double>) {
    init {
        val x = pairs.values.fold(0.0) {l, r -> l + r}
        if(x != 1.0) throw Exception("Portfolio values didn't add up to 1")
    }

    /**
     * The [StockTicker] and associated weights of positions in the [Portfolio].
     * The weights must sum to 1.
     *
     * @param pairs The [StockTicker] and associated weights.
     */
    constructor(vararg pairs: Pair<StockTicker, Double>) : this(pairs.toMap())

    /**
     * Gets the weight of a [StockTicker] in the [Portfolio].
     *
     * @param ticker The [StockTicker] whose weight is to be looked up.
     * @returns The weight associated with [ticker] or 0.0 if [ticker] is not a position in the [Portfolio].
     */
    fun getWeight(ticker: StockTicker): Double = pairs[ticker] ?: 0.0

    /**
     * The list of positions in this [Portfolio].
     */
    val tickers: List<StockTicker>
        get() = pairs.keys.toList()
}