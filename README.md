# Base60 Documentation (Kotlin)

Base60 is an immutable Kotlin library implementing a sexagesimal (base-60) rational number type inspired by Babylonian mathematics. It provides exact rational arithmetic using `BigInteger` internally, storing numbers as numerator/denominator pairs rather than floating-point values. This is a Kotlin rewrite of [SumeriansBabylon](https://github.com/oldengremlin/SumeriansBabylon) (Java).

## Core Capabilities

The library supports exact arithmetic operations, base-60 formatting with periodic fraction detection, parsing from base-60 strings, comparisons, and decimal conversion. Key features include automatic GCD normalization, full immutability, and a representation layer that treats base-60 as display-only. The class extends `kotlin.Number` and implements `Comparable<Base60>`, integrating naturally into Kotlin's type hierarchy.

## Notable Design Approach

The system detects repeating cycles in fractions through remainder tracking: multiplying remainders by 60, dividing by the denominator, and identifying when remainders repeat to mark periodicity. For example, 1/7 converts to the repeating pattern `0.(8:34:17)`.

Construction is available only through factory methods in the companion object (`fromInt`, `fromLong`, `fromFraction`, `fromDecimal`, `parse`) — the primary constructor is private.

## Mathematical Foundation

Base-60 was historically significant because 60 has many divisors: 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60 — making many fractions terminate cleanly rather than repeat. This ancient system persists in modern time and angle measurements.

The library includes `sqrtSumerians()` — the Babylonian method of Heron: `x_{n+1} = (x_n + S/x_n) / 2`, iterated 10 times in exact rational arithmetic. The initial guess is deliberately randomised (`value / Math.random()`) to reflect that the historical algorithm's power lies in its convergence, regardless of the starting point.

## Cuneiform Output

Numbers can be rendered in Unicode Sumerian cuneiform (block U+12000–U+1247F). Since all cuneiform digits are above U+FFFF, they require surrogate pairs and are handled via `Character.toString(codePoint)`. The digit table is built once at class load time in `buildCuneiformDigits()`.

```
𒁹 × 𒌋𒌋 = 𒌋𒌋          (1 × 20 = 20)
𒌋𒌋𒌋 × 𒌋𒌋𒌋 = 𒐄𒌋𒌋𒌋    (30 × 30 = 15:0, тобто 900)
```

## Implementation Details

The library eliminates floating-point precision loss by avoiding `Double` entirely in all exact operations, maintaining rational forms throughout all calculations. When `Double` is used — only in `pow(Base60)` with a fractional exponent — it is explicitly documented as an approximation.

`toDecimal()` returns a `BigDecimal` computed at 50-digit precision (`MathContext(50, RoundingMode.HALF_UP)`). `toBase60WithPeriod()` and `toString(precision)` are independent formatting paths: the former finds the exact cycle, the latter truncates to a given number of sexagesimal places.
