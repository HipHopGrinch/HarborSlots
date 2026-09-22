package com.harborreel.engine

const val LOCK_FILL_PERCENT = 8
const val FLEET_BONUS_COINS = 700

val BUOY_PRIZES: List<Pair<NamedPrize, Int>> = listOf(
    NamedPrize(5, null) to 40,
    NamedPrize(8, null) to 30,
    NamedPrize(12, null) to 16,
    NamedPrize(20, null) to 8,
    NamedPrize(36, null) to 4,
    NamedPrize(60, "Cove") to 3,
    NamedPrize(150, "Bay") to 1,
    NamedPrize(400, "Harbor") to 1,
)

val POT_COINS: List<Pair<Int, Int>> = listOf(
    3 to 40,
    6 to 28,
    10 to 16,
    16 to 8,
    28 to 4,
)

val TIP_MULTIPLIERS: List<Pair<Int, Int>> = listOf(
    1 to 46,
    2 to 28,
    3 to 12,
    5 to 4,
)

val SQUALL_BASE: List<Pair<Int, Int>> = listOf(
    14 to 40,
    22 to 28,
    40 to 16,
    64 to 8,
    110 to 3,
)

fun skiffStage(progress: Int): SkiffStage = when {
    progress >= 80 -> SkiffStage("Brightwork", 8, null)
    progress >= 50 -> SkiffStage("Sail", 5, 80)
    progress >= 28 -> SkiffStage("Hull", 3, 50)
    progress >= 12 -> SkiffStage("Ribs", 2, 28)
    else -> SkiffStage("Keel line", 1, 12)
}

val PAYLINES: List<List<Int>> = listOf(
    listOf(1, 1, 1, 1, 1),
    listOf(0, 0, 0, 0, 0),
    listOf(2, 2, 2, 2, 2),
    listOf(0, 1, 2, 1, 0),
    listOf(2, 1, 0, 1, 2),
    listOf(0, 0, 1, 2, 2),
    listOf(2, 2, 1, 0, 0),
    listOf(1, 0, 0, 0, 1),
    listOf(1, 2, 2, 2, 1),
    listOf(0, 1, 1, 1, 0),
    listOf(2, 1, 1, 1, 2),
    listOf(1, 0, 1, 2, 1),
    listOf(1, 2, 1, 0, 1),
    listOf(0, 1, 0, 1, 0),
    listOf(2, 1, 2, 1, 2),
    listOf(1, 1, 0, 1, 1),
    listOf(1, 1, 2, 1, 1),
    listOf(0, 1, 2, 2, 2),
    listOf(2, 1, 0, 0, 0),
    listOf(0, 2, 1, 2, 0),
)
