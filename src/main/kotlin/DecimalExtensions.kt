import com.ib.client.Decimal

operator fun Decimal.plus(lhs: Decimal) = this.add(lhs)
operator fun Decimal.minus(lhs: Decimal) = this.add(-lhs)
operator fun Decimal.times(lhs: Decimal) = this.multiply(lhs)
operator fun Decimal.div(lhs: Decimal) = this.divide(lhs)
operator fun Decimal.unaryMinus() = this.negate()