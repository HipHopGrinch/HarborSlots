package com.harborreel.app.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.harborreel.app.R

enum class Sfx {
    SpinStart,
    ReelStop,
    WinSmall,
    WinBig,
    WheelSpin,
    WheelStop,
    Tap,
}

class HarborSfx(context: Context) {
    private val app = context.applicationContext
    private val audio = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var muted by mutableStateOf(prefs.getBoolean(MUTE_KEY, false))
        private set

    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids = mapOf(
        Sfx.SpinStart to pool.load(app, R.raw.sfx_spin_start, 1),
        Sfx.ReelStop to pool.load(app, R.raw.sfx_reel_stop, 1),
        Sfx.WinSmall to pool.load(app, R.raw.sfx_win_small, 1),
        Sfx.WinBig to pool.load(app, R.raw.sfx_win_big, 1),
        Sfx.WheelSpin to pool.load(app, R.raw.sfx_wheel_spin, 1),
        Sfx.WheelStop to pool.load(app, R.raw.sfx_wheel_stop, 1),
        Sfx.Tap to pool.load(app, R.raw.sfx_tap, 1),
    )

    fun setSoundMuted(value: Boolean) {
        muted = value
        prefs.edit().putBoolean(MUTE_KEY, value).apply()
    }

    fun play(sfx: Sfx) {
        if (muted) return
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        val id = ids[sfx] ?: return
        val vol = volume(sfx)
        pool.play(id, vol, vol, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }

    private fun volume(sfx: Sfx): Float = when (sfx) {
        Sfx.Tap -> 0.42f
        Sfx.ReelStop -> 0.52f
        Sfx.SpinStart, Sfx.WheelSpin -> 0.70f
        Sfx.WinSmall, Sfx.WheelStop -> 0.78f
        Sfx.WinBig -> 0.86f
    }

    companion object {
        private const val PREFS = "harbor_reel"
        private const val MUTE_KEY = "sfx_muted"
    }
}

val LocalHarborSfx = staticCompositionLocalOf<HarborSfx?> { null }
