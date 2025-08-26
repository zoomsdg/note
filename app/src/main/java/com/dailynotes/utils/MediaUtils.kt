package com.dailynotes.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object MediaUtils {
    
    fun getAudioDuration(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun createAudioFile(context: Context, fileName: String): File {
        val audioDir = File(context.filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return File(audioDir, "$fileName.3gp")
    }
    
    fun createImageFile(context: Context, fileName: String): File {
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return File(imageDir, "$fileName.jpg")
    }
    
    fun copyImageFromUri(context: Context, uri: Uri, targetFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun copyAudioFromUri(context: Context, uri: Uri, targetFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%d:%02d".format(minutes, remainingSeconds)
    }
    
    fun mediaItemsToJson(mediaItems: List<MediaItem>): String {
        return Gson().toJson(mediaItems)
    }
    
    fun mediaItemsFromJson(json: String): List<MediaItem> {
        return if (json.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<MediaItem>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        }
    }
}