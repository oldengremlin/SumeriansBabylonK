package ua.org.olden.sumeriansbabylon

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

class Base60 private constructor(
    private val numerator: BigInteger,
    private val denominator: BigInteger
) : Number(), Comparable<Base60> {

    companion object {
        private val SIXTY = BigInteger.valueOf(60)
        private val MC = MathContext(50, RoundingMode.HALF_UP)

        // --- Клинопис ---
        // Нуль: 𒑱 (U+12471) — вертикальне двокрапля, пізньовавилонський знак-розділювач
        // Роздільник цілої та дробової частин: 𒑲 (U+12472) — діагональне двокрапля
        private val CUNEIFORM_ZERO = Character.toString(0x12471)
        val CUNEIFORM_FRAC = Character.toString(0x12472)
        val CUNEIFORM_DIGITS: Array<String> = buildCuneiformDigits()

        private fun buildCuneiformDigits(): Array<String> {
            // Одиниці 1–9: готові злиті знаки (ASH = вертикальний клин)
            //   1 → 𒁹 U+12079,  2–9 → U+12400–U+12407
            val ones = arrayOf(
                "",
                Character.toString(0x12079), // 𒁹  1
                Character.toString(0x12400), // 𒐀  2
                Character.toString(0x12401), // 𒐁  3
                Character.toString(0x12402), // 𒐂  4
                Character.toString(0x12403), // 𒐃  5
                Character.toString(0x12404), // 𒐄  6
                Character.toString(0x12405), // 𒐅  7
                Character.toString(0x12406), // 𒐆  8
                Character.toString(0x12407), // 𒐇  9
            )
            // Десятки: повторення знаку Winkelhaken 𒌋 (U+1230B)
            //   10→𒌋  20→𒌋𒌋  30→𒌋𒌋𒌋  40→𒌋𒌋𒌋𒌋  50→𒌋𒌋𒌋𒌋𒌋
            val ten = Character.toString(0x1230B)
            val tens = arrayOf("", ten, ten + ten, ten + ten + ten, ten + ten + ten + ten, ten + ten + ten + ten + ten)

            return Array(60) { i ->
                if (i == 0) CUNEIFORM_ZERO else tens[i / 10] + ones[i % 10]
            }
        }

        // Нормалізація: GCD, знак в num, den > 0
        private fun create(num: BigInteger, den: BigInteger): Base60 {
            if (den.signum() == 0) throw ArithmeticException("Denominator cannot be zero")
            val gcd = num.gcd(den).abs()
            val n = num.divide(gcd).multiply(BigInteger.valueOf(den.signum().toLong()))
            val d = den.abs().divide(gcd)
            return Base60(n, d)
        }

        fun fromInteger(value: BigInteger) = Base60(value, BigInteger.ONE)
        fun fromInt(value: Int) = Base60(BigInteger.valueOf(value.toLong()), BigInteger.ONE)
        fun fromLong(value: Long) = Base60(BigInteger.valueOf(value), BigInteger.ONE)

        fun fromDecimal(value: BigDecimal): Base60 {
            val num = value.unscaledValue()
            val scale = value.scale()
            return if (scale >= 0) {
                create(num, BigInteger.TEN.pow(scale))
            } else {
                create(num.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE)
            }
        }

        fun fromFraction(num: BigInteger, den: BigInteger) = create(num, den)
        fun fromFraction(num: Int, den: Int) =
            create(BigInteger.valueOf(num.toLong()), BigInteger.valueOf(den.toLong()))

        fun fromFraction(num: Long, den: Long) =
            create(BigInteger.valueOf(num), BigInteger.valueOf(den))

        // --- Парсер типу 2:46:58.30:15 ---
        fun parse(input: String): Base60 {
            var s = input
            val negative = s.startsWith("-")
            if (negative) s = s.substring(1)

            val parts = s.split(".")
            val integerNum = parseIntegerPart(parts[0])
            var fracNum = BigInteger.ZERO
            var fracDen = BigInteger.ONE

            if (parts.size > 1) {
                val fracDigits = parts[1].split(":")
                fracDen = SIXTY.pow(fracDigits.size)
                for (i in fracDigits.indices) {
                    val digit = fracDigits[i].replace(Regex("[^\\d]+"), "").toInt()
                    require(digit in 0..59) { "Digit must be 0-59" }
                    val power = SIXTY.pow(fracDigits.size - 1 - i)
                    fracNum = fracNum.add(BigInteger.valueOf(digit.toLong()).multiply(power))
                }
            }

            // Збираємо в один дріб: (integerNum * fracDen + fracNum) / fracDen
            var totalNum = integerNum.multiply(fracDen).add(fracNum)
            if (negative) totalNum = totalNum.negate()
            return create(totalNum, fracDen)
        }

        private fun parseIntegerPart(part: String): BigInteger {
            if (part.isEmpty()) return BigInteger.ZERO
            var result = BigInteger.ZERO
            for (d in part.split(":")) {
                val digit = d.toInt()
                require(digit in 0..59) { "Digit must be 0-59" }
                result = result.multiply(SIXTY).add(BigInteger.valueOf(digit.toLong()))
            }
            return result
        }
    }

    // --- Конвертація в base-60 список розрядів ---
    private fun toBase60IntegerDigits(): List<Int> {
        var absIntPart = numerator.abs().divide(denominator)
        if (absIntPart == BigInteger.ZERO) return listOf(0)
        val digits = mutableListOf<Int>()
        while (absIntPart > BigInteger.ZERO) {
            val divRem = absIntPart.divideAndRemainder(SIXTY)
            digits.add(divRem[1].toInt())
            absIntPart = divRem[0]
        }
        return digits.reversed()
    }

    private fun toBase60FractionDigits(precision: Int): List<Int> {
        var absRemainder = numerator.abs().remainder(denominator)
        if (absRemainder == BigInteger.ZERO) return emptyList()
        val digits = mutableListOf<Int>()
        repeat(precision) {
            absRemainder = absRemainder.multiply(SIXTY)
            val digit = absRemainder.divide(denominator)
            digits.add(digit.toInt())
            absRemainder = absRemainder.remainder(denominator)
            if (absRemainder == BigInteger.ZERO) return digits
        }
        return digits
    }

    // --- Форматування ---
    fun toString(precision: Int): String {
        val intDigits = toBase60IntegerDigits()
        var fracDigits = toBase60FractionDigits(precision)

        // обрізання нулів у кінці
        var lastNonZero = fracDigits.size - 1
        while (lastNonZero >= 0 && fracDigits[lastNonZero] == 0) lastNonZero--
        fracDigits = if (lastNonZero >= 0) fracDigits.subList(0, lastNonZero + 1) else emptyList()

        val intPart = intDigits.joinToString(":")
        val result = if (fracDigits.isEmpty()) intPart else "$intPart.${fracDigits.joinToString(":")}"
        return if (numerator.signum() < 0) "-$result" else result
    }

    fun toBase60WithPeriod(): String {
        val negative = numerator.signum() < 0
        val absNum = numerator.abs()
        val intPart = absNum.divide(denominator)
        var remainder = absNum.remainder(denominator)

        val sb = StringBuilder()
        sb.append(toBase60IntegerDigitsFor(intPart).joinToString(":"))

        if (remainder == BigInteger.ZERO) {
            return if (negative) "-$sb" else sb.toString()
        }

        sb.append(".")

        val seen = mutableMapOf<BigInteger, Int>()
        val digits = mutableListOf<Int>()
        var cycleStart: Int? = null
        var current = remainder

        while (current != BigInteger.ZERO) {
            if (current in seen) {
                cycleStart = seen[current]
                break
            }
            seen[current] = digits.size
            current = current.multiply(SIXTY)
            val digit = current.divide(denominator)
            digits.add(digit.toInt())
            current = current.remainder(denominator)
        }

        for (i in digits.indices) {
            if (cycleStart != null && i == cycleStart) sb.append("(")
            sb.append(digits[i])
            if (i < digits.size - 1) sb.append(":")
        }
        if (cycleStart != null) sb.append(")")

        return if (negative) "-$sb" else sb.toString()
    }

    private fun toBase60IntegerDigitsFor(n: BigInteger): List<Int> {
        if (n == BigInteger.ZERO) return listOf(0)
        var num = n
        val digits = mutableListOf<Int>()
        while (num > BigInteger.ZERO) {
            val divRem = num.divideAndRemainder(SIXTY)
            digits.add(divRem[1].toInt())
            num = divRem[0]
        }
        return digits.reversed()
    }

    override fun toString() = toString(10)

    // Виводить число шумерсько-вавилонським клинописом.
    // Розряди розділені пробілом; 𒑲 відокремлює цілу частину від дробової.
    // Нуль у будь-якій позиції: 𒑱
    // Від'ємні числа позначаються знаком «-» (клинопис знака мінус не мав).
    fun toSumerianString(): String {
        val intDigits = toBase60IntegerDigits()
        var fracDigits = toBase60FractionDigits(10)

        var lastNonZero = fracDigits.size - 1
        while (lastNonZero >= 0 && fracDigits[lastNonZero] == 0) lastNonZero--
        fracDigits = if (lastNonZero >= 0) fracDigits.subList(0, lastNonZero + 1) else emptyList()

        val intPart = intDigits.joinToString(" ") { CUNEIFORM_DIGITS[it] }
        val result = if (fracDigits.isEmpty()) intPart
        else intPart + CUNEIFORM_FRAC + fracDigits.joinToString(" ") { CUNEIFORM_DIGITS[it] }
        return if (numerator.signum() < 0) "-$result" else result
    }

    // --- Доступ до десяткового значення ---
    fun toDecimal(): BigDecimal = BigDecimal(numerator).divide(BigDecimal(denominator), MC)
    fun toInteger(): BigInteger = numerator.divide(denominator)

    // --- Арифметика ---
    fun add(other: Base60) = create(
        numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
        denominator.multiply(other.denominator)
    )

    fun subtract(other: Base60) = create(
        numerator.multiply(other.denominator).subtract(other.numerator.multiply(denominator)),
        denominator.multiply(other.denominator)
    )

    fun multiply(other: Base60) = create(
        numerator.multiply(other.numerator),
        denominator.multiply(other.denominator)
    )

    fun divide(other: Base60): Base60 {
        if (other.numerator.signum() == 0) throw ArithmeticException("Division by zero")
        return create(numerator.multiply(other.denominator), denominator.multiply(other.numerator))
    }

    // --- Знак ---
    fun negate() = Base60(numerator.negate(), denominator)
    fun abs() = Base60(numerator.abs(), denominator)
    fun signum() = numerator.signum()

    // --- Степінь ---
    fun pow(n: Int): Base60 {
        if (n == 0) return fromInt(1)
        if (n > 0) return Base60(numerator.pow(n), denominator.pow(n))
        if (numerator.signum() == 0) throw ArithmeticException("Zero cannot be raised to a negative power")
        val absN = -n
        return Base60(denominator.pow(absN), numerator.pow(absN))
    }

    fun pow(exp: Base60): Base60 {
        if (exp.denominator == BigInteger.ONE && exp.numerator.abs().bitLength() <= 31) {
            return pow(exp.numerator.toInt())
        }
        val base = toDecimal().toDouble()
        val e = exp.toDecimal().toDouble()
        return fromDecimal(BigDecimal.valueOf(Math.pow(base, e)))
    }

    // --- Остача (floor mod: результат має знак дільника) ---
    fun mod(other: Base60): Base60 {
        if (other.numerator.signum() == 0) throw ArithmeticException("Modulo by zero")
        val p = numerator.multiply(other.denominator)
        val q = denominator.multiply(other.numerator)
        val divRem = p.divideAndRemainder(q)
        // BigInteger.divide усікає до нуля; коригуємо до floor для від'ємних
        val floor = if (divRem[1].signum() != 0 && (p.signum() < 0) != (q.signum() < 0))
            divRem[0].subtract(BigInteger.ONE) else divRem[0]
        return subtract(other.multiply(fromInteger(floor)))
    }

    // --- Квадратний корінь ---
    fun sqrt(): Base60 {
        if (numerator.signum() < 0) throw ArithmeticException("Square root of negative number")
        return fromDecimal(toDecimal().sqrt(MC))
    }

    // Вавілонський (шумерський) метод Герона: x_{n+1} = (x_n + S/x_n) / 2
    fun sqrtSumerians(): Base60 {
        if (numerator.signum() < 0) throw ArithmeticException("Square root of negative number")
        if (numerator.signum() == 0) return fromInt(0)
        var x = fromDecimal(BigDecimal.valueOf(toDecimal().toDouble() / Math.random()))
        val two = fromInt(2)
        repeat(10) { x = x.add(this.divide(x)).divide(two) }
        return x
    }

    // --- kotlin.Number ---
    override fun toInt() = toInteger().toInt()
    override fun toLong() = toInteger().toLong()
    override fun toFloat() = toDecimal().toFloat()
    override fun toDouble() = toDecimal().toDouble()
    override fun toByte() = toInteger().toByte()
    override fun toShort() = toInteger().toShort()

    // --- Comparable ---
    override fun compareTo(other: Base60): Int =
        numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Base60) return false
        return compareTo(other) == 0
    }

    override fun hashCode() = java.util.Objects.hash(numerator, denominator)
}
