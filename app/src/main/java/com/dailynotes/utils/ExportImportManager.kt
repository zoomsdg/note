package com.dailynotes.utils

import android.content.Context
import android.net.Uri
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportImportManager @Inject constructor(
    private val repository: NoteRepository
) {
    
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setPrettyPrinting()
        .create()

    suspend fun exportNotesToUserLocation(context: Context, uri: Uri): ExportResult {
        return try {
            val notes = repository.getAllNotes().first()
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zip ->
                    // 导出笔记数据
                    val notesJson = gson.toJson(notes)
                    val notesEntry = ZipEntry("notes.json")
                    zip.putNextEntry(notesEntry)
                    zip.write(notesJson.toByteArray())
                    zip.closeEntry()
                    
                    // 导出媒体文件
                    notes.forEach { note ->
                        note.mediaItems.forEach { mediaItem ->
                            val mediaFile = File(mediaItem.path)
                            if (mediaFile.exists()) {
                                val entryName = "media/${mediaFile.name}"
                                val mediaEntry = ZipEntry(entryName)
                                zip.putNextEntry(mediaEntry)
                                
                                mediaFile.inputStream().use { input ->
                                    input.copyTo(zip)
                                }
                                zip.closeEntry()
                            }
                        }
                    }
                }
            }
            
            ExportResult.Success("导出成功")
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }
    
    suspend fun exportNotes(context: Context): ExportResult {
        return try {
            val notes = repository.getAllNotes().first()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "日记本备份_${dateFormat.format(Date())}.zip"
            
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val zipFile = File(exportDir, fileName)
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                // 导出笔记数据
                val notesJson = gson.toJson(notes)
                val notesEntry = ZipEntry("notes.json")
                zip.putNextEntry(notesEntry)
                zip.write(notesJson.toByteArray())
                zip.closeEntry()
                
                // 导出媒体文件
                notes.forEach { note ->
                    note.mediaItems.forEach { mediaItem ->
                        val mediaFile = File(mediaItem.path)
                        if (mediaFile.exists()) {
                            val entryName = "media/${mediaFile.name}"
                            val mediaEntry = ZipEntry(entryName)
                            zip.putNextEntry(mediaEntry)
                            
                            mediaFile.inputStream().use { input ->
                                input.copyTo(zip)
                            }
                            zip.closeEntry()
                        }
                    }
                }
            }
            
            ExportResult.Success(zipFile.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }
    
    suspend fun importNotes(context: Context, zipUri: Uri): ImportResult {
        return try {
            val tempDir = File(context.cacheDir, "import_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()
            
            val mediaPathMapping = mutableMapOf<String, String>()
            
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry: ZipEntry?
                    var notesJson: String? = null
                    
                    while (zip.nextEntry.also { entry = it } != null) {
                        val currentEntry = entry ?: continue
                        
                        when {
                            currentEntry.name == "notes.json" -> {
                                notesJson = zip.readBytes().toString(Charsets.UTF_8)
                            }
                            currentEntry.name.startsWith("media/") && !currentEntry.isDirectory -> {
                                val originalFileName = File(currentEntry.name).name
                                val newMediaFile = when {
                                    originalFileName.endsWith(".jpg", true) || 
                                    originalFileName.endsWith(".png", true) || 
                                    originalFileName.endsWith(".jpeg", true) -> {
                                        MediaUtils.createImageFile(context, "imported_${System.currentTimeMillis()}")
                                    }
                                    originalFileName.endsWith(".3gp", true) || 
                                    originalFileName.endsWith(".mp3", true) || 
                                    originalFileName.endsWith(".wav", true) -> {
                                        MediaUtils.createAudioFile(context, "imported_${System.currentTimeMillis()}")
                                    }
                                    else -> {
                                        File(tempDir, "imported_${System.currentTimeMillis()}_$originalFileName")
                                    }
                                }
                                
                                newMediaFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                
                                // 记录路径映射关系
                                mediaPathMapping[currentEntry.name] = newMediaFile.absolutePath
                            }
                        }
                        zip.closeEntry()
                    }
                    
                    if (notesJson != null) {
                        val importedNotes = gson.fromJson(notesJson, Array<NoteEntity>::class.java).toList()
                        
                        // 更新媒体文件路径
                        val updatedNotes = importedNotes.map { note ->
                            val updatedMediaItems = note.mediaItems.map { mediaItem ->
                                val oldPath = "media/${File(mediaItem.path).name}"
                                val newPath = mediaPathMapping[oldPath] ?: mediaItem.path
                                mediaItem.copy(path = newPath)
                            }
                            note.copy(id = 0, mediaItems = updatedMediaItems) // 重置ID以避免冲突
                        }
                        
                        // 插入导入的笔记
                        updatedNotes.forEach { note ->
                            repository.insertNote(note)
                        }
                        
                        ImportResult.Success(updatedNotes.size)
                    } else {
                        ImportResult.Error("ZIP文件中未找到笔记数据")
                    }
                }
            } ?: ImportResult.Error("无法读取ZIP文件")
            
        } catch (e: Exception) {
            ImportResult.Error("导入失败: ${e.message}")
        }
    }
    
    fun getExportFile(context: Context, fileName: String): File {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return File(exportDir, fileName)
    }
}

sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val importedCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}