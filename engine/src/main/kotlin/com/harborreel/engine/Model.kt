package com.harborreel.engine

enum class GameKind {
    LOCK_RESPIN,
    GALE,
    FREE_SPINS,
    JAR,
    WAYS,
    TUMBLE,
    CLUSTER,
    WHEEL,
}

data class Cell(
    val symbol: Symbol,
    val prizeCoins: Int = 0,
    val prizeName: String? = null,
)

data class LineWin(
    val index: Int,
    val symbol: Symbol,
    val count: Int,
    val amount: Long,
    val cells: List<Pair<Int, Int>>,
)

data class LockFrame(
    val livesLeft: Int,
    val grid: List<List<Cell>>,
)

data class FreeSpinFrame(
    val grid: List<List<Cell>>,
    val wins: List<LineWin>,
    val win: Long,
    val spinsLeft: Int,
    val wildReel: Int,
    val extraSpins: Int,
)

sealed interface FeatureResult {
    val title: String
    val detail: String
    val amount: Long
}

data class LockRespin(
    val frames: List<LockFrame>,
    override val amount: Long,
    val filledBoard: Boolean,
) : FeatureResult {
    override val title: String = "Lock & Respin"
    override val detail: String =
        if (filledBoard) "Every spot filled. Fleet prize added." else "Buoys cashed in."
}

data class SquallPrize(
    val stageName: String,
    val multiplier: Int,
    val baseAmount: Long,
    override val amount: Long,
) : FeatureResult {
    override val title: String = "Squall Prize"
    override val detail: String = "$stageName  ×$multiplier"
}

data class FreeSpins(
    val frames: List<FreeSpinFrame>,
    override val amount: Long,
) : FeatureResult {
    override val title: String = "Night Service"
    override val detail: String = "${frames.size} free spins"
}

data class TumbleFrame(
    val grid: List<List<Cell>>,
    val wins: List<LineWin>,
    val multiplier: Int,
    val win: Long,
)

data class Tumble(
    val frames: List<TumbleFrame>,
    val settled: List<List<Cell>>,
    override val amount: Long,
) : FeatureResult {
    override val title: String = "Reel Drop"
    override val detail: String = if (frames.isEmpty()) "Symbols fell in" else "${frames.size} more drop${if (frames.size == 1) "" else "s"}"
}

data class WheelSpin(
    val wedge: String,
    val wedgeIndex: Int,
    override val amount: Long,
) : FeatureResult {
    override val title: String = "Prize Wheel"
    override val detail: String = wedge
}

data class JarTip(
    val jarBefore: Long,
    val multiplier: Int,
    override val amount: Long,
    val consolation: Boolean,
) : FeatureResult {
    override val title: String = "Tip the Jar"
    override val detail: String =
        if (consolation) "The jar was empty. Basket pays a small tip." else "Jar ×$multiplier"
}

data class SpinOutcome(
    val spinId: Long,
    val gameId: String,
    val bet: Long,
    val grid: List<List<Cell>>,
    val lineWins: List<LineWin>,
    val scatterPay: Long,
    val feature: FeatureResult?,
    val totalWin: Long,
    val skiffAfter: Int,
    val skiffGain: Int,
    val jarAfter: Long,
    val jarGain: Long,
)

data class HistoryEntry(
    val gameId: String,
    val bet: Long,
    val win: Long,
)

enum class Payout(
    val id: String,
    val title: String,
    val blurb: String,
    /** Extra feature symbols wound onto each reel, spread along the strip. */
    val extraFeatures: Int,
) {
    QUIET("quiet", "Quiet", "Feature symbols stay scarce.", 0),
    CRUISE("cruise", "Cruise", "Feature symbols show up about as often as a cruise-floor video.", 1),
    GENEROUS("generous", "Generous", "Feature symbols come up often.", 2),
    ;

    companion object {
        fun fromId(id: String?): Payout = entries.firstOrNull { it.id == id } ?: CRUISE
    }
}

data class PlayerState(
    val credits: Long = 20_000,
    val denom: Int = 1,
    val betPerLine: Int = 1,
    val payout: Payout = Payout.CRUISE,
    val lineId: String = CruiseLines.defaultId,
    val points: Map<String, Long> = CruiseLines.ids.associateWith { 0L },
    /** Wagered cents on each line that have not yet made a whole point. */
    val pointRemainder: Map<String, Long> = CruiseLines.ids.associateWith { 0L },
    val skiff: Int = 0,
    val jar: Long = 0L,
    val history: List<HistoryEntry> = emptyList(),
) {
    /** Cash wager for one spin, in cents. Denomination times credits played. */
    val bet: Int
        get() = denom * betPerLine * LINE_COUNT
}

const val LINE_COUNT: Int = 20

data class GameDef(
    val id: String,
    val name: String,
    val blurb: String,
    val featureLabel: String,
    val rules: String,
    val kind: GameKind,
    val reels: List<List<Symbol>>,
    val paylines: List<List<Int>>,
    val pays: Map<Symbol, Map<Int, Int>>,
    val wild: Symbol,
    val blockers: Set<Symbol>,
    val scatter: Symbol? = null,
    val scatterPays: Map<Int, Int> = emptyMap(),
)

data class NamedPrize(val coins: Int, val name: String?)

data class SkiffStage(val name: String, val multiplier: Int, val nextAt: Int?)
