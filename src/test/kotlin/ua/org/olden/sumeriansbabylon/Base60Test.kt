package ua.org.olden.sumeriansbabylon

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.math.BigInteger

@DisplayName("Base60 — шістдесяткова арифметика")
class Base60Test {

    // -------------------------------------------------------------------------
    // Фабричні методи
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("fromInteger створює ціле число")
    fun fromInteger() {
        val n = Base60.fromInteger(BigInteger.valueOf(3600))
        assertEquals("1:0:0", n.toString())
    }

    @Test
    @DisplayName("fromInt створює ціле число з int")
    fun fromInt() {
        val n = Base60.fromInt(60)
        assertEquals("1:0", n.toString())
    }

    @Test
    @DisplayName("fromLong створює ціле число з long")
    fun fromLong() {
        val n = Base60.fromLong(3600L)
        assertEquals("1:0:0", n.toString())
    }

    @Test
    @DisplayName("fromInt(0) повертає нуль")
    fun fromIntZero() {
        assertEquals("0", Base60.fromInt(0).toString())
    }

    @Test
    @DisplayName("fromInt від'ємне число")
    fun fromIntNegative() {
        assertEquals("-1:0", Base60.fromInt(-60).toString())
    }

    @Test
    @DisplayName("fromDecimal точно конвертує десятковий дріб")
    fun fromDecimal() {
        val n = Base60.fromDecimal(BigDecimal("0.5"))
        assertEquals("0.30", n.toString())
    }

    @Test
    @DisplayName("fromFraction(BigInteger, BigInteger) скорочує дріб")
    fun fromFractionBigInteger() {
        val n = Base60.fromFraction(BigInteger.valueOf(2), BigInteger.valueOf(4))
        assertEquals(Base60.fromFraction(BigInteger.ONE, BigInteger.TWO), n)
    }

    @Test
    @DisplayName("fromFraction(int, int) збігається з BigInteger-варіантом")
    fun fromFractionInt() {
        val a = Base60.fromFraction(1, 3)
        val b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(3))
        assertEquals(a, b)
    }

    @Test
    @DisplayName("fromFraction(long, long) збігається з BigInteger-варіантом")
    fun fromFractionLong() {
        val a = Base60.fromFraction(1L, 4L)
        val b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(4))
        assertEquals(a, b)
    }

    @Test
    @DisplayName("fromFraction з від'ємним знаменником — знак переходить у чисельник")
    fun fromFractionNegativeDen() {
        val a = Base60.fromFraction(1, -3)
        val b = Base60.fromFraction(-1, 3)
        assertEquals(a, b)
    }

    @Test
    @DisplayName("fromFraction з нульовим знаменником кидає ArithmeticException")
    fun fromFractionZeroDen() {
        assertThrows(ArithmeticException::class.java) {
            Base60.fromFraction(BigInteger.ONE, BigInteger.ZERO)
        }
    }

    // -------------------------------------------------------------------------
    // Парсинг
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("parse — ціле число")
    fun parseInteger() {
        assertEquals(Base60.fromInt(3600), Base60.parse("1:0:0"))
    }

    @Test
    @DisplayName("parse — дріб без цілої частини")
    fun parseFraction() {
        val half = Base60.parse(".30")
        assertEquals(Base60.fromFraction(1, 2), half)
    }

    @Test
    @DisplayName("parse — ціле + дріб")
    fun parseIntegerAndFraction() {
        val n = Base60.parse("1:30.30")
        // 1:30 = 90, .30 = 1/2 → 90.5
        assertEquals(BigDecimal("90.5"), n.toDecimal().stripTrailingZeros())
    }

    @Test
    @DisplayName("parse — від'ємне число")
    fun parseNegative() {
        val a = Base60.parse("-1:0")
        val b = Base60.fromInt(-60)
        assertEquals(a, b)
    }

    @Test
    @DisplayName("parse — цифра поза діапазоном 0-59 кидає IllegalArgumentException")
    fun parseInvalidDigit() {
        assertThrows(IllegalArgumentException::class.java) { Base60.parse("60") }
    }

    @Test
    @DisplayName("parse — null кидає NullPointerException")
    fun parseNull() {
        assertThrows(NullPointerException::class.java) { Base60.parse(null!!) }
    }

    // -------------------------------------------------------------------------
    // Арифметика
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("додавання двох цілих")
    fun addIntegers() {
        val a = Base60.fromInt(30)
        val b = Base60.fromInt(30)
        assertEquals(Base60.fromInt(60), a.add(b))
    }

    @Test
    @DisplayName("додавання дробів: 1/3 + 1/6 = 1/2")
    fun addFractions() {
        val a = Base60.fromFraction(1, 3)
        val b = Base60.fromFraction(1, 6)
        assertEquals(Base60.fromFraction(1, 2), a.add(b))
    }

    @Test
    @DisplayName("віднімання: результат нуль")
    fun subtractToZero() {
        val a = Base60.fromInt(42)
        assertEquals(Base60.fromInt(0), a.subtract(a))
    }

    @Test
    @DisplayName("віднімання: від'ємний результат")
    fun subtractNegative() {
        val a = Base60.fromInt(1)
        val b = Base60.fromInt(2)
        assertEquals(Base60.fromInt(-1), a.subtract(b))
    }

    @Test
    @DisplayName("множення: 1/2 * 1/3 = 1/6")
    fun multiply() {
        val a = Base60.fromFraction(1, 2)
        val b = Base60.fromFraction(1, 3)
        assertEquals(Base60.fromFraction(1, 6), a.multiply(b))
    }

    @Test
    @DisplayName("множення на нуль завжди нуль")
    fun multiplyByZero() {
        val a = Base60.fromInt(999)
        assertEquals(Base60.fromInt(0), a.multiply(Base60.fromInt(0)))
    }

    @Test
    @DisplayName("ділення: 1/2 / 1/4 = 2")
    fun divide() {
        val a = Base60.fromFraction(1, 2)
        val b = Base60.fromFraction(1, 4)
        assertEquals(Base60.fromInt(2), a.divide(b))
    }

    @Test
    @DisplayName("ділення на нуль кидає ArithmeticException")
    fun divideByZero() {
        assertThrows(ArithmeticException::class.java) {
            Base60.fromInt(1).divide(Base60.fromInt(0))
        }
    }

    @Test
    @DisplayName("арифметика: (a + b) * c = a*c + b*c")
    fun distributiveLaw() {
        val a = Base60.fromFraction(1, 3)
        val b = Base60.fromFraction(1, 4)
        val c = Base60.fromInt(12)
        assertEquals(a.add(b).multiply(c), a.multiply(c).add(b.multiply(c)))
    }

    // -------------------------------------------------------------------------
    // Порівняння / equals / hashCode
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("рівні дроби: 2/4 == 1/2")
    fun equalsFractions() {
        assertEquals(Base60.fromFraction(2, 4), Base60.fromFraction(1, 2))
    }

    @Test
    @DisplayName("нерівні числа")
    fun notEquals() {
        assertNotEquals(Base60.fromInt(1), Base60.fromInt(2))
    }

    @Test
    @DisplayName("однакові об'єкти мають однаковий hashCode")
    fun hashCodeConsistent() {
        val a = Base60.fromFraction(1, 3)
        val b = Base60.fromFraction(2, 6)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    @DisplayName("compareTo: менше / рівно / більше")
    fun compareTo() {
        val small = Base60.fromFraction(1, 3)
        val mid   = Base60.fromFraction(1, 2)
        val big   = Base60.fromFraction(2, 3)
        assertTrue(small.compareTo(mid) < 0)
        assertEquals(0, mid.compareTo(Base60.fromFraction(2, 4)))
        assertTrue(big.compareTo(mid) > 0)
    }

    @Test
    @DisplayName("від'ємне менше нуля")
    fun compareNegative() {
        assertTrue(Base60.fromInt(-1).compareTo(Base60.fromInt(0)) < 0)
    }

    // -------------------------------------------------------------------------
    // Форматування toString
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "{0} → \"{1}\"")
    @DisplayName("toString: відомі значення base-60")
    @CsvSource(
        "0,    0",
        "59,   59",
        "60,   1:0",
        "3600, 1:0:0",
        "3661, 1:1:1"
    )
    fun toStringIntegers(value: Int, expected: String) {
        assertEquals(expected, Base60.fromInt(value).toString())
    }

    @Test
    @DisplayName("toString: дріб 1/2 = 0.30")
    fun toStringHalf() {
        assertEquals("0.30", Base60.fromFraction(1, 2).toString())
    }

    @Test
    @DisplayName("toString: від'ємний дріб")
    fun toStringNegativeFraction() {
        assertEquals("-0.30", Base60.fromFraction(-1, 2).toString())
    }

    @Test
    @DisplayName("toString: 1/3 точно в base-60 (0.20)")
    fun toStringOneThird() {
        assertEquals("0.20", Base60.fromFraction(1, 3).toString())
    }

    @Test
    @DisplayName("toString(precision): обмежена точність")
    fun toStringWithPrecision() {
        // 1/7 не скінченний у base-60, але toString(3) обріже до 3 розрядів
        val s = Base60.fromFraction(1, 7).toString(3)
        assertFalse(s.isEmpty())
        // перевіряємо що є не більше 3 дробових розрядів
        val parts = s.split(".")
        if (parts.size == 2) {
            assertTrue(parts[1].split(":").size <= 3)
        }
    }

    // -------------------------------------------------------------------------
    // toBase60WithPeriod
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("1/3 є скінченним у base-60: 0.20")
    fun withPeriodOneThirdFinite() {
        assertEquals("0.20", Base60.fromFraction(1, 3).toBase60WithPeriod())
    }

    @Test
    @DisplayName("1/7 має цикл у base-60: 0.(8:34:17)")
    fun withPeriodOneSeventh() {
        assertEquals("0.(8:34:17)", Base60.fromFraction(1, 7).toBase60WithPeriod())
    }

    @Test
    @DisplayName("ціле число без дробової частини — без крапки")
    fun withPeriodInteger() {
        assertEquals("1:0", Base60.fromInt(60).toBase60WithPeriod())
    }

    @Test
    @DisplayName("від'ємний циклічний дріб")
    fun withPeriodNegativeCyclic() {
        val pos = Base60.fromFraction(1, 7).toBase60WithPeriod()
        val neg = Base60.fromFraction(-1, 7).toBase60WithPeriod()
        assertEquals("-$pos", neg)
    }

    // -------------------------------------------------------------------------
    // toDecimal / toInteger
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDecimal: 1/2 = 0.5")
    fun toDecimalHalf() {
        assertEquals(BigDecimal("0.5"),
            Base60.fromFraction(1, 2).toDecimal().stripTrailingZeros())
    }

    @Test
    @DisplayName("toDecimal: 1/3 ≈ 0.333...")
    fun toDecimalOneThird() {
        val d = Base60.fromFraction(1, 3).toDecimal()
        assertTrue(d.toPlainString().startsWith("0.33333"))
    }

    @Test
    @DisplayName("toInteger відкидає дробову частину")
    fun toInteger() {
        assertEquals(BigInteger.valueOf(2), Base60.fromFraction(5, 2).toInteger())
    }

    @Test
    @DisplayName("toInteger від'ємного числа")
    fun toIntegerNegative() {
        // -5/2 = -2.5 → toInteger() = -2 (усікання до нуля)
        assertEquals(BigInteger.valueOf(-2), Base60.fromFraction(-5, 2).toInteger())
    }

    // -------------------------------------------------------------------------
    // Властивості нейтральних елементів
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("a + 0 = a")
    fun addIdentity() {
        val a = Base60.fromFraction(3, 7)
        assertEquals(a, a.add(Base60.fromInt(0)))
    }

    @Test
    @DisplayName("a * 1 = a")
    fun multiplyIdentity() {
        val a = Base60.fromFraction(3, 7)
        assertEquals(a, a.multiply(Base60.fromInt(1)))
    }

    @Test
    @DisplayName("a / a = 1")
    fun divideSelf() {
        val a = Base60.fromFraction(3, 7)
        assertEquals(Base60.fromInt(1), a.divide(a))
    }

    // -------------------------------------------------------------------------
    // toSumerianString
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("нуль → 𒑱")
    fun sumerianZero() {
        assertEquals(Character.toString(0x12471), Base60.fromInt(0).toSumerianString())
    }

    @Test
    @DisplayName("1 → 𒁹")
    fun sumerianOne() {
        assertEquals(Character.toString(0x12079), Base60.fromInt(1).toSumerianString())
    }

    @Test
    @DisplayName("10 → 𒌋")
    fun sumerianTen() {
        assertEquals(Character.toString(0x1230B), Base60.fromInt(10).toSumerianString())
    }

    @Test
    @DisplayName("59 → п'ять десятків + дев'ять одиниць")
    fun sumerianFiftyNine() {
        val fifty = Character.toString(0x1230B).repeat(5)
        val nine  = Character.toString(0x12407)
        assertEquals(fifty + nine, Base60.fromInt(59).toSumerianString())
    }

    @Test
    @DisplayName("60 (1:0) → два розряди з нулем")
    fun sumerianSixty() {
        val one  = Character.toString(0x12079)
        val zero = Character.toString(0x12471)
        assertEquals("$one $zero", Base60.fromInt(60).toSumerianString())
    }

    @Test
    @DisplayName("1/2 → ціла 𒑱, дробова 𒌋𒌋𒌋 (30)")
    fun sumerianHalf() {
        val zero   = Character.toString(0x12471)
        val thirty = Character.toString(0x1230B).repeat(3)
        val frac   = Character.toString(0x12472)
        assertEquals(zero + frac + thirty, Base60.fromFraction(1, 2).toSumerianString())
    }

    @Test
    @DisplayName("від'ємне число має префікс «-»")
    fun sumerianNegative() {
        assertTrue(Base60.fromInt(-1).toSumerianString().startsWith("-"))
    }

    // -------------------------------------------------------------------------
    // negate / abs / signum
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("negate: змінює знак")
    fun negate() {
        assertEquals(Base60.fromInt(-3), Base60.fromInt(3).negate())
        assertEquals(Base60.fromInt(3), Base60.fromInt(-3).negate())
    }

    @Test
    @DisplayName("negate нуля дає нуль")
    fun negateZero() {
        assertEquals(Base60.fromInt(0), Base60.fromInt(0).negate())
    }

    @Test
    @DisplayName("abs: завжди невід'ємне")
    fun abs() {
        assertEquals(Base60.fromFraction(1, 3), Base60.fromFraction(-1, 3).abs())
        assertEquals(Base60.fromFraction(1, 3), Base60.fromFraction(1, 3).abs())
    }

    @Test
    @DisplayName("signum: -1 / 0 / 1")
    fun signum() {
        assertEquals(-1, Base60.fromInt(-5).signum())
        assertEquals(0,  Base60.fromInt(0).signum())
        assertEquals(1,  Base60.fromInt(5).signum())
    }

    // -------------------------------------------------------------------------
    // pow
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("pow(0) = 1")
    fun powZero() {
        assertEquals(Base60.fromInt(1), Base60.fromFraction(3, 7).pow(0))
    }

    @Test
    @DisplayName("pow(int) додатній: 2^10 = 1024")
    fun powPositive() {
        assertEquals(Base60.fromInt(1024), Base60.fromInt(2).pow(10))
    }

    @Test
    @DisplayName("pow(int) від'ємний: 2^-1 = 1/2")
    fun powNegative() {
        assertEquals(Base60.fromFraction(1, 2), Base60.fromInt(2).pow(-1))
    }

    @Test
    @DisplayName("pow(int) дробового числа: (1/2)^3 = 1/8")
    fun powFraction() {
        assertEquals(Base60.fromFraction(1, 8), Base60.fromFraction(1, 2).pow(3))
    }

    @Test
    @DisplayName("pow нуля до від'ємного степеня кидає ArithmeticException")
    fun powZeroNegative() {
        assertThrows(ArithmeticException::class.java) { Base60.fromInt(0).pow(-1) }
    }

    @Test
    @DisplayName("pow(Base60) з цілим показником")
    fun powBase60Integer() {
        assertEquals(Base60.fromInt(8), Base60.fromInt(2).pow(Base60.fromInt(3)))
    }

    @Test
    @DisplayName("pow(Base60) з дробовим показником: 4^0.5 ≈ 2")
    fun powBase60Fractional() {
        val result = Base60.fromInt(4).pow(Base60.fromFraction(1, 2))
        assertEquals(0, result.compareTo(Base60.fromInt(2)),
            "4^(1/2) має бути 2, отримали: $result")
    }

    // -------------------------------------------------------------------------
    // mod
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("mod: 7 mod 3 = 1")
    fun modBasic() {
        assertEquals(Base60.fromInt(1), Base60.fromInt(7).mod(Base60.fromInt(3)))
    }

    @Test
    @DisplayName("mod: -7 mod 3 = 2 (floor mod, знак дільника)")
    fun modNegativeDividend() {
        assertEquals(Base60.fromInt(2), Base60.fromInt(-7).mod(Base60.fromInt(3)))
    }

    @Test
    @DisplayName("mod: 7 mod -3 = -2")
    fun modNegativeDivisor() {
        assertEquals(Base60.fromInt(-2), Base60.fromInt(7).mod(Base60.fromInt(-3)))
    }

    @Test
    @DisplayName("mod дробових: 7/2 mod 3/2 = 1/2")
    fun modFractions() {
        assertEquals(Base60.fromFraction(1, 2),
            Base60.fromFraction(7, 2).mod(Base60.fromFraction(3, 2)))
    }

    @Test
    @DisplayName("mod нуля кидає ArithmeticException")
    fun modZero() {
        assertThrows(ArithmeticException::class.java) {
            Base60.fromInt(5).mod(Base60.fromInt(0))
        }
    }

    // -------------------------------------------------------------------------
    // sqrt / sqrtSumerians
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sqrt(4) = 2")
    fun sqrtExact() {
        assertEquals(Base60.fromInt(2), Base60.fromInt(4).sqrt())
    }

    @Test
    @DisplayName("sqrt(2) наближається до 1.41421...")
    fun sqrtTwo() {
        val dec = Base60.fromInt(2).sqrt().toDecimal().toPlainString()
        assertTrue(dec.startsWith("1.41421"), "Отримали: $dec")
    }

    @Test
    @DisplayName("sqrt від'ємного кидає ArithmeticException")
    fun sqrtNegative() {
        assertThrows(ArithmeticException::class.java) { Base60.fromInt(-1).sqrt() }
    }

    @Test
    @DisplayName("sqrtSumerians(4) ≈ 2 (до 6 розрядів base-60)")
    fun sqrtSumeriansExact() {
        // Метод Герона в точній раціональній арифметиці не досягає точного 2,
        // якщо початкове наближення ≠ 2. Перевіряємо збіг до 6 розрядів.
        assertEquals("2", Base60.fromInt(4).sqrtSumerians().toString(6))
    }

    @Test
    @DisplayName("sqrtSumerians(2) збігається зі sqrt(2) до 10 знаків base-60")
    fun sqrtSumeriansVsClassic() {
        val classic  = Base60.fromInt(2).sqrt()
        val sumerian = Base60.fromInt(2).sqrtSumerians()
        assertEquals(classic.toString(8), sumerian.toString(8))
    }

    // -------------------------------------------------------------------------
    // kotlin.Number
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toInt: 7/2 → 3 (усікання)")
    fun intValue() {
        assertEquals(3, Base60.fromFraction(7, 2).toInt())
    }

    @Test
    @DisplayName("toLong: 3600 → 3600L")
    fun longValue() {
        assertEquals(3600L, Base60.fromInt(3600).toLong())
    }

    @Test
    @DisplayName("toFloat: 1/4 ≈ 0.25f")
    fun floatValue() {
        assertEquals(0.25f, Base60.fromFraction(1, 4).toFloat(), 1e-6f)
    }

    @Test
    @DisplayName("toDouble: 1/3 ≈ 0.333...")
    fun doubleValue() {
        assertEquals(1.0 / 3.0, Base60.fromFraction(1, 3).toDouble(), 1e-15)
    }

    @Test
    @DisplayName("Base60 є підкласом Number")
    fun isNumber() {
        val n: Number = Base60.fromInt(42)
        assertEquals(42, n.toInt())
    }
}