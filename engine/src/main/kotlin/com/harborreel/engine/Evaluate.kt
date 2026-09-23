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

fun evaluateWays(game: GameDef, columns: List<List<Cell>>, coin: Long): List<LineWin> {
    val wins = mutableListOf<LineWin>()
    var index = 0
    for (symbol in game.pays.keys) {
        if (symbol == game.wild) continue
        val hit = wayRun(columns, symbol, game.wild) ?: continue
        val pay = game.pays[symbol]?.get(hit.length) ?: 0
        if (pay <= 0) continue
        wins += LineWin(index++, symbol, hit.length, pay.toLong() * hit.ways * coin, hit.cells)
    }
    val wildHit = wayRun(columns, game.wild, game.wild) ?: return wins
    val wildPay = game.pays[game.wild]?.get(wildHit.length) ?: 0
    if (wildPay > 0) {
        wins += LineWin(index, game.wild, wildHit.length, wildPay.toLong() * wildHit.ways * coin, wildHit.cells)
    }
    return wins
}

private data class WayHit(val length: Int, val ways: Int, val cells: List<Pair<Int, Int>>)

private fun wayRun(columns: List<List<Cell>>, symbol: Symbol, wild: Symbol): WayHit? {
    var ways = 1
    val cells = mutableListOf<Pair<Int, Int>>()
    var length = 0
    for (reel in columns.indices) {
        var count = 0
        for (row in columns[reel].indices) {
            val found = columns[reel][row].symbol
            val matches = if (symbol == wild) found == wild else found == symbol || found == wild
            if (matches) {
                count++
                cells += reel to row
            }
        }
        if (count == 0) break
        ways *= count
        length++
    }
    if (length < 3) return null
    return WayHit(length, ways, cells.filter { it.first < length })
}

fun evaluateClusters(game: GameDef, columns: List<List<Cell>>, coin: Long): List<LineWin> {
    val width = columns.size
    val height = columns.first().size
    val wild = game.wild
    val seen = Array(width) { BooleanArray(height) }
    val clusters = mutableListOf<MutableList<Pair<Int, Int>>>()
    val symbols = mutableListOf<Symbol>()

    for (reel in 0 until width) {
        for (row in 0 until height) {
            val symbol = columns[reel][row].symbol
            if (seen[reel][row] || symbol == wild || symbol in game.blockers || symbol == Symbol.BLANK) continue
            val cells = mutableListOf<Pair<Int, Int>>()
            val stack = ArrayDeque<Pair<Int, Int>>()
            stack.add(reel to row)
            seen[reel][row] = true
            while (stack.isNotEmpty()) {
                val (r, c) = stack.removeLast()
                cells += r to c
                for ((nr, nc) in orthogonal(r, c, width, height)) {
                    if (seen[nr][nc] || columns[nr][nc].symbol != symbol) continue
                    seen[nr][nc] = true
                    stack.add(nr to nc)
                }
            }
            clusters += cells
            symbols += symbol
        }
    }

    val looseWilds = mutableListOf<Pair<Int, Int>>()
    for (reel in 0 until width) {
        for (row in 0 until height) {
            if (columns[reel][row].symbol != wild) continue
            var best = -1
            var bestPay = -1
            for (index in clusters.indices) {
                val touches = clusters[index].any { (r, c) -> adjacent(r, c, reel, row) }
                if (!touches) continue
                val pay = game.pays[symbols[index]]?.get(5) ?: 0
                if (pay > bestPay) {
                    bestPay = pay
                    best = index
                }
            }
            if (best >= 0) clusters[best] += reel to row else looseWilds += reel to row
        }
    }
    if (looseWilds.isNotEmpty()) {
        clusters += looseWilds.toMutableList()
        symbols += wild
    }

    val wins = mutableListOf<LineWin>()
    clusters.forEachIndexed { index, cells ->
        if (cells.size < 3) return@forEachIndexed
        val symbol = symbols[index]
        val pay = clusterPay(game, symbol, cells.size)
        if (pay > 0) wins += LineWin(index, symbol, cells.size, pay.toLong() * coin, cells)
    }
    return wins
}

private fun clusterPay(game: GameDef, symbol: Symbol, size: Int): Int {
    val table = game.pays[symbol] ?: return 0
    if (size <= 5) return table[size] ?: 0
    val five = table[5] ?: return 0
    return five + (size - 5) * (five / 2)
}

private fun orthogonal(reel: Int, row: Int, width: Int, height: Int): List<Pair<Int, Int>> =
    listOf(reel - 1 to row, reel + 1 to row, reel to row - 1, reel to row + 1)
        .filter { (r, c) -> r in 0 until width && c in 0 until height }

private fun adjacent(reel: Int, row: Int, otherReel: Int, otherRow: Int): Boolean =
    kotlin.math.abs(reel - otherReel) + kotlin.math.abs(row - otherRow) == 1
