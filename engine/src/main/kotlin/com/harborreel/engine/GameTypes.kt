package com.harborreel.engine

/** A kind of cabinet, described without a commercial title or studio name. */
data class FloorExample(
    val name: String,
    val note: String,
)

enum class GameType(
    val title: String,
    val alsoCalled: String?,
    val description: String,
    val floorExamples: List<FloorExample>,
    val floorNote: String? = null,
) {
    LOCK_AND_RESPIN(
        title = "Lock and Respin",
        alsoCalled = "hold and spin",
        description = "These machines rely on locking trigger symbols in place and resetting respins to chase jackpots or cash multipliers.",
        floorExamples = listOf(
            FloorExample("Linking dragons", "hold-and-spin, orbs lock in place"),
            FloorExample("Lantern mysteries", "hold-and-spin, lamps lock in"),
            FloorExample("Temple coins", "mechanical reels, coins lock in place"),
        ),
    ),
    BUILD_AND_BONUS(
        title = "Build and Bonus",
        alsoCalled = "Progression & Accumulation",
        description = "These slots feature progress meters, collection elements, or evolving features that build up over time toward a bonus payoff.",
        floorExamples = listOf(
            FloorExample("Colored coin bowls", "coins fill pots, then a bonus"),
            FloorExample("Climbing meters", "a meter builds, then a bigger bonus"),
            FloorExample("Growing wilds", "a wild spreads as play goes on"),
        ),
    ),
    FREE_SPINS(
        title = "Free Spins",
        alsoCalled = null,
        description = "The staple feature found across traditional and modern video slots where landing scatter symbols awards a batch of complimentary bonus spins.",
        floorExamples = listOf(
            FloorExample("Gold animal herd", "free spins, coins turn animals into the top symbol"),
            FloorExample("Houses of straw", "free spins, straw then sticks then brick, then a bigger house"),
            FloorExample("Drum grid", "free spins, pick a wider grid"),
        ),
    ),
    POT_BONUS(
        title = "Pot Bonus",
        alsoCalled = null,
        description = "These games feature visual pots, chests, or lucky red envelopes on the screen that randomly or via symbols trigger a pick-em jackpot round.",
        floorExamples = listOf(
            FloorExample("Herd and a wheel", "free spins, plus a pot that spins a wheel"),
            FloorExample("Wealth pots", "a pot opens a pick-a-prize"),
            FloorExample("Bursting pots", "stacked pots pop for a set prize"),
        ),
    ),
    TUMBLING_WAYS(
        title = "Tumbling Ways",
        alsoCalled = "Cascading / Avalanche Reels",
        description = "Games where winning symbols disappear and new ones cascade down from above to create potential chain-reaction wins on a single spin.",
        floorExamples = listOf(
            FloorExample("Rolling herd", "wins fall away, and a wheel can spin"),
            FloorExample("Fire cascade", "wins drop out and new symbols fall"),
            FloorExample("Bull tumble", "wins cascade on a big grid"),
        ),
    ),
    WAYS_243(
        title = "243 Ways",
        alsoCalled = "Ways-to-Win",
        description = "Slots that discard standard paylines in favor of adjacent-reel matching combinations resulting in 243 ways to win.",
        floorExamples = listOf(
            FloorExample("Dragon ways", "243 ways, neighboring reels pay"),
            FloorExample("Extra reel rows", "bonus rows open more ways"),
            FloorExample("Left-to-right ways", "wins run across reels, not on a line"),
        ),
    ),
    CLUSTER_PAYS(
        title = "Cluster Pays",
        alsoCalled = null,
        description = "Grid-based games that discard lines and ways entirely, paying out when clusters of matching symbols connect horizontally or vertically.",
        floorExamples = listOf(
            FloorExample("Touching clusters", "connected symbols pay, no lines"),
        ),
        floorNote = "Cluster grids show up less often on a cruise floor than lock-and-respin cabinets.",
    ),
    PRIZE_WHEEL(
        title = "Prize Wheel",
        alsoCalled = null,
        description = "Games centered around spinning a mechanical or digital bonus wheel for high-value multipliers, credits, or progressive jackpots.",
        floorExamples = listOf(
            FloorExample("Spinning prize wheel", "a big wheel turns for the top prize"),
            FloorExample("Take-or-spin offer", "take the cash, or spin for a higher offer"),
            FloorExample("Rolling ball bonus", "a ball drops into multiplier pockets"),
        ),
    ),
    ;

    companion object {
        /** The type a game's mechanic belongs to, for any game that does not name one itself. */
        fun forKind(kind: GameKind): GameType = when (kind) {
            GameKind.LOCK_RESPIN -> LOCK_AND_RESPIN
            GameKind.GALE -> BUILD_AND_BONUS
            GameKind.FREE_SPINS -> FREE_SPINS
            GameKind.JAR -> POT_BONUS
            GameKind.TUMBLE -> TUMBLING_WAYS
            GameKind.WAYS -> WAYS_243
            GameKind.CLUSTER -> CLUSTER_PAYS
            GameKind.WHEEL -> PRIZE_WHEEL
        }
    }
}
