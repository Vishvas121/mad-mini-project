package com.example.gamefiedsarvya.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.gamefiedsarvya.R

/**
 * Background music + SFX manager.
 *
 * Available files in res/raw:
 *   sfx_correct.mp3   (58 KB)  -- used as menu BGM loop
 *   sfx_attack.mp3    (61 KB)  -- used as explore/battle BGM loop
 *   sfx_game_over.mp3 (27 KB)  -- SFX
 *   sfx_level_up.mp3  (103 KB) -- SFX
 *
 * Ticking fix: isLooping=false + manual seekTo(0)+start() on completion.
 * MP3 LAME encoder adds silence padding at start/end; isLooping=true plays
 * that silence on every loop → audible tick. Manual restart skips it.
 */
object AudioManager {

    private var bgmPlayer:       MediaPlayer? = null
    private var currentTrackRes: Int          = -1
    private var appContext:      Context?      = null

    var musicEnabled: Boolean = true
    var sfxEnabled:   Boolean = true
    var musicVolume:  Float   = 0.45f
    var sfxVolume:    Float   = 0.8f

    private var isInitialised = false

    // ── Init ──────────────────────────────────────────────────────────────────

    fun init(context: Context) {
        if (isInitialised) return
        isInitialised = true
        appContext = context.applicationContext
    }

    // ── BGM — seamless loop, no ticking ──────────────────────────────────────

    fun playMusic(context: Context, resId: Int) {
        if (!musicEnabled) return
        if (currentTrackRes == resId && bgmPlayer?.isPlaying == true) return

        stopMusic()
        currentTrackRes = resId

        try {
            val afd = context.resources.openRawResourceFd(resId) ?: return
            bgmPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                isLooping = false   // manual restart avoids LAME gap click
                setVolume(musicVolume, musicVolume)

                setOnCompletionListener { mp ->
                    try { mp.seekTo(0); if (musicEnabled) mp.start() }
                    catch (_: Exception) {}
                }
                setOnErrorListener { _, _, _ -> stopMusic(); false }
                setOnPreparedListener { mp -> if (musicEnabled) mp.start() }
                prepareAsync()
            }
        } catch (_: Exception) {}
    }

    fun stopMusic() {
        try {
            bgmPlayer?.apply {
                setOnCompletionListener(null)
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        bgmPlayer = null
        currentTrackRes = -1
    }

    fun pauseMusic() {
        try { if (bgmPlayer?.isPlaying == true) bgmPlayer?.pause() } catch (_: Exception) {}
    }

    fun resumeMusic() {
        if (!musicEnabled) return
        try { if (bgmPlayer?.isPlaying == false) bgmPlayer?.start() } catch (_: Exception) {}
    }

    fun applyMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) pauseMusic() else resumeMusic()
    }

    fun applyMusicVolume(vol: Float) {
        musicVolume = vol.coerceIn(0f, 1f)
        try { bgmPlayer?.setVolume(musicVolume, musicVolume) } catch (_: Exception) {}
    }

    // ── SFX — one-shot MediaPlayer ────────────────────────────────────────────

    fun playCorrect()  { playSfxOneShot(R.raw.sfx_correct) }
    fun playLevelUp()  { playSfxOneShot(R.raw.sfx_level_up) }
    fun playAttack()   { playSfxOneShot(R.raw.sfx_attack) }
    fun playGameOver() { playSfxOneShot(R.raw.sfx_game_over) }

    private fun playSfxOneShot(resId: Int) {
        if (!sfxEnabled) return
        val ctx = appContext ?: return
        try {
            MediaPlayer.create(ctx, resId)?.apply {
                setVolume(sfxVolume, sfxVolume)
                setOnCompletionListener { release() }
                start()
            }
        } catch (_: Exception) {}
    }

    fun release() {
        stopMusic()
        isInitialised = false
    }

    // ── Track constants ───────────────────────────────────────────────────────
    // bgm_menu and bgm_explore were removed — using remaining SFX files as BGM

    object Tracks {
        val MENU    get() = R.raw.sfx_correct   // looped as menu background
        val EXPLORE get() = R.raw.sfx_attack    // looped as explore/battle background
    }
}
