class Portfolio private constructor (private val pairs: Map<StockTicker, Double>) {
    init {
        val x = pairs.values.fold(0.0) {l, r -> l + r}
        if(x != 1.0) throw Exception("Portfolio values didn't add up to 1")
    }

    constructor(vararg pairs: Pair<StockTicker, Double>) : this(pairs.toMap())

    fun getWeight(ticker: StockTicker): Double = pairs[ticker] ?: 0.0

    val tickers: List<StockTicker>
        get() = pairs.keys.toList()
}