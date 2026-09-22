package com.harborreel.engine

fun evaluateLines(game: GameDef, columns: List<List<Cell>>, coin: Long): List<LineWin> {
    val wins = mutableListOf<LineWin>()
    game.paylines.forEachIndexed { index, rows ->
        val symbols = rows.mapIndexed { reel, row -> columns[reel][row].symbol }
        val resolved = resolveRun(symbols, game.wild, game.blockers) ?: return@forEachIndexed
        val (symbol, count) = resolved
        val pay = game.pays[symbol]?.get(count) ?: 0
        if (pay > 0 && count >= 3) {
            val cells = (0 until count).map { reel -> reel to rows[reel] }
            wins += LineWin(index, symbol, count, pay.toLong() * coin, cells)
        }
    }
    return wins
}

internal fun resolveRun(symbols: List<Symbol>, wild: Symbol, blockers: Set<Symbol>): Pair<Symbol, Int>? {
    var count = 0
    var target: Symbol? = null
    for (symbol in symbols) {
        if (symbol in blockers || symbol == Symbol.BLANK) break
        if (symbol == wild) {
            count++
            continue
        }
        if (target == null) {
            target = symbol
            count++
            continue
        }
        if (symbol == target) count++ else break
    }
    if (count == 0) return null
    return (target ?: wild) to count
}
