package com.dailynotes.utils

import android.content.Context
import android.net.Uri
import com.dailynotes.data.NoteEntity
import com.dailynotes.data.NoteRepository
import com.dailynotes.data.security.DatabaseSecurity
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
import java.security.MessageDigest
import java.util.*
import java.util.regex.Pattern
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
    
    // 安全常量
    private val MAX_FILE_SIZE = 100 * 1024 * 1024L // 100MB
    private val MAX_ENTRIES = 10000
    private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
    private val ALLOWED_AUDIO_EXTENSIONS = setOf("3gp", "mp3", "wav", "m4a", "aac")
    private val DANGEROUS_PATH_PATTERN = Pattern.compile("(../|\\\\\\.\\.\\\\|/\\.\\./|\\.\\./|\\\\\\.\\.)")
    
    private fun sanitizeFileName(fileName: String): String {
        if (fileName.contains("..") || DANGEROUS_PATH_PATTERN.matcher(fileName).find()) {
            throw SecurityException("危险的文件路径: $fileName")
        }
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
    
    private fun validateFileExtension(fileName: String, allowedExtensions: Set<String>): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return allowedExtensions.contains(extension)
    }
    
    private fun validateFileSize(size: Long): Boolean {
        return size > 0 && size <= MAX_FILE_SIZE
    }
    
    private fun createSecureTempFile(context: Context, prefix: String, suffix: String): File {
        val tempDir = File(context.cacheDir, "secure_temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
            // 设置临时目录权限，仅应用自身可访问
            tempDir.setReadable(false, false)
            tempDir.setWritable(false, false)
            tempDir.setExecutable(false, false)
            tempDir.setReadable(true, true)
            tempDir.setWritable(true, true)
            tempDir.setExecutable(true, true)
        }
        return File.createTempFile(prefix, suffix, tempDir)
    }
    
    private fun secureDeleteFile(file: File) {
        if (file.exists()) {
            try {
                // 多次覆写文件内容以防止数据恢复
                if (file.isFile) {
                    val fileSize = file.length()
                    file.writeBytes(ByteArray(fileSize.toInt()) { 0 })
                    file.writeBytes(ByteArray(fileSize.toInt()) { 0xFF.toByte() })
                    file.writeBytes(ByteArray(fileSize.toInt()) { (Math.random() * 256).toInt().toByte() })
                }
            } catch (e: Exception) {
                // 覆写失败时至少删除文件
            } finally {
                file.delete()
            }
        }
    }
    
    private fun cleanupTempDirectory(context: Context) {
        val tempDir = File(context.cacheDir, "secure_temp")
        if (tempDir.exists()) {
            tempDir.listFiles()?.forEach { file ->
                secureDeleteFile(file)
            }
        }
    }
    
    // 添加数据清理功能
    suspend fun cleanupOrphanedMediaFiles(context: Context): Int {
        val notes = repository.getAllNotes().first()
        val referencedMediaPaths = mutableSetOf<String>()
        
        // 收集所有被引用的媒体文件路径
        notes.forEach { note ->
            note.mediaItems.forEach { mediaItem ->
                referencedMediaPaths.add(mediaItem.path)
            }
        }
        
        var deletedCount = 0
        
        // 清理图片目录
        val imageDir = File(context.filesDir, "images")
        if (imageDir.exists()) {
            imageDir.listFiles()?.forEach { file ->
                if (!referencedMediaPaths.contains(file.absolutePath)) {
                    secureDeleteFile(file)
                    deletedCount++
                }
            }
        }
        
        // 清理音频目录
        val audioDir = File(context.filesDir, "audio")
        if (audioDir.exists()) {
            audioDir.listFiles()?.forEach { file ->
                if (!referencedMediaPaths.contains(file.absolutePath)) {
                    secureDeleteFile(file)
                    deletedCount++
                }
            }
        }
        
        // 清理缓存和临时文件
        cleanupTempDirectory(context)
        
        return deletedCount
    }
    
    suspend fun clearAllAppData(context: Context): Boolean {
        return try {
            // 清除所有笔记数据
            repository.deleteAllNotes()
            
            // 安全删除所有媒体文件
            val imageDir = File(context.filesDir, "images")
            val audioDir = File(context.filesDir, "audio")
            
            imageDir.listFiles()?.forEach { secureDeleteFile(it) }
            audioDir.listFiles()?.forEach { secureDeleteFile(it) }
            
            // 清理临时文件
            cleanupTempDirectory(context)
            
            // 清理数据库安全信息（会导致数据库重新生成新密钥）
            DatabaseSecurity.clearDatabaseSecurity(context)
            
            true
        } catch (e: Exception) {
            false
        }
    }

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
            
            // 创建安全临时文件用于ZIP加密
            val tempFile = createSecureTempFile(context, "export_", ".zip")
            
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
                // 安全清理临时文件
                secureDeleteFile(tempFile)
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
                    
                    var entryCount = 0
                    while (zip.nextEntry.also { entry = it } != null) {
                        val currentEntry = entry ?: continue
                        
                        // 防止ZIP炸弹攻击
                        if (++entryCount > MAX_ENTRIES) {
                            throw SecurityException("ZIP文件条目过多，可能存在安全风险")
                        }
                        
                        // 验证文件路径安全性
                        sanitizeFileName(currentEntry.name)
                        
                        when {
                            currentEntry.name == "notes.json" -> {
                                if (!validateFileSize(currentEntry.size)) {
                                    throw SecurityException("notes.json文件过大")
                                }
                                
                                val buffer = ByteArray(currentEntry.size.toInt().coerceAtMost(1024 * 1024)) // 最大1MB
                                val bytesRead = zip.read(buffer)
                                notesJson = String(buffer, 0, bytesRead, Charsets.UTF_8)
                            }
                            currentEntry.name.startsWith("media/") && !currentEntry.isDirectory -> {
                                val originalFileName = sanitizeFileName(File(currentEntry.name).name)
                                
                                // 验证文件大小和类型
                                if (!validateFileSize(currentEntry.size)) {
                                    throw SecurityException("媒体文件过大: $originalFileName")
                                }
                                
                                val newMediaFile = when {
                                    validateFileExtension(originalFileName, ALLOWED_IMAGE_EXTENSIONS) -> {
                                        MediaUtils.createImageFile(context, "imported_${System.currentTimeMillis()}")
                                    }
                                    validateFileExtension(originalFileName, ALLOWED_AUDIO_EXTENSIONS) -> {
                                        MediaUtils.createAudioFile(context, "imported_${System.currentTimeMillis()}")
                                    }
                                    else -> {
                                        throw SecurityException("不支持的媒体文件类型: $originalFileName")
                                    }
                                }
                                
                                // 安全地复制文件内容
                                var copiedBytes = 0L
                                newMediaFile.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var bytesRead: Int
                                    while (zip.read(buffer).also { bytesRead = it } != -1) {
                                        copiedBytes += bytesRead
                                        if (copiedBytes > MAX_FILE_SIZE) {
                                            throw SecurityException("文件内容超出大小限制")
                                        }
                                        output.write(buffer, 0, bytesRead)
                                    }
                                }
                                
                                // 记录路径映射关系
                                mediaPathMapping[currentEntry.name] = newMediaFile.absolutePath
                            }
                            else -> {
                                // 忽略其他不认识的文件，增强安全性
                                continue
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
            
            // 创建安全临时ZIP文件
            val tempZipFile = createSecureTempFile(context, "import_", ".zip")
            
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
                // 安全清理临时文件
                secureDeleteFile(tempZipFile)
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
        if (password.length < 12) return false
        
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSpecial = false
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        for (char in password) {
            when {
                char.isUpperCase() -> hasUpper = true
                char.isLowerCase() -> hasLower = true
                char.isDigit() -> hasDigit = true
                specialChars.contains(char) -> hasSpecial = true
            }
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial
    }
    
    fun getPasswordRequirements(): String {
        return "密码要求：\n• 至少12个字符\n• 包含大写字母\n• 包含小写字母\n• 包含数字\n• 包含特殊字符 (!@#$%^&*等)"
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