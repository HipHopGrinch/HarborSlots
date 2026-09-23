package com.harborreel.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineTest {
    @Test
    fun lineOfAcesPaysEveryLine() {
        val game = Catalog.harbor.copy(
            reels = List(5) { List(8) { Symbol.ACE } },
            kind = GameKind.LOCK_RESPIN,
            blockers = emptySet(),
        )
        val start = PlayerState(credits = 1_000, denom = 1, betPerLine = 1)
        val (next, outcome) = resolveSpin(game, start, ZeroRng(), 1)
        val coin = 1L
        val expected = 20L * 80L * coin
        assertEquals(expected, outcome.totalWin)
        assertEquals(1_000 - 20 + expected, next.credits)
        assertEquals(0L, next.points.getValue("carnival"))
        assertEquals(20L, next.pointRemainder.getValue("carnival"))
    }

    @Test
    fun royalCaribbeanAwardsOnePointPerFiveDollars() {
        val start = PlayerState(credits = 100_000, denom = 25, betPerLine = 1, lineId = "royal")
        assertEquals(500, start.bet)
        val (next, _) = resolveSpin(Catalog.harbor, start, ZeroRng(), 1)
        assertEquals(1L, next.points.getValue("royal"))
        assertEquals(0L, next.pointRemainder.getValue("royal"))
    }

    @Test
    fun blockerEndsARun() {
        val symbols = listOf(Symbol.ACE, Symbol.ACE, Symbol.BUOY, Symbol.ACE, Symbol.ACE)
        val resolved = resolveRun(symbols, Symbol.ANCHOR, setOf(Symbol.BUOY))
        assertEquals(Symbol.ACE to 2, resolved)
    }

    @Test
    fun wildsFillFromTheLeft() {
        val symbols = listOf(Symbol.ANCHOR, Symbol.ANCHOR, Symbol.COMPASS, Symbol.COMPASS, Symbol.JACK)
        val resolved = resolveRun(symbols, Symbol.ANCHOR, setOf(Symbol.BUOY))
        assertEquals(Symbol.COMPASS to 4, resolved)
    }

    @Test
    fun insufficientCreditsLeaveTheBankAlone() {
        val casino = Casino(ZeroRng(), PlayerState(credits = 10, denom = 1, betPerLine = 1))
        assertTrue(casino.spin("harbor") is SpinAttempt.Insufficient)
        assertEquals(10, casino.state.credits)
        assertTrue(casino.state.history.isEmpty())
    }

    @Test
    fun saveRoundTrip() {
        val state = PlayerState(
            credits = 1_250,
            denom = 1,
            betPerLine = 2,
            lineId = "princess",
            payout = Payout.GENEROUS,
            points = CruiseLines.ids.associateWith { if (it == "princess") 80L else 0L },
            skiff = 17,
            jar = 36,
            history = listOf(HistoryEntry("harbor", 40, 90)),
        )
        val decoded = SaveCodec.decode(SaveCodec.encode(state))
        assertEquals(state.credits, decoded.credits)
        assertEquals(40, decoded.bet)
        assertEquals(2, decoded.betPerLine)
        assertEquals("princess", decoded.lineId)
        assertEquals(Payout.GENEROUS, decoded.payout)
        assertEquals(80L, decoded.points.getValue("princess"))
        assertEquals(17, decoded.skiff)
        assertEquals(36L, decoded.jar)
        assertEquals(state.history, decoded.history)
    }

    @Test
    fun sameSeedRepeats() {
        fun once(): Long {
            val casino = Casino(Rng(99), PlayerState(credits = 50_000, denom = 1, betPerLine = 1))
            return (casino.spin("harbor") as SpinAttempt.Ok).outcome.totalWin
        }
        assertEquals(once(), once())
    }

    @Test
    fun fiveAceReelsPay243Ways() {
        val game = Catalog.mesa.copy(
            reels = List(5) { List(6) { Symbol.ACE } },
            pays = mapOf(Symbol.ACE to mapOf(5 to 12)),
        )
        val (_, outcome) = resolveSpin(game, PlayerState(credits = 10_000, denom = 1, betPerLine = 1), ZeroRng(), 1)
        assertEquals(12L * 243L, outcome.totalWin)
        assertEquals(null, outcome.feature)
    }

    @Test
    fun everyGameTypeHasOneGame() {
        assertEquals(GameType.entries.toSet(), Catalog.games.map { it.type }.toSet())
        assertEquals(Catalog.games.size, Catalog.games.map { it.type }.distinct().size)
    }

    @Test
    fun gameTypesMatchTheirMechanics() {
        Catalog.games.forEach { game ->
            assertEquals(GameType.forKind(game.kind), game.type, game.id)
        }
    }

    @Test
    fun unnamedTypeFallsBackToTheMechanic() {
        val game = GameDef(
            id = "test",
            name = "Test",
            blurb = "",
            featureLabel = "",
            rules = "",
            kind = GameKind.CLUSTER,
            reels = emptyList(),
            paylines = emptyList(),
            pays = emptyMap(),
            wild = Symbol.PUFFER,
            blockers = emptySet(),
        )
        assertEquals(GameType.CLUSTER_PAYS, game.type)
    }

    @Test
    fun everyGameTypeDescribesTheFloor() {
        GameType.entries.forEach { type ->
            assertTrue(type.description.isNotBlank(), type.name)
            assertTrue(type.floorExamples.isNotEmpty() || type.floorNote != null, type.name)
            type.floorExamples.forEach { assertTrue(it.note.isNotBlank(), it.name) }
        }
    }

    @Test
    fun returnRatesStayInAPlayableBand() {
        val quiet = rates(Payout.QUIET)
        val cruise = rates(Payout.CRUISE)
        val generous = rates(Payout.GENEROUS)
        println("quiet $quiet")
        println("cruise $cruise")
        println("generous $generous")
        quiet.forEach { (id, rate) -> assertTrue(rate in 0.80..1.08, "quiet $id rtp=$rate") }
        cruise.forEach { (id, rate) -> assertTrue(rate in 0.80..1.40, "cruise $id rtp=$rate") }
        generous.forEach { (id, rate) -> assertTrue(rate in 0.80..2.40, "generous $id rtp=$rate") }
    }

    private fun rates(payout: Payout): Map<String, Double> =
        Catalog.games.associate { game -> game.id to returnRate(game.id, payout, spins = 12_000) }

    private fun returnRate(gameId: String, payout: Payout, spins: Int): Double {
        val casino = Casino(
            Rng(42),
            PlayerState(credits = 1_000_000_000L, denom = 1, betPerLine = 1, payout = payout),
        )
        var wagered = 0L
        var won = 0L
        repeat(spins) {
            val outcome = (casino.spin(gameId) as SpinAttempt.Ok).outcome
            wagered += outcome.bet
            won += outcome.totalWin
            assertTrue(casino.state.credits >= 0)
        }
        return won.toDouble() / wagered.toDouble()
    }
}
