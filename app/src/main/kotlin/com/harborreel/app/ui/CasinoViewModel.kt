package com.harborreel.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.harborreel.engine.Casino
import com.harborreel.engine.Payout
import com.harborreel.engine.Rng
import com.harborreel.engine.SaveCodec
import com.harborreel.engine.SpinAttempt
import com.harborreel.engine.SpinOutcome

class CasinoViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("harbor_reel", Application.MODE_PRIVATE)
    private val casino = Casino(
        rng = Rng(),
        initial = SaveCodec.decode(prefs.getString(KEY, null)),
    )

    var snapshot by mutableStateOf(casino.state)
        private set

    /** Cash shown on the cabinet. Stays put until the spin animation finishes. */
    var bankroll by mutableStateOf(casino.state.credits)
        private set

    var last by mutableStateOf<SpinOutcome?>(null)
        private set

    var notice by mutableStateOf<String?>(null)
        private set

    private var presentedSpinId = -1L

    fun isPresented(spinId: Long): Boolean = presentedSpinId == spinId

    fun markPresented(spinId: Long) {
        presentedSpinId = spinId
    }

    fun spin(gameId: String) {
        when (val attempt = casino.spin(gameId)) {
            SpinAttempt.Insufficient -> notice = "Not enough cash for this spin."
            is SpinAttempt.Ok -> {
                notice = null
                last = attempt.outcome
            }
        }
        publish()
    }

    fun nudgeBet(direction: Int) {
        casino.nudgeBet(direction)
        publish()
    }

    fun nudgeDenom(direction: Int) {
        casino.nudgeDenom(direction)
        publish()
    }

    fun setLine(lineId: String) {
        casino.setLine(lineId)
        publish()
    }

    fun setPayout(payout: Payout) {
        casino.setPayout(payout)
        publish()
    }

    fun addCredits(amount: Long) {
        casino.addCredits(amount)
        notice = null
        publish()
        bankroll = casino.state.credits
    }

    fun setCash(cents: Long) {
        casino.setCash(cents)
        notice = null
        publish()
        bankroll = casino.state.credits
    }

    fun clearCash() {
        casino.clearCash()
        notice = null
        publish()
        bankroll = casino.state.credits
    }

    fun clearHistory() {
        casino.clearHistory()
        notice = null
        publish()
    }

    fun revealBankroll() {
        bankroll = casino.state.credits
    }

    fun resetPoints() {
        casino.resetPoints()
        publish()
    }

    fun resetLinePoints(lineId: String) {
        casino.resetLinePoints(lineId)
        publish()
    }

    private fun publish() {
        snapshot = casino.state
        prefs.edit().putString(KEY, SaveCodec.encode(casino.state)).apply()
    }

    companion object {
        private const val KEY = "save"
    }
}
