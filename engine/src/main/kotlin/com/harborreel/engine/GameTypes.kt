package com.harborreel.engine

/** A real cabinet seen on a cruise casino floor, kept as a reference for its game type. */
data class FloorExample(
    val name: String,
    val maker: String? = null,
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
        alsoCalled = "Hold & Win",
        description = "These machines rely on locking trigger symbols in place and resetting respins to chase jackpots or cash multipliers.",
        floorExamples = listOf(
            FloorExample(
                "Dragon Link",
                "Aristocrat",
                "One of the most prominent hold-and-spin series on board, featured across multiple banks in both non-smoking and smoking sections.",
            ),
            FloorExample(
                "Mystery of the Lamps",
                "Scientific Games",
                "A newer multi-machine bank added to the ship featuring vibrant lantern hold-and-win bonus triggers.",
            ),
            FloorExample(
                "Ascending Fortunes / Temple Falls",
                "Aristocrat",
                "Asian-themed mechanical reel games built around locking specific scatter or coin triggers into place.",
            ),
        ),
    ),
    BUILD_AND_BONUS(
        title = "Build and Bonus",
        alsoCalled = "Progression & Accumulation",
        description = "These slots feature progress meters, collection elements, or evolving features that build up over time toward a bonus payoff.",
        floorExamples = listOf(
            FloorExample(
                "Coin Trios (Fortune Trails / Piggy Coin Trios)",
                "Aristocrat",
                "Players collect colored coins into corresponding bowls on top of the reels to build and trigger distinct feature bonuses.",
            ),
            FloorExample(
                "Olympus Strike / Fan Nan Fu Fu",
                note = "Games that utilize cascading or collection meters to scale up multipliers and unlock tiered bonus features.",
            ),
            FloorExample(
                "Wonder Boost Gold",
                note = "Features mechanics where expanding wild progression and boosts accumulate as you play through sessions.",
            ),
        ),
    ),
    FREE_SPINS(
        title = "Free Spins",
        alsoCalled = null,
        description = "The staple feature found across traditional and modern video slots where landing scatter symbols awards a batch of complimentary bonus spins.",
        floorExamples = listOf(
            FloorExample(
                "Buffalo Gold",
                "Aristocrat",
                "The ultimate free spins powerhouse where collecting gold coin symbols permanently transforms lower-paying animal symbols into top-tier Buffalo symbols for the remainder of the free spins round.",
            ),
            FloorExample(
                "Huff 'N More Puff (Hard Hat Edition)",
                "Scientific Games",
                "Triggers free spins where building straw, stick, or brick houses ultimately leads to the massive \"Mansion\" feature spins.",
            ),
            FloorExample(
                "Dancing Drums Prosperity",
                "Scientific Games",
                "Awards free spins with a choice of grid-size expansions (up to 7,776 ways to win) alongside random jackpot picks.",
            ),
        ),
    ),
    POT_BONUS(
        title = "Pot Bonus",
        alsoCalled = null,
        description = "These games feature visual pots, chests, or lucky red envelopes on the screen that randomly or via symbols trigger a pick-em jackpot round.",
        floorExamples = listOf(
            FloorExample(
                "Buffalo Gold Wheels",
                "Aristocrat",
                "Combines the classic Buffalo free spins model with a prominent bonus wheel and jackpot pot triggers.",
            ),
            FloorExample(
                "Grand Buddha / Gong Gong Lu",
                note = "Asian-themed progressive games featuring prominent on-screen wealth pots that trigger multi-level pick bonuses.",
            ),
            FloorExample(
                "Golden Blessings (Boost Jackpots)",
                note = "Built around stacked pot symbols that randomly burst open to award fixed jackpots or enhanced bonuses.",
            ),
        ),
    ),
    TUMBLING_WAYS(
        title = "Tumbling Ways",
        alsoCalled = "Cascading / Avalanche Reels",
        description = "Games where winning symbols disappear and new ones cascade down from above to create potential chain-reaction wins on a single spin.",
        floorExamples = listOf(
            FloorExample(
                "Buffalo Gold Revolution",
                note = "Incorporates cascading/rolling mechanics alongside wheel features into the classic Buffalo engine.",
            ),
            FloorExample(
                "Firestorm / Line Link Series",
                note = "Utilize rolling reel mechanics where winning combinations trigger cascading sequences to extend the action.",
            ),
            FloorExample(
                "Raging Bulls / Custom Tournament Machines",
                note = "Featured in the slot tournament bank, many of these modern multi-line setups utilize cascading symbol drops.",
            ),
        ),
    ),
    WAYS_243(
        title = "243 Ways",
        alsoCalled = "Ways-to-Win",
        description = "Slots that discard standard paylines in favor of adjacent-reel matching combinations resulting in 243 ways to win.",
        floorExamples = listOf(
            FloorExample(
                "Legends Deluxe (featuring Five Dragons & Miss Kitty)",
                "Aristocrat",
                "Multi-game machines that include classic 243-ways heavy-hitters like Five Dragons.",
            ),
            FloorExample(
                "Buffalo Chief",
                note = "Expands on traditional ways mechanics by opening up extra reel rows during bonus features to exponentially increase winning paths.",
            ),
            FloorExample(
                "Triple Supreme",
                note = "Employs multi-way grid setups that award wins from left to right independent of strict linear paylines.",
            ),
        ),
    ),
    CLUSTER_PAYS(
        title = "Cluster Pays",
        alsoCalled = null,
        description = "Grid-based games that discard lines and ways entirely, paying out when clusters of matching symbols connect horizontally or vertically.",
        floorExamples = emptyList(),
        floorNote = "While heavy-hitting traditional video slots and mechanical reels dominate a cruise ship casino floor like the Icon of the Seas, traditional dedicated cluster-pays titles are less common on physical ship floors compared to high-volatility Hold & Spin and Buffalo variants.",
    ),
    PRIZE_WHEEL(
        title = "Prize Wheel",
        alsoCalled = null,
        description = "Games centered around spinning a mechanical or digital bonus wheel for high-value multipliers, credits, or progressive jackpots.",
        floorExamples = listOf(
            FloorExample(
                "Wheel of Fortune",
                "IGT",
                "The absolute king of cruise ship prize wheel slots, featured prominently with multiple physical wheels in the smoking section.",
            ),
            FloorExample(
                "Top Dollar",
                "IGT",
                "A mechanical classic where triggering the bonus awards an offer on a marquee mechanical sign, letting players accept cash or spin for higher offers.",
            ),
            FloorExample(
                "Pinball",
                "IGT",
                "A nostalgic mechanical-style hybrid slot that launches a physical pinball across a top-box playfield to land in multiplier pockets.",
            ),
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
