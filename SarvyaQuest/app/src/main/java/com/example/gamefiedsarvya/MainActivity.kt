package com.example.gamefiedsarvya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.gamefiedsarvya.audio.AudioManager
import com.example.gamefiedsarvya.voice.VoiceManager
import com.example.gamefiedsarvya.ui.navigation.SarvyaNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        AudioManager.init(this)
        AudioManager.playMusic(this, AudioManager.Tracks.MENU)
        VoiceManager.init(this)
        // Theme is applied inside SarvyaNavGraph — no wrapper needed here
        setContent { SarvyaNavGraph() }
    }

    override fun onPause()   { super.onPause();   AudioManager.pauseMusic() }
    override fun onResume()  { super.onResume();  AudioManager.resumeMusic() }
    override fun onDestroy() {
        super.onDestroy()
        AudioManager.release()
        VoiceManager.release()
    }
}
