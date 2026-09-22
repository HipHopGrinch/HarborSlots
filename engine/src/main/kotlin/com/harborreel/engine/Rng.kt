package com.harborreel.engine

import java.util.concurrent.atomic.AtomicLong

/**
 * Source of uniform integers for reel stops and bonus draws.
 *
 * Harbor Slots uses an original virtual-reel model:
 * 1. [Rng] (PCG-XSH-RR, 64-bit state) draws a uniform integer.
 * 2. The integer selects a stop on that reel's strip. Strips are longer than the
 *    window and repeat some symbols, so common symbols land more often.
 * 3. The window is that stop and the next two symbols, wrapping around the strip.
 * Bonus prizes use the same generator and the weight tables in the game catalog.
 */
interface RandomSource {
    fun nextInt(bound: Int): Int
}

class Rng(seed: Long = freshSeed()) : RandomSource {
    private var state: Long = seed xor MIX

    override fun nextInt(bound: Int): Int {
        require(bound > 0)
        val bits = nextBits().toLong() and 0xFFFFFFFFL
        return ((bits * bound.toLong()) ushr 32).toInt()
    }

    private fun nextBits(): Int {
        val old = state
        state = old * MULTIPLIER + INCREMENT
        val xorshifted = (((old ushr 18) xor old) ushr 27).toInt()
        val rot = (old ushr 59).toInt()
        return Integer.rotateRight(xorshifted, rot)
    }

    companion object {
        private const val MULTIPLIER = 6364136223846793005L
        private const val INCREMENT = -0x25C1C6346B46A423L
        private const val MIX = -0x7AC3B6198B701565L
        private val sequence = AtomicLong(1L)

        fun freshSeed(): Long =
            System.nanoTime() xor sequence.incrementAndGet() xor -0x61C8864680B583EBL
    }
}

class ZeroRng : RandomSource {
    override fun nextInt(bound: Int): Int {
        require(bound > 0)
        return 0
    }
}

fun <T> RandomSource.pick(table: List<Pair<T, Int>>): T {
    require(table.isNotEmpty())
    val total = table.sumOf { it.second }
    require(total > 0)
    var roll = nextInt(total)
    for ((item, weight) in table) {
        if (roll < weight) return item
        roll -= weight
    }
    return table.last().first
}
