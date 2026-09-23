package com.harborreel.engine

fun openingGrid(game: GameDef): List<List<Cell>> =
    game.reels.map { reel -> List(3) { row -> Cell(reel[row]) } }

fun resolveSpin(
    game: GameDef,
    state: PlayerState,
    rng: RandomSource,
    spinId: Long,
): Pair<PlayerState, SpinOutcome> {
    val wager = state.bet.toLong()
    val coin = wager / game.paylines.size
    check(coin > 0) { "Bet must cover every payline." }

    var credits = state.credits - wager
    val (points, remainder) = awardSlotPoints(state.lineId, wager, state.points, state.pointRemainder)

    val window = spinWindow(game, rng, state.payout.extraFeatures)
    val grid = window.grid
    val lineWins = when (game.kind) {
        GameKind.WAYS, GameKind.TUMBLE -> evaluateWays(game, grid, coin)
        GameKind.CLUSTER -> evaluateClusters(game, grid, coin)
        else -> evaluateLines(game, grid, coin)
    }
    var total = lineWins.sumOf { it.amount }

    val scatterCount = countSymbol(grid, game.scatter)
    val scatterPay = (game.scatterPays[scatterCount] ?: 0).toLong() * coin
    total += scatterPay

    var skiff = state.skiff
    var skiffGain = 0
    var jar = state.jar
    var jarGain = 0L
    var feature: FeatureResult? = null

    when (game.kind) {
        GameKind.LOCK_RESPIN -> {
            if (countSymbol(grid, Symbol.BUOY) >= 5) {
                val result = lockRespin(grid, rng, coin)
                feature = result
                total += result.amount
            }
        }
        GameKind.GALE -> {
            skiffGain = grid.sumOf { reel ->
                reel.sumOf { cell -> materialPoints(cell.symbol) }
            }
            skiff += skiffGain
            if (scatterCount >= 3) {
                val stage = skiffStage(skiff)
                val baseCoins = rng.pick(SQUALL_BASE)
                val baseAmount = baseCoins.toLong() * coin
                val amount = baseAmount * stage.multiplier
                feature = SquallPrize(stage.name, stage.multiplier, baseAmount, amount)
                total += amount
                skiff = 0
            }
        }
        GameKind.FREE_SPINS -> {
            if (scatterCount >= 3) {
                val result = freeSpins(game, rng, coin, scatterCount)
                feature = result
                total += result.amount
            }
        }
        GameKind.JAR -> {
            val potCoins = grid.sumOf { reel ->
                reel.filter { it.symbol == Symbol.COIN }.sumOf { it.prizeCoins }
            }
            jarGain = potCoins * coin
            jar += jarGain
            if (scatterCount >= 3) {
                val multiplier = rng.pick(TIP_MULTIPLIERS)
                val consolation = jar == 0L
                val amount = if (consolation) coin * 10 else jar * multiplier
                feature = JarTip(jar, multiplier, amount, consolation)
                total += amount
                jar = 0L
            }
        }
        GameKind.WAYS, GameKind.CLUSTER -> Unit
        GameKind.TUMBLE -> {
            if (lineWins.isNotEmpty()) {
                val drop = tumbleAfter(game, window.reels, window.stops, grid, lineWins, coin)
                feature = drop
                total += drop.amount
            }
        }
        GameKind.WHEEL -> {
            if (scatterCount >= 3) {
                val index = rng.pickIndex(WHEEL_WEDGES)
                val wedge = WHEEL_WEDGES[index].first
                val amount = wedge.coins.toLong() * coin
                feature = WheelSpin(wedge.name ?: "Prize", index, amount)
                total += amount
            }
        }
    }

    credits += total
    val history = (listOf(HistoryEntry(game.id, wager, total)) + state.history).take(40)
    val next = state.copy(
        credits = credits,
        points = points,
        pointRemainder = remainder,
        skiff = skiff,
        jar = jar,
        history = history,
    )
    val outcome = SpinOutcome(
        spinId = spinId,
        gameId = game.id,
        bet = wager,
        grid = grid,
        lineWins = lineWins,
        scatterPay = scatterPay,
        feature = feature,
        totalWin = total,
        skiffAfter = skiff,
        skiffGain = skiffGain,
        jarAfter = jar,
        jarGain = jarGain,
    )
    return next to outcome
}

private data class SpinWindow(
    val stops: IntArray,
    val grid: List<List<Cell>>,
    val reels: List<List<Symbol>>,
)

private fun featureSymbol(game: GameDef): Symbol? = when (game.kind) {
    GameKind.LOCK_RESPIN -> Symbol.BUOY
    GameKind.GALE, GameKind.FREE_SPINS, GameKind.JAR, GameKind.WHEEL -> game.scatter
    else -> null
}

private fun woundReel(reel: List<Symbol>, feature: Symbol?, extra: Int): List<Symbol> {
    if (feature == null || extra <= 0 || feature !in reel) return reel
    val out = reel.toMutableList()
    val step = (reel.size / (extra + 1)).coerceAtLeast(1)
    repeat(extra) { index ->
        val at = ((index + 1) * step + index).coerceAtMost(out.size)
        out.add(at, feature)
    }
    return out
}

private fun spinWindow(game: GameDef, rng: RandomSource, extraFeatures: Int): SpinWindow {
    val feature = featureSymbol(game)
    val reels = game.reels.map { woundReel(it, feature, extraFeatures) }
    val stops = IntArray(reels.size)
    val grid = reels.mapIndexed { reelIndex, reel ->
        val stop = rng.nextInt(reel.size)
        stops[reelIndex] = stop
        List(3) { row ->
            val symbol = reel[(stop + row) % reel.size]
            decorate(game, symbol, rng)
        }
    }
    return SpinWindow(stops, grid, reels)
}

private fun spinGrid(game: GameDef, rng: RandomSource, extraFeatures: Int): List<List<Cell>> =
    spinWindow(game, rng, extraFeatures).grid

private fun decorate(game: GameDef, symbol: Symbol, rng: RandomSource): Cell = when {
    game.kind == GameKind.LOCK_RESPIN && symbol == Symbol.BUOY -> {
        val prize = rng.pick(BUOY_PRIZES)
        Cell(symbol, prize.coins, prize.name)
    }
    game.kind == GameKind.JAR && symbol == Symbol.COIN -> {
        Cell(symbol, rng.pick(POT_COINS), null)
    }
    else -> Cell(symbol)
}

private fun materialPoints(symbol: Symbol): Int = when (symbol) {
    Symbol.KEEL -> 1
    Symbol.PLANK -> 2
    Symbol.CANVAS -> 3
    else -> 0
}

private fun countSymbol(grid: List<List<Cell>>, symbol: Symbol?): Int {
    if (symbol == null) return 0
    return grid.sumOf { reel -> reel.count { it.symbol == symbol } }
}

private fun lockRespin(start: List<List<Cell>>, rng: RandomSource, coin: Long): LockRespin {
    val board = start.map { reel ->
        reel.map { cell -> if (cell.symbol == Symbol.BUOY) cell else Cell(Symbol.BLANK) }.toMutableList()
    }
    val frames = mutableListOf<LockFrame>()
    var lives = 3

    fun filled(): Int = board.sumOf { reel -> reel.count { it.symbol == Symbol.BUOY } }

    while (lives > 0 && filled() < 15) {
        lives -= 1
        var added = false
        for (reel in board.indices) {
            for (row in board[reel].indices) {
                if (board[reel][row].symbol == Symbol.BLANK && rng.nextInt(100) < LOCK_FILL_PERCENT) {
                    val prize = rng.pick(BUOY_PRIZES)
                    board[reel][row] = Cell(Symbol.BUOY, prize.coins, prize.name)
                    added = true
                }
            }
        }
        if (added) lives = 3
        frames += LockFrame(lives, board.map { it.toList() })
    }

    val full = filled() == 15
    var coins = board.sumOf { reel ->
        reel.sumOf { cell -> if (cell.symbol == Symbol.BUOY) cell.prizeCoins else 0 }
    }
    if (full) coins += FLEET_BONUS_COINS
    return LockRespin(frames, coins.toLong() * coin, full)
}

private fun tumbleAfter(
    game: GameDef,
    reels: List<List<Symbol>>,
    initialStops: IntArray,
    initialGrid: List<List<Cell>>,
    initialWins: List<LineWin>,
    coin: Long,
): Tumble {
    var stops = initialStops.copyOf()
    var grid = initialGrid
    var pending = initialWins
    var multiplier = 1
    var extra = 0L
    val frames = mutableListOf<TumbleFrame>()
    var guard = 0
    while (pending.isNotEmpty() && guard < TUMBLE_CAP) {
        val removed = pending.flatMap { it.cells }.toSet()
        val collapsed = collapse(reels, stops, grid, removed)
        stops = collapsed.first
        grid = collapsed.second
        multiplier = minOf(TUMBLE_MULT_CAP, multiplier + 1)
        pending = evaluateWays(game, grid, coin)
        guard++
        if (pending.isEmpty()) break
        val win = pending.sumOf { it.amount } * multiplier
        extra += win
        frames += TumbleFrame(grid, pending, multiplier, win)
    }
    return Tumble(frames, grid, extra)
}

private fun collapse(
    reels: List<List<Symbol>>,
    stops: IntArray,
    grid: List<List<Cell>>,
    removed: Set<Pair<Int, Int>>,
): Pair<IntArray, List<List<Cell>>> {
    val nextStops = stops.copyOf()
    val nextGrid = grid.mapIndexed { reel, column ->
        val strip = reels[reel]
        val kept = column.mapIndexedNotNull { row, cell ->
            if (reel to row in removed) null else cell
        }
        val need = column.size - kept.size
        val incoming = ArrayDeque<Cell>()
        repeat(need) {
            nextStops[reel] = (nextStops[reel] - 1 + strip.size) % strip.size
            incoming.addFirst(Cell(strip[nextStops[reel]]))
        }
        incoming + kept
    }
    return nextStops to nextGrid
}

private fun awardedSpins(scatterCount: Int): Int = when {
    scatterCount >= 5 -> 20
    scatterCount == 4 -> 12
    else -> 8
}

private fun freeSpins(game: GameDef, rng: RandomSource, coin: Long, trigger: Int): FreeSpins {
    var left = awardedSpins(trigger)
    val frames = mutableListOf<FreeSpinFrame>()
    var total = 0L
    var played = 0
    while (left > 0 && played < 80) {
        played += 1
        left -= 1
        val natural = spinGrid(game, rng, 0)
        val wildReel = rng.nextInt(game.reels.size)
        val grid = natural.mapIndexed { index, reel ->
            if (index != wildReel) {
                reel
            } else {
                reel.map { cell ->
                    if (cell.symbol == game.scatter) cell else Cell(game.wild)
                }
            }
        }
        val wins = evaluateLines(game, grid, coin)
        val scatters = countSymbol(natural, game.scatter)
        var extra = 0
        val scatterPay = if (scatters >= 3 && played < 80) {
            extra = awardedSpins(scatters)
            left += extra
            (game.scatterPays[scatters] ?: 0).toLong() * coin
        } else {
            0L
        }
        val win = wins.sumOf { it.amount } + scatterPay
        total += win
        frames += FreeSpinFrame(grid, wins, win, left, wildReel, extra)
    }
    return FreeSpins(frames, total)
}
