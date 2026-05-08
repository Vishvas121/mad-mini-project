package com.example.gamefiedsarvya.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.gamefiedsarvya.R

/**
 * Audio manager — BGM only.
 *
 * SFX disabled: the available files (sfx_correct, sfx_attack, etc.)
 * are music tracks, not short clips. Playing them as SFX causes
 * overlapping audio and the "beeping" the user reported.
 * SFX can be re-enabled when proper short WAV/OGG clips are provided.
 *
 * BGM uses isLooping=false + manual restart to avoid the LAME gap click.
 */
object AudioManager {

    private var bgmPlayer:       MediaPlayer? = null
    private var currentTrackRes: Int          = -1
    private var appContext:      Context?      = null

    var musicEnabled: Boolean = true
    var sfxEnabled:   Boolean = false   // disabled until proper SFX files added
    var musicVolume:  Float   = 0.45f
    var sfxVolume:    Float   = 0.8f

    private var isInitialised = false

    fun init(context: Context) {
        if (isInitialised) return
        isInitialised = true
        appContext = context.applicationContext
    }

    // ── BGM ───────────────────────────────────────────────────────────────────

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
                isLooping = false
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

    // ── SFX — no-op until proper short clips are provided ────────────────────

    fun playCorrect()  { /* SFX disabled */ }
    fun playLevelUp()  { /* SFX disabled */ }
    fun playAttack()   { /* SFX disabled */ }
    fun playGameOver() { /* SFX disabled */ }

    fun release() {
        stopMusic()
        isInitialised = false
    }

    object Tracks {
        val MENU    get() = R.raw.sfx_correct
        val EXPLORE get() = R.raw.sfx_attack
    }
}
