# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Компіляція
mvn compile

# Запуск всіх тестів
mvn test

# Запуск одного тестового класу
mvn test -Dtest=Base60Test

# Запуск одного тестового методу
mvn test -Dtest=Base60Test#addFractions

# Запуск основної демонстрації (SumeriansBabylon.kt)
mvn exec:java

# Запуск таблиці множення (MulTable.kt)
mvn exec:java -Dexec.mainClass=ua.org.olden.sumeriansbabylon.MulTableKt
```

## Архітектура

Бібліотека реалізує точну шістдесяткову (base-60) арифметику — числову систему шумерів/вавілонян.

### Base60 (`Base60.kt`)

Центральний клас. Числа зберігаються як раціональні дроби `numerator/denominator` (обидва `BigInteger`), нормалізовані через GCD. Знак завжди в чисельнику, знаменник завжди > 0.

**Формат запису** (рядки): `цілі_розряди.дробові_розряди`, де розряди base-60 розділені `:`.
Приклад: `"2:46:58.30:15"` — два розряди цілої частини, два дробових.

**Ключові методи:**
- `parse(String)` — парсить рядок формату `D:D:D.D:D`
- `toString(precision)` / `toString()` — виводить у base-60
- `toBase60WithPeriod()` — виявляє і позначає дужками циклічний повтор у дробовій частині, напр. `"0.(8:34:17)"`
- `toSumerianString()` — виводить клинописом Unicode (U+12xxx). Роздільник цілої/дробової частин: `𒑲` (U+12472), нуль: `𒑱` (U+12471)
- `sqrtSumerians()` — квадратний корінь методом Герона (вавілонський алгоритм, 10 ітерацій)

### MulTable.kt

Окремий `main`-файл. Виводить вавілонську таблицю множення 12×12 у base-60 і клинописом.

### SumeriansBabylon.kt

Точка входу за замовчуванням (`exec.mainClass`). Демонструє всі можливості `Base60`: арифметику, форматування, pow, mod, sqrt.

## Технічний стек

- Kotlin 2.2.0, JVM target 21
- JUnit Jupiter 6.0.3 (параметризовані тести через `@CsvSource`)
- Maven (без Gradle)
