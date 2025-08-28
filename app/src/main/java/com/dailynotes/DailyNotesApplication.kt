package com.dailynotes

import android.app.Application
import com.dailynotes.utils.AudioPlayerManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailyNotesApplication : Application() {
    
    override fun onTerminate() {
        super.onTerminate()
        // 释放音频播放器资源
        AudioPlayerManager.getInstance(this).release()
    }
}