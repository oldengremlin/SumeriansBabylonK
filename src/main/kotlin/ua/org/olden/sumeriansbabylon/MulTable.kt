package ua.org.olden.sumeriansbabylon

/**
 * Вавілонська таблиця множення клинописом.
 * Вавілоняни мали «таблиці множення» для фіксованих множників (1–59)
 * з результатами у системі base-60.
 */
private const val MAX_FACTOR = 12

fun main() {
    println("=== Вавілонська таблиця множення (base-60, клинопис) ===")
    println()

    // Заголовок
    print("%-6s".format("×"))
    for (col in 1..MAX_FACTOR) print("%8d".format(col))
    println()
    println("-".repeat(6 + MAX_FACTOR * 8))

    for (row in 1..MAX_FACTOR) {
        print("%-6d".format(row))
        for (col in 1..MAX_FACTOR) {
            val product = Base60.fromInt(row).multiply(Base60.fromInt(col))
            print("%8s".format(product.toString()))
        }
        println()
    }

    println()
    println("=== Те саме клинописом ===")
    println()

    for (row in 1..MAX_FACTOR) {
        for (col in 1..MAX_FACTOR) {
            val a = Base60.fromInt(row)
            val b = Base60.fromInt(col)
            val product = a.multiply(b)
            println("  ${a.toSumerianString()} × ${b.toSumerianString()} = ${product.toSumerianString()}")
        }
    }
}