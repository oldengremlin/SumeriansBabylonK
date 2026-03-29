package ua.org.olden.sumeriansbabylon

import java.math.BigInteger

/**
 * Демонстрація можливостей бібліотеки Base60.
 */
fun main() {
    demoBasic()
    println()
    demoSignNegateAbs()
    println()
    demoPow()
    println()
    demoMod()
    println()
    demoSqrt()
    println()
    demoNumber()
}

// --- Базові операції ---
fun demoBasic() {
    println("=== Базові операції ===")
    val a = Base60.parse("2:46:58.30:15")
    println("  parse(\"2:46:58.30:15\") → $a → ${a.toDecimal()}")
    println("  куніформ: ${a.toSumerianString()}")

    val b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(7))
    println("  1/7 = ${b.toBase60WithPeriod()} → ${b.toDecimal()}")

    val c = Base60.parse("1:30")
    val d = Base60.parse("2:15")
    println("  $c + $d = ${c.add(d)}")
    println("  $c × $d = ${c.multiply(d)}")
    println("  $c ÷ $d = ${c.divide(d)}")
}

// --- negate / abs / signum ---
fun demoSignNegateAbs() {
    println("=== negate / abs / signum ===")
    val x = Base60.parse("3:45.30")
    println("  x        = $x")
    println("  negate() = ${x.negate()}")
    println("  abs(-x)  = ${x.negate().abs()}")
    println("  signum() = ${x.signum()}  (знак: -1/0/+1)")
    println("  signum(-x)= ${x.negate().signum()}")
    println("  куніформ -x: ${x.negate().toSumerianString()}")
}

// --- pow ---
fun demoPow() {
    println("=== pow ===")
    val two = Base60.fromInt(2)
    println("  2^10       = ${two.pow(10)}")
    println("  2^-1       = ${two.pow(-1)}  (1/2)")
    println("  (1:30)^2   = ${Base60.parse("1:30").pow(2)}")

    // pow(Base60) — цілий показник
    val three = Base60.fromInt(3)
    println("  3^Base60(3) = ${three.pow(Base60.fromInt(3))}")

    // pow(Base60) — дробовий показник (4^(1/2) = 2)
    val four = Base60.fromInt(4)
    val half = Base60.fromFraction(1, 2)
    println("  4^(1/2)    = ${four.pow(half)}  (через double)")

    // куніформ 60^2
    val sixty = Base60.fromInt(60)
    println("  60^2       = ${sixty.pow(2)} → ${sixty.pow(2).toSumerianString()}")
}

// --- mod ---
fun demoMod() {
    println("=== mod (floor mod) ===")
    val a = Base60.fromInt(7)
    val b = Base60.fromInt(3)
    println("   7 mod  3 = ${a.mod(b)}")
    println("  -7 mod  3 = ${a.negate().mod(b)}")
    println("   7 mod -3 = ${a.mod(b.negate())}")
    println("  -7 mod -3 = ${a.negate().mod(b.negate())}")

    // кутовий приклад — переведення секунд у хвилини:секунди
    val seconds = Base60.fromInt(3723)
    val sixty = Base60.fromInt(60)
    val minutes = Base60.fromInteger(seconds.toInteger().divide(BigInteger.valueOf(60)))
    val secs = seconds.mod(sixty)
    println("  3723 сек = $minutes хв $secs с")
}

// --- sqrt / sqrtSumerians ---
fun demoSqrt() {
    println("=== sqrt / sqrtSumerians ===")

    // sqrt через BigDecimal (висока точність)
    val two = Base60.fromInt(2)
    val sqrtClassic = two.sqrt()
    println("  √2 (класичний):   ${sqrtClassic.toString(8)}")
    println("  √2 куніформ:      ${sqrtClassic.toSumerianString()}")

    // sqrtSumerians — метод Герона
    val sqrtBabylon = two.sqrtSumerians()
    println("  √2 (вавілонський): ${sqrtBabylon.toString(8)}")

    // різниця між двома методами
    val diff = sqrtClassic.subtract(sqrtBabylon).abs()
    println("  Різниця:          ${diff.toDecimal().toPlainString()}")

    // Перевірка: sqrt(60^2) = 60
    val sq = Base60.fromInt(3600)
    println("  √3600 = ${sq.sqrt()}  (= ${sq.sqrtSumerians()})")

    // Вавілонська задача: √2 на табличці YBC 7289
    // Вавілоняни записали 1;24,51,10 ≈ √2
    val ybc = Base60.parse("1.24:51:10")
    println("  YBC 7289 (1;24,51,10) = ${ybc.toDecimal()}")
    println("  Наш sqrt(2)           = ${sqrtClassic.toDecimal()}")
}

// --- kotlin.Number ---
fun demoNumber() {
    println("=== Base60 як kotlin.Number ===")
    val n: Number = Base60.fromFraction(7, 2)  // 3.5
    println(
        "  7/2 як Number: toInt=${n.toInt()}, toLong=${n.toLong()}, toFloat=${"%.2f".format(n.toFloat())}, toDouble=${
            "%.6f".format(
                n.toDouble()
            )
        }"
    )

    // Використання у Stream через toDouble
    val sum = listOf(
        Base60.fromInt(1), Base60.fromFraction(1, 2), Base60.fromFraction(1, 3)
    ).sumOf { it.toDouble() }
    println("  1 + 1/2 + 1/3 (через toDouble) ≈ ${"%.6f".format(sum)}")
    println(
        "  1 + 1/2 + 1/3 (точно)          = ${
            Base60.fromInt(1).add(Base60.fromFraction(1, 2)).add(Base60.fromFraction(1, 3))
        }"
    )
}
