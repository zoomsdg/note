package com.dailynotes.utils

import android.content.Context
import android.net.Uri
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
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

    suspend fun exportNotesToUserLocation(
        context: Context, 
        uri: Uri, 
        selectedNoteIds: List<Long>? = null
    ): ExportResult {
        return try {
            val allNotes = repository.getAllNotes().first()
            val notes = if (selectedNoteIds != null) {
                allNotes.filter { it.id in selectedNoteIds }
            } else {
                allNotes
            }
            
            if (notes.isEmpty()) {
                return ExportResult.Error("没有选择要导出的记事")
            }
            
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
            
            val message = if (selectedNoteIds != null) {
                "导出成功，共导出 ${notes.size} 条记事"
            } else {
                "导出成功"
            }
            ExportResult.Success(message)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }
    
    suspend fun exportNotesToUserLocationWithPassword(
        context: Context, 
        uri: Uri, 
        password: String,
        selectedNoteIds: List<Long>? = null
    ): ExportResult {
        return try {
            val allNotes = repository.getAllNotes().first()
            val notes = if (selectedNoteIds != null) {
                allNotes.filter { it.id in selectedNoteIds }
            } else {
                allNotes
            }
            
            if (notes.isEmpty()) {
                return ExportResult.Error("没有选择要导出的记事")
            }
            
            // 创建临时文件用于ZIP加密
            val tempFile = File(context.cacheDir, "temp_export_${System.currentTimeMillis()}.zip")
            
            try {
                // 使用zip4j创建加密的ZIP文件
                val zipFile = ZipFile(tempFile)
                zipFile.setPassword(password.toCharArray())
                
                // 创建加密参数
                val zipParameters = ZipParameters().apply {
                    isEncryptFiles = true
                    encryptionMethod = EncryptionMethod.AES
                }
                
                // 先创建临时的notes.json文件
                val notesJson = gson.toJson(notes)
                val notesJsonFile = File(context.cacheDir, "notes_temp.json")
                notesJsonFile.writeText(notesJson)
                
                // 添加notes.json到加密ZIP
                zipFile.addFile(notesJsonFile, zipParameters.apply { fileNameInZip = "notes.json" })
                
                // 添加媒体文件到加密ZIP
                notes.forEach { note ->
                    note.mediaItems.forEach { mediaItem ->
                        val mediaFile = File(mediaItem.path)
                        if (mediaFile.exists()) {
                            val mediaParams = ZipParameters().apply {
                                isEncryptFiles = true
                                encryptionMethod = EncryptionMethod.AES
                                fileNameInZip = "media/${mediaFile.name}"
                            }
                            zipFile.addFile(mediaFile, mediaParams)
                        }
                    }
                }
                
                // 将加密的ZIP文件复制到用户选择的位置
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    tempFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                // 清理临时文件
                notesJsonFile.delete()
                
                val message = if (selectedNoteIds != null) {
                    "加密导出成功，共导出 ${notes.size} 条记事"
                } else {
                    "加密导出成功"
                }
                ExportResult.Success(message)
            } finally {
                // 确保清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
            
        } catch (e: Exception) {
            ExportResult.Error("加密导出失败: ${e.message}")
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
    
    suspend fun importNotes(
        context: Context, 
        zipUri: Uri, 
        replaceExisting: Boolean = false
    ): ImportResult {
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
                        
                        if (replaceExisting) {
                            // 清空现有数据
                            repository.getAllNotes().first().forEach { note ->
                                repository.deleteNote(note)
                            }
                        }
                        
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
                        
                        val message = if (replaceExisting) {
                            "替换导入成功，共导入 ${updatedNotes.size} 条记事"
                        } else {
                            "追加导入成功，共导入 ${updatedNotes.size} 条记事"
                        }
                        ImportResult.Success(updatedNotes.size, message)
                    } else {
                        ImportResult.Error("ZIP文件中未找到笔记数据")
                    }
                }
            } ?: ImportResult.Error("无法读取ZIP文件")
            
        } catch (e: Exception) {
            ImportResult.Error("导入失败: ${e.message}")
        }
    }
    
    suspend fun importNotesWithPassword(
        context: Context, 
        zipUri: Uri, 
        password: String,
        replaceExisting: Boolean = false
    ): ImportResult {
        return try {
            val tempDir = File(context.cacheDir, "import_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()
            
            val mediaPathMapping = mutableMapOf<String, String>()
            
            // 创建临时ZIP文件
            val tempZipFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.zip")
            
            try {
                // 复制加密的ZIP文件到临时位置
                context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                    tempZipFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                // 使用zip4j解密ZIP文件
                val zipFile = ZipFile(tempZipFile)
                if (zipFile.isEncrypted) {
                    zipFile.setPassword(password.toCharArray())
                }
                
                var notesJson: String? = null
                
                // 解压缩文件
                val extractDir = File(tempDir, "extracted")
                zipFile.extractAll(extractDir.absolutePath)
                
                // 读取notes.json
                val notesJsonFile = File(extractDir, "notes.json")
                if (notesJsonFile.exists()) {
                    notesJson = notesJsonFile.readText()
                }
                
                // 处理媒体文件
                val mediaDir = File(extractDir, "media")
                if (mediaDir.exists() && mediaDir.isDirectory) {
                    mediaDir.listFiles()?.forEach { mediaFile ->
                        val originalFileName = mediaFile.name
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
                        
                        mediaFile.copyTo(newMediaFile, overwrite = true)
                        mediaPathMapping["media/$originalFileName"] = newMediaFile.absolutePath
                    }
                }
                
                if (notesJson != null) {
                    val importedNotes = gson.fromJson(notesJson, Array<NoteEntity>::class.java).toList()
                    
                    if (replaceExisting) {
                        // 清空现有数据
                        repository.getAllNotes().first().forEach { note ->
                            repository.deleteNote(note)
                        }
                    }
                    
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
                    
                    val message = if (replaceExisting) {
                        "加密替换导入成功，共导入 ${updatedNotes.size} 条记事"
                    } else {
                        "加密追加导入成功，共导入 ${updatedNotes.size} 条记事"
                    }
                    ImportResult.Success(updatedNotes.size, message)
                } else {
                    ImportResult.Error("加密ZIP文件中未找到笔记数据")
                }
                
            } finally {
                // 清理临时文件
                if (tempZipFile.exists()) {
                    tempZipFile.delete()
                }
            }
            
        } catch (e: Exception) {
            when {
                e.message?.contains("Wrong password") == true -> {
                    ImportResult.Error("密码错误，请检查密码是否正确")
                }
                e.message?.contains("password") == true -> {
                    ImportResult.Error("密码验证失败")
                }
                else -> {
                    ImportResult.Error("加密导入失败: ${e.message}")
                }
            }
        }
    }
    
    fun validatePassword(password: String): Boolean {
        if (password.length < 8) return false
        
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        
        for (char in password) {
            when {
                char.isUpperCase() -> hasUpper = true
                char.isLowerCase() -> hasLower = true
                char.isDigit() -> hasDigit = true
            }
        }
        
        return hasUpper && hasLower && hasDigit
    }
    
    fun getPasswordRequirements(): String {
        return "密码要求：\n• 至少8个字符\n• 包含大写字母\n• 包含小写字母\n• 包含数字"
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
    data class Success(val importedCount: Int, val message: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}