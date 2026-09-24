package com.harborreel.engine

data class CruiseLine(
    val id: String,
    val name: String,
    /** Slot coin-in, in cents, required for one casino point. */
    val centsPerPoint: Int,
)

object CruiseLines {
    val all: List<CruiseLine> = listOf(
        CruiseLine("carnival", "🎪 Carnivore", 200),
        CruiseLine("royal", "👑 Regal Caribbean", 500),
        CruiseLine("norwegian", "🧭 Nordician", 500),
        CruiseLine("princess", "🖨️ Prints-S", 200),
        CruiseLine("celebrity", "✨ Celebrating", 500),
        CruiseLine("holland", "⚓ All Hand America", 200),
        CruiseLine("msc", "🌊 MS-Sea", 300),
        CruiseLine("virgin", "🦊 Vixen Voyagers", 500),
    )

    fun byId(id: String): CruiseLine = all.firstOrNull { it.id == id } ?: all.first()

    fun rateLabel(line: CruiseLine): String {
        val dollars = line.centsPerPoint / 100
        val cents = line.centsPerPoint % 100
        val money = if (cents == 0) "$$dollars" else "$$dollars.${cents.toString().padStart(2, '0')}"
        return "1 point per $money slot coin-in"
    }

    val ids: List<String> = all.map { it.id }
    const val defaultId: String = "carnival"

    fun nameOf(id: String): String = all.firstOrNull { it.id == id }?.name ?: all.first().name
}

val DENOM_CENTS: List<Int> = listOf(1, 2, 5, 10, 25, 50, 100, 200, 500)
val BET_PER_LINE: List<Int> = listOf(1, 2, 3, 5, 10, 15, 20)

fun PlayerState.normalized(): PlayerState {
    val denom = if (denom in DENOM_CENTS) denom else 1
    val level = if (betPerLine in BET_PER_LINE) betPerLine else 1
    val line = if (lineId in CruiseLines.ids) lineId else CruiseLines.defaultId
    val points = CruiseLines.ids.associateWith { id -> this.points[id] ?: 0L }
    val remainder = CruiseLines.ids.associateWith { id -> (this.pointRemainder[id] ?: 0L).coerceAtLeast(0) }
    return copy(
        denom = denom,
        betPerLine = level,
        lineId = line,
        points = points,
        pointRemainder = remainder,
        credits = credits.coerceAtLeast(0),
    )
}

fun awardSlotPoints(
    lineId: String,
    wagerCents: Long,
    points: Map<String, Long>,
    remainder: Map<String, Long>,
): Pair<Map<String, Long>, Map<String, Long>> {
    val rate = CruiseLines.byId(lineId).centsPerPoint.toLong()
    val pool = (remainder[lineId] ?: 0L) + wagerCents
    val gained = pool / rate
    val left = pool % rate
    val nextPoints = points + (lineId to ((points[lineId] ?: 0L) + gained))
    val nextRemainder = remainder + (lineId to left)
    return nextPoints to nextRemainder
}

sealed interface SpinAttempt {
    data class Ok(val outcome: SpinOutcome) : SpinAttempt
    data object Insufficient : SpinAttempt
}

class Casino(
    private val rng: RandomSource = Rng(),
    initial: PlayerState = PlayerState(),
) {
    var state: PlayerState = initial.normalized()
        private set

    private var nextId: Long = 1L

    fun spin(gameId: String): SpinAttempt {
        if (state.credits < state.bet) return SpinAttempt.Insufficient
        val game = Catalog.byId(gameId)
        val (next, outcome) = resolveSpin(game, state, rng, nextId)
        nextId += 1
        state = next
        return SpinAttempt.Ok(outcome)
    }

    fun nudgeDenom(direction: Int) {
        val index = DENOM_CENTS.indexOf(state.denom).coerceAtLeast(0)
        val next = (index + direction).coerceIn(0, DENOM_CENTS.lastIndex)
        state = state.copy(denom = DENOM_CENTS[next])
    }

    fun nudgeBet(direction: Int) {
        val index = BET_PER_LINE.indexOf(state.betPerLine).coerceAtLeast(0)
        val next = (index + direction).coerceIn(0, BET_PER_LINE.lastIndex)
        state = state.copy(betPerLine = BET_PER_LINE[next])
    }

    fun setLine(lineId: String) {
        if (lineId !in CruiseLines.ids) return
        state = state.copy(lineId = lineId)
    }

    fun setPayout(payout: Payout) {
        state = state.copy(payout = payout)
    }

    fun addCredits(amount: Long) {
        if (amount <= 0) return
        state = state.copy(credits = state.credits + amount)
    }

    fun setCash(cents: Long) {
        state = state.copy(credits = cents.coerceAtLeast(0))
    }

    fun clearCash() {
        state = state.copy(credits = 0)
    }

    fun clearHistory() {
        state = state.copy(history = emptyList())
    }

    fun resetPoints() {
        val zero = CruiseLines.ids.associateWith { 0L }
        state = state.copy(points = zero, pointRemainder = zero)
    }

    fun resetLinePoints(lineId: String) {
        if (lineId !in CruiseLines.ids) return
        state = state.copy(
            points = state.points + (lineId to 0L),
            pointRemainder = state.pointRemainder + (lineId to 0L),
        )
    }
}

object SaveCodec {
    fun encode(state: PlayerState): String = buildString {
        appendLine("v3")
        appendLine("earn=slot")
        appendLine("credits=${state.credits}")
        appendLine("denom=${state.denom}")
        appendLine("betPerLine=${state.betPerLine}")
        appendLine("bet=${state.bet}")
        appendLine("line=${state.lineId}")
        appendLine("payout=${state.payout.id}")
        appendLine("skiff=${state.skiff}")
        appendLine("jar=${state.jar}")
        appendLine("points=" + state.points.entries.joinToString(",") { "${it.key}:${it.value}" })
        appendLine("remainder=" + state.pointRemainder.entries.joinToString(",") { "${it.key}:${it.value}" })
        appendLine("history=" + state.history.joinToString(",") { "${it.gameId}:${it.bet}:${it.win}" })
    }

    fun decode(text: String?): PlayerState {
        if (text.isNullOrBlank()) return PlayerState()
        return try {
            val values = text.lineSequence()
                .map { it.trim() }
                .filter { it.contains('=') }
                .associate { line ->
                    val cut = line.indexOf('=')
                    line.substring(0, cut) to line.substring(cut + 1)
                }
            val hadDenom = values.containsKey("denom")
            val legacyBet = values["bet"]?.toIntOrNull()
            val betPerLine = values["betPerLine"]?.toIntOrNull()
                ?: ((legacyBet ?: LINE_COUNT) / LINE_COUNT).coerceAtLeast(1)
            val scale = if (hadDenom) 1L else 100L
            val rawPoints = parsePoints(values["points"])
            val slotEarn = values["earn"] == "slot"
            val points = if (slotEarn) {
                rawPoints
            } else {
                rawPoints.mapValues { (id, cents) -> cents / CruiseLines.byId(id).centsPerPoint }
            }
            val remainder = if (slotEarn) {
                parsePoints(values["remainder"])
            } else {
                rawPoints.mapValues { (id, cents) -> cents % CruiseLines.byId(id).centsPerPoint }
            }
            PlayerState(
                credits = (values["credits"]?.toLongOrNull() ?: 20_000) * scale,
                denom = values["denom"]?.toIntOrNull() ?: 1,
                betPerLine = betPerLine,
                lineId = values["line"] ?: CruiseLines.defaultId,
                payout = Payout.fromId(values["payout"]),
                points = points,
                pointRemainder = remainder,
                skiff = values["skiff"]?.toIntOrNull() ?: 0,
                jar = (values["jar"]?.toLongOrNull() ?: 0L) * scale,
                history = parseHistory(values["history"]).map { entry ->
                    if (hadDenom) entry else entry.copy(bet = entry.bet * scale, win = entry.win * scale)
                },
            ).normalized()
        } catch (_: RuntimeException) {
            PlayerState()
        }
    }

    private fun parsePoints(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(',').mapNotNull { part ->
            val bits = part.split(':')
            if (bits.size != 2) return@mapNotNull null
            val amount = bits[1].toLongOrNull() ?: return@mapNotNull null
            bits[0] to amount
        }.toMap()
    }

    private fun parseHistory(raw: String?): List<HistoryEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',').mapNotNull { part ->
            val bits = part.split(':')
            if (bits.size != 3) return@mapNotNull null
            val bet = bits[1].toLongOrNull() ?: return@mapNotNull null
            val win = bits[2].toLongOrNull() ?: return@mapNotNull null
            HistoryEntry(bits[0], bet, win)
        }
    }
}
