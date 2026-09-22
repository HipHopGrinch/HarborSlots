package com.harborreel.engine

private fun pay(three: Int, four: Int, five: Int): Map<Int, Int> = mapOf(3 to three, 4 to four, 5 to five)

private fun strip(vararg parts: Pair<Symbol, Int>): List<Symbol> =
    parts.flatMap { (symbol, count) -> List(count) { symbol } }

private fun List<Symbol>.rotate(by: Int): List<Symbol> {
    if (isEmpty()) return this
    val shift = by % size
    return drop(shift) + take(shift)
}

private fun harborReel(extraBuoy: Int, extraWild: Int): List<Symbol> = strip(
    Symbol.BUOY to (4 + extraBuoy),
    Symbol.ANCHOR to (1 + extraWild),
    Symbol.COMPASS to 2,
    Symbol.WHEEL to 3,
    Symbol.ROPE to 4,
    Symbol.BELL to 4,
    Symbol.LANTERN to 5,
    Symbol.ACE to 6,
    Symbol.KING to 6,
    Symbol.QUEEN to 7,
    Symbol.JACK to 7,
)

private fun brightworkReel(extraCloud: Int): List<Symbol> = strip(
    Symbol.CLOUD to (2 + extraCloud),
    Symbol.SQUALL to 2,
    Symbol.KEEL to 3,
    Symbol.PLANK to 3,
    Symbol.CANVAS to 2,
    Symbol.GULL to 3,
    Symbol.LAMP to 4,
    Symbol.ACE to 6,
    Symbol.KING to 6,
    Symbol.QUEEN to 6,
    Symbol.JACK to 6,
    Symbol.TEN to 6,
)

private fun marketReel(stalls: Int): List<Symbol> = strip(
    Symbol.STALL to stalls,
    Symbol.CHEF to 2,
    Symbol.NOODLE to 3,
    Symbol.DUMPLING to 3,
    Symbol.TEA to 4,
    Symbol.FAN to 4,
    Symbol.ACE to 6,
    Symbol.KING to 6,
    Symbol.QUEEN to 6,
    Symbol.JACK to 6,
    Symbol.TEN to 6,
    Symbol.NINE to 6,
)

private fun patchReel(baskets: Int, coins: Int): List<Symbol> = strip(
    Symbol.BASKET to baskets,
    Symbol.COIN to coins,
    Symbol.SCARECROW to 2,
    Symbol.APPLE to 4,
    Symbol.CLOVER to 4,
    Symbol.BOOT to 5,
    Symbol.ACE to 6,
    Symbol.KING to 6,
    Symbol.QUEEN to 7,
    Symbol.JACK to 7,
)

private val cardPays = mapOf(
    Symbol.ACE to pay(14, 30, 80),
    Symbol.KING to pay(14, 30, 80),
    Symbol.QUEEN to pay(10, 22, 64),
    Symbol.JACK to pay(10, 22, 64),
    Symbol.TEN to pay(8, 18, 48),
    Symbol.NINE to pay(8, 18, 48),
)

object Catalog {
    val harbor = GameDef(
        id = "harbor",
        name = "Harbor Hold",
        blurb = "Credit buoys stick in place. Three respins, and every new buoy restores them.",
        featureLabel = "Lock & Respin",
        rules = """
            Five reels, three rows, 20 lines. Wins pay left to right. Anchor is wild.

            Buoys show a credit prize and do not make line wins. Five or more buoys start Lock & Respin. Those buoys stay. You have three respins. Each new buoy restores three respins. When the respins end, every buoy prize is paid. Filling all 15 spots adds a Fleet prize.

            Cove, Bay, and Harbor are rare buoy names with larger prizes. The weight of each prize and the reel strips are the tables in this app.
        """.trimIndent(),
        kind = GameKind.LOCK_RESPIN,
        reels = listOf(
            harborReel(0, 0),
            harborReel(1, 0).rotate(5),
            harborReel(0, 1).rotate(9),
            harborReel(1, 0).rotate(13),
            harborReel(0, 0).rotate(3),
        ),
        paylines = PAYLINES,
        pays = cardPays + mapOf(
            Symbol.ANCHOR to pay(80, 200, 600),
            Symbol.COMPASS to pay(60, 160, 400),
            Symbol.WHEEL to pay(40, 110, 280),
            Symbol.ROPE to pay(28, 70, 180),
            Symbol.BELL to pay(20, 50, 140),
            Symbol.LANTERN to pay(16, 40, 110),
        ),
        wild = Symbol.ANCHOR,
        blockers = setOf(Symbol.BUOY),
    )

    val brightwork = GameDef(
        id = "brightwork",
        name = "Brightwork",
        blurb = "Keel, plank, and canvas build a skiff. Clouds call a squall that pays from how far you got.",
        featureLabel = "Build & Bonus",
        rules = """
            Five reels, three rows, 20 lines. Squall is wild.

            Keel, plank, and canvas pay on lines and also add to the skiff meter (1, 2, and 3). Stages are Keel line, Ribs, Hull, Sail, and Brightwork. Each stage raises the Squall Prize multiplier.

            Three or more Clouds pay a scatter prize and call a Squall Prize using the stage you have reached, including materials from that spin. The meter then resets.

            The skiff is saved on this device until the next Squall Prize.
        """.trimIndent(),
        kind = GameKind.GALE,
        reels = listOf(
            brightworkReel(0),
            brightworkReel(1).rotate(4),
            brightworkReel(0).rotate(8),
            brightworkReel(1).rotate(12),
            brightworkReel(0).rotate(2),
        ),
        paylines = PAYLINES,
        pays = cardPays + mapOf(
            Symbol.SQUALL to pay(64, 160, 480),
            Symbol.KEEL to pay(32, 80, 240),
            Symbol.PLANK to pay(26, 64, 190),
            Symbol.CANVAS to pay(26, 64, 190),
            Symbol.GULL to pay(16, 40, 120),
            Symbol.LAMP to pay(12, 32, 90),
        ),
        wild = Symbol.SQUALL,
        blockers = setOf(Symbol.CLOUD),
        scatter = Symbol.CLOUD,
        scatterPays = mapOf(3 to 4, 4 to 20, 5 to 80),
    )

    val market = GameDef(
        id = "market",
        name = "Night Market",
        blurb = "Stall symbols open the night service. One reel becomes all chefs on every free spin.",
        featureLabel = "Free Spins",
        rules = """
            Five reels, three rows, 20 lines. Chef is wild.

            Stall is a scatter. Three stalls award 8 free spins, four award 12, and five award 20. Stalls also pay a scatter prize.

            On each free spin, one reel turns into Chef wilds. A stall that was already on that reel stays, so it can still help a retrigger. Three or more stalls during free spins add another set of spins. Free spins stop at 80 total.

            The triggering bet is the bet for every free spin.
        """.trimIndent(),
        kind = GameKind.FREE_SPINS,
        reels = listOf(
            marketReel(1),
            marketReel(2).rotate(6),
            marketReel(2).rotate(10),
            marketReel(1).rotate(14),
            marketReel(2).rotate(3),
        ),
        paylines = PAYLINES,
        pays = cardPays + mapOf(
            Symbol.CHEF to pay(48, 130, 400),
            Symbol.NOODLE to pay(32, 80, 250),
            Symbol.DUMPLING to pay(26, 64, 200),
            Symbol.TEA to pay(18, 48, 140),
            Symbol.FAN to pay(14, 32, 96),
        ),
        wild = Symbol.CHEF,
        blockers = setOf(Symbol.STALL),
        scatter = Symbol.STALL,
        scatterPays = mapOf(3 to 4, 4 to 15, 5 to 60),
    )

    val patch = GameDef(
        id = "patch",
        name = "Penny Patch",
        blurb = "Coins fall into a jar. Baskets tip the jar and multiply whatever collected.",
        featureLabel = "Pot Bonus",
        rules = """
            Five reels, three rows, 20 lines. Scarecrow is wild.

            Coins do not pay on the spin they land. Their prize is added to the jar, which is saved on this device.

            Basket is a scatter. Three or more baskets pay a small scatter prize and tip the jar. The tip multiplies the jar by 1, 2, 3, or 5, then the jar empties. An empty jar pays a small consolation tip.

            The multiplier weights are in this app's prize table.
        """.trimIndent(),
        kind = GameKind.JAR,
        reels = listOf(
            patchReel(1, 2),
            patchReel(1, 2).rotate(5),
            patchReel(2, 2).rotate(9),
            patchReel(1, 2).rotate(7),
            patchReel(1, 2).rotate(2),
        ),
        paylines = PAYLINES,
        pays = cardPays + mapOf(
            Symbol.SCARECROW to pay(48, 120, 340),
            Symbol.APPLE to pay(20, 48, 150),
            Symbol.CLOVER to pay(16, 40, 120),
            Symbol.BOOT to pay(12, 32, 90),
        ),
        wild = Symbol.SCARECROW,
        blockers = setOf(Symbol.COIN, Symbol.BASKET),
        scatter = Symbol.BASKET,
        scatterPays = mapOf(3 to 4, 4 to 16, 5 to 50),
    )

    val games: List<GameDef> = listOf(harbor, brightwork, market, patch)

    fun byId(id: String): GameDef = games.first { it.id == id }
}
