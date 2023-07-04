package com.me.app.messagecenter.util

import java.math.BigDecimal
import java.math.RoundingMode

fun div(
    divisor: BigDecimal,
    dividend: Number,
    scale: Int = 3,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
): BigDecimal = div(divisor, BigDecimal(dividend.toString()), scale, roundingMode)

fun div(
    divisor: BigDecimal,
    dividend: BigDecimal,
    scale: Int = 3,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
): BigDecimal {
    if (BigDecimal.ZERO == dividend) {
        return if (BigDecimal.ZERO == divisor) divisor else BigDecimal.valueOf(Double.NaN)
    }
    return divisor.divide(dividend, scale, roundingMode)
}