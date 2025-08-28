package com.dailynotes.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class AudioPlayerManager private constructor(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    private var currentPlayingPath: String? = null
    private var playbackStateListener: ((Boolean, String?) -> Unit)? = null
    
    companion object {
        @Volatile
        private var INSTANCE: AudioPlayerManager? = null
        
        fun getInstance(context: Context): AudioPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioPlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun playAudio(audioPath: String, onStateChange: (Boolean, String?) -> Unit) {
        playbackStateListener = onStateChange
        
        if (currentPlayingPath == audioPath && exoPlayer?.isPlaying == true) {
            // 如果正在播放同一个文件，暂停
            pause()
            return
        }
        
        // 停止当前播放
        stop()
        
        try {
            val audioFile = File(audioPath)
            if (!audioFile.exists()) {
                onStateChange(false, null)
                return
            }
            
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(audioFile.toURI().toString())
                setMediaItem(mediaItem)
                prepare()
                
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        playbackStateListener?.invoke(isPlaying, if (isPlaying) audioPath else null)
                    }
                    
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                stop()
                                playbackStateListener?.invoke(false, null)
                            }
                            Player.STATE_READY -> {
                                if (playWhenReady) {
                                    currentPlayingPath = audioPath
                                }
                            }
                        }
                    }
                })
                
                playWhenReady = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onStateChange(false, null)
        }
    }
    
    fun pause() {
        exoPlayer?.pause()
        currentPlayingPath = null
    }
    
    fun stop() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        currentPlayingPath = null
    }
    
    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false
    
    fun getCurrentPlayingPath(): String? = if (isPlaying()) currentPlayingPath else null
    
    fun release() {
        stop()
        playbackStateListener = null
    }
}