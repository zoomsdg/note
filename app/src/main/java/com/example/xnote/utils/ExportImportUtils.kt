package com.example.xnote.utils

import android.content.Context
import android.os.Environment
import com.example.xnote.data.FullNote
import com.example.xnote.data.NoteBlock
import com.example.xnote.data.BlockType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 记事导出导入工具类
 */
class ExportImportUtils(private val context: Context) {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * 导出记事为加密ZIP文件
     */
    fun exportNotes(
        notes: List<FullNote>,
        password: String,
        onProgress: (String) -> Unit = {},
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            onProgress("准备导出数据...")
            
            // 创建临时目录
            val tempDir = File(context.cacheDir, "export_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            // 创建媒体文件目录
            val mediaDir = File(tempDir, "media")
            mediaDir.mkdirs()
            
            onProgress("处理记事数据...")
            
            // 导出记事数据
            val exportData = mutableListOf<ExportNote>()
            
            for (note in notes) {
                val exportBlocks = mutableListOf<ExportBlock>()
                
                for (block in note.blocks) {
                    val exportBlock = when (block.type) {
                        BlockType.TEXT -> {
                            ExportBlock(
                                type = "text",
                                order = block.order,
                                text = block.text
                            )
                        }
                        BlockType.IMAGE -> {
                            // 复制图片文件
                            val originalFile = File(block.url ?: "")
                            if (originalFile.exists()) {
                                val fileName = "image_${block.id}_${originalFile.name}"
                                val targetFile = File(mediaDir, fileName)
                                originalFile.copyTo(targetFile, overwrite = true)
                                
                                ExportBlock(
                                    type = "image",
                                    order = block.order,
                                    mediaFileName = fileName,
                                    alt = block.alt,
                                    width = block.width,
                                    height = block.height
                                )
                            } else {
                                ExportBlock(type = "text", order = block.order, text = "[图片文件丢失]")
                            }
                        }
                        BlockType.AUDIO -> {
                            // 复制音频文件
                            val originalFile = File(block.url ?: "")
                            if (originalFile.exists()) {
                                val fileName = "audio_${block.id}_${originalFile.name}"
                                val targetFile = File(mediaDir, fileName)
                                originalFile.copyTo(targetFile, overwrite = true)
                                
                                ExportBlock(
                                    type = "audio",
                                    order = block.order,
                                    mediaFileName = fileName,
                                    duration = block.duration
                                )
                            } else {
                                ExportBlock(type = "text", order = block.order, text = "[音频文件丢失]")
                            }
                        }
                    }
                    exportBlocks.add(exportBlock)
                }
                
                exportData.add(
                    ExportNote(
                        id = note.note.id,
                        title = note.note.title,
                        createdAt = note.note.createdAt,
                        updatedAt = note.note.updatedAt,
                        version = note.note.version,
                        blocks = exportBlocks
                    )
                )
            }
            
            // 写入JSON数据文件
            val dataFile = File(tempDir, "notes_data.json")
            FileWriter(dataFile).use { writer ->
                gson.toJson(exportData, writer)
            }
            
            onProgress("创建加密ZIP文件...")
            
            // 创建输出ZIP文件到下载目录
            val timestamp = dateFormat.format(Date())
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            
            // 确保下载目录存在
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val outputFile = File(downloadsDir, "XNote_Export_$timestamp.zip")
            
            // 创建加密ZIP
            val zipFile = ZipFile(outputFile)
            val zipParameters = ZipParameters().apply {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
            }
            
            zipFile.setPassword(password.toCharArray())
            
            // 添加数据文件
            zipFile.addFile(dataFile, zipParameters)
            
            // 添加媒体文件目录（如果有文件）
            if (mediaDir.listFiles()?.isNotEmpty() == true) {
                zipFile.addFolder(mediaDir, zipParameters)
            }
            
            // 清理临时文件
            tempDir.deleteRecursively()
            
            onProgress("导出完成！")
            onSuccess(outputFile)
            
        } catch (e: Exception) {
            onError("导出失败：${e.message}")
        }
    }
    
    /**
     * 从加密ZIP文件导入记事
     */
    fun importNotes(
        zipFile: File,
        password: String,
        onProgress: (String) -> Unit = {},
        onSuccess: (List<ImportNote>) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            onProgress("验证ZIP文件...")
            
            val zip = ZipFile(zipFile)
            if (!zip.isValidZipFile) {
                onError("不是有效的ZIP文件")
                return
            }
            
            zip.setPassword(password.toCharArray())
            
            onProgress("解密文件...")
            
            // 创建临时解压目录
            val tempDir = File(context.cacheDir, "import_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            try {
                zip.extractAll(tempDir.absolutePath)
            } catch (e: Exception) {
                tempDir.deleteRecursively()
                onError("解密失败，请检查密码是否正确")
                return
            }
            
            onProgress("读取记事数据...")
            
            // 读取数据文件
            val dataFile = File(tempDir, "notes_data.json")
            if (!dataFile.exists()) {
                tempDir.deleteRecursively()
                onError("ZIP文件格式不正确，缺少数据文件")
                return
            }
            
            val exportData: List<ExportNote> = try {
                val json = dataFile.readText()
                val type = object : TypeToken<List<ExportNote>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                tempDir.deleteRecursively()
                onError("数据文件格式错误：${e.message}")
                return
            }
            
            onProgress("处理媒体文件...")
            
            val mediaDir = File(tempDir, "media")
            val importNotes = mutableListOf<ImportNote>()
            
            for (exportNote in exportData) {
                val importBlocks = mutableListOf<ImportBlock>()
                
                for (exportBlock in exportNote.blocks) {
                    val importBlock = when (exportBlock.type) {
                        "text" -> ImportBlock(
                            type = BlockType.TEXT,
                            order = exportBlock.order,
                            text = exportBlock.text
                        )
                        "image" -> {
                            val mediaFile = if (exportBlock.mediaFileName != null) {
                                File(mediaDir, exportBlock.mediaFileName)
                            } else null
                            
                            ImportBlock(
                                type = BlockType.IMAGE,
                                order = exportBlock.order,
                                mediaFile = mediaFile,
                                alt = exportBlock.alt,
                                width = exportBlock.width,
                                height = exportBlock.height
                            )
                        }
                        "audio" -> {
                            val mediaFile = if (exportBlock.mediaFileName != null) {
                                File(mediaDir, exportBlock.mediaFileName)
                            } else null
                            
                            ImportBlock(
                                type = BlockType.AUDIO,
                                order = exportBlock.order,
                                mediaFile = mediaFile,
                                duration = exportBlock.duration
                            )
                        }
                        else -> ImportBlock(
                            type = BlockType.TEXT,
                            order = exportBlock.order,
                            text = "[未知类型的内容]"
                        )
                    }
                    importBlocks.add(importBlock)
                }
                
                importNotes.add(
                    ImportNote(
                        id = exportNote.id,
                        title = exportNote.title,
                        createdAt = exportNote.createdAt,
                        updatedAt = exportNote.updatedAt,
                        version = exportNote.version,
                        blocks = importBlocks,
                        tempDir = tempDir
                    )
                )
            }
            
            onProgress("导入完成！")
            onSuccess(importNotes)
            
        } catch (e: Exception) {
            onError("导入失败：${e.message}")
        }
    }
    
    // 导出数据结构
    data class ExportNote(
        val id: String,
        val title: String,
        val createdAt: Long,
        val updatedAt: Long,
        val version: Int,
        val blocks: List<ExportBlock>
    )
    
    data class ExportBlock(
        val type: String,
        val order: Int,
        val text: String? = null,
        val mediaFileName: String? = null,
        val alt: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        val duration: Long? = null
    )
    
    // 导入数据结构
    data class ImportNote(
        val id: String,
        val title: String,
        val createdAt: Long,
        val updatedAt: Long,
        val version: Int,
        val blocks: List<ImportBlock>,
        val tempDir: File
    )
    
    data class ImportBlock(
        val type: BlockType,
        val order: Int,
        val text: String? = null,
        val mediaFile: File? = null,
        val alt: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        val duration: Long? = null
    )
}