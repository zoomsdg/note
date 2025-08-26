package com.dailynotes.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val type: MediaType,
    val path: String,
    val duration: Long = 0 // 音频时长，毫秒
) : Parcelable

enum class MediaType {
    IMAGE, AUDIO
}