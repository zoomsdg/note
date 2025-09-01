package com.dailynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
import com.dailynotes.utils.AudioPlayerManager
import kotlinx.coroutines.delay
import java.io.File

data class ContentBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ContentBlockType,
    val content: String = "",
    val textFieldValue: TextFieldValue = TextFieldValue(content),
    val mediaItem: MediaItem? = null,
    val position: Int = 0, // 在内容中的位置索引
    val timestamp: Long = System.currentTimeMillis()
)

enum class ContentBlockType {
    TEXT, IMAGE, AUDIO
}

// 富文本内容的统一表示
data class RichContent(
    val segments: List<ContentSegment> = emptyList(),
    val currentCursorPosition: Int = 0
)

sealed class ContentSegment {
    abstract val id: String
    abstract val position: Int
    
    data class TextSegment(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val position: Int,
        val text: String
    ) : ContentSegment()
    
    data class MediaSegment(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val position: Int,
        val mediaItem: MediaItem,
        val type: ContentBlockType
    ) : ContentSegment()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditor(
    contentBlocks: List<ContentBlock>,
    onContentChange: (List<ContentBlock>) -> Unit,
    onRequestFocus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    
    // 将内容块转换为统一的RichContent结构
    val richContent = remember(contentBlocks) {
        buildRichContentFromBlocks(contentBlocks)
    }
    
    // 构建用于显示的AnnotatedString和内联内容
    val (annotatedText, inlineContentMap) = remember(richContent) {
        buildDisplayContent(richContent) { blockId ->
            // 删除回调
            val updatedBlocks = contentBlocks.filter { it.id != blockId }
            onContentChange(updatedBlocks)
        }
    }
    
    // 管理文本编辑状态
    val textFieldValue = remember { 
        mutableStateOf(TextFieldValue(
            text = extractPlainText(richContent),
            selection = TextRange(extractPlainText(richContent).length)
        ))
    }
    
    // 当内容块变化时更新文本框
    LaunchedEffect(richContent) {
        val plainText = extractPlainText(richContent)
        if (textFieldValue.value.text != plainText) {
            textFieldValue.value = TextFieldValue(
                text = plainText,
                selection = TextRange(plainText.length)
            )
        }
    }
    
    // 自动聚焦
    LaunchedEffect(Unit) {
        delay(200)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // 忽略错误
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize() // 占满整个屏幕
            .padding(8.dp)
    ) {
        // 主编辑区域：混合显示文本和媒体的完整文本框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            // 改进的分离式编辑实现
            if (inlineContentMap.isNotEmpty()) {
                // 混合内容模式：富文本显示 + 点击编辑
                Column(modifier = Modifier.fillMaxSize()) {
                    // 富文本展示区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                try {
                                    focusRequester.requestFocus()
                                } catch (e: Exception) {
                                    // 忽略焦点请求错误
                                }
                            }
                    ) {
                        BasicText(
                            text = annotatedText,
                            inlineContent = inlineContentMap,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // 分离的文本编辑区域
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        BasicTextField(
                            value = textFieldValue.value,
                            onValueChange = { newValue ->
                                textFieldValue.value = newValue
                                updateContentFromText(newValue.text, contentBlocks, onContentChange)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .padding(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            ),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (textFieldValue.value.text.isEmpty()) {
                                        Text(
                                            text = "点击编辑文本内容...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            } else {
                // 纯文本编辑模式
                BasicTextField(
                    value = textFieldValue.value,
                    onValueChange = { newValue ->
                        textFieldValue.value = newValue
                        updateContentFromText(newValue.text, contentBlocks, onContentChange)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (textFieldValue.value.text.isEmpty()) {
                                Text(
                                    text = "开始输入记事内容...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun InlineAudioPlayer(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播放按钮
            IconButton(
                onClick = { 
                    audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                        isPlaying = playing
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                if (isPlaying) {
                    Text(
                        text = "⏸",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // 时长
            Text(
                text = if (mediaItem.duration > 0) {
                    val minutes = mediaItem.duration / 1000 / 60
                    val seconds = (mediaItem.duration / 1000) % 60
                    "%02d:%02d".format(minutes, seconds)
                } else "00:00",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // 波形线
            Text(
                text = "⎯⎯⎯⎯⎯⎯⎯⎯",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun InlineImageViewer(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            AsyncImage(
                model = File(mediaItem.path),
                contentDescription = "图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { showPreview = true },
                contentScale = ContentScale.Crop
            )
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
            ) {
                Card(
                    modifier = Modifier.size(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 图片预览
    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AsyncImage(
                        model = File(mediaItem.path),
                        contentDescription = "图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .clickable { showPreview = false },
                        contentScale = ContentScale.Fit
                    )
                    
                    TextButton(
                        onClick = { showPreview = false },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

// 辅助函数：将ContentBlock列表转换为RichContent
private fun buildRichContentFromBlocks(blocks: List<ContentBlock>): RichContent {
    val segments = mutableListOf<ContentSegment>()
    var currentPosition = 0
    
    // 按时间戳排序后构建段落
    blocks.sortedBy { it.timestamp }.forEach { block ->
        when (block.type) {
            ContentBlockType.TEXT -> {
                if (block.content.isNotEmpty()) {
                    segments.add(
                        ContentSegment.TextSegment(
                            id = block.id,
                            position = currentPosition,
                            text = block.content
                        )
                    )
                    currentPosition += block.content.length + 1 // +1 for newline
                }
            }
            ContentBlockType.IMAGE, ContentBlockType.AUDIO -> {
                block.mediaItem?.let { media ->
                    segments.add(
                        ContentSegment.MediaSegment(
                            id = block.id,
                            position = currentPosition,
                            mediaItem = media,
                            type = block.type
                        )
                    )
                    currentPosition += 1 // Media placeholder takes 1 position
                }
            }
        }
    }
    
    return RichContent(segments = segments)
}

// 辅助函数：从RichContent提取纯文本
private fun extractPlainText(richContent: RichContent): String {
    return richContent.segments
        .filterIsInstance<ContentSegment.TextSegment>()
        .joinToString("\n") { it.text }
}

// 辅助函数：构建显示用的AnnotatedString和内联内容
private fun buildDisplayContent(
    richContent: RichContent,
    onDeleteMedia: (String) -> Unit
): Pair<androidx.compose.ui.text.AnnotatedString, Map<String, InlineTextContent>> {
    val annotatedString = buildAnnotatedString {
        var hasContent = false
        
        richContent.segments.forEach { segment ->
            when (segment) {
                is ContentSegment.TextSegment -> {
                    if (segment.text.isNotEmpty()) {
                        if (hasContent) append("\n")
                        append(segment.text)
                        hasContent = true
                    }
                }
                is ContentSegment.MediaSegment -> {
                    if (hasContent) append("\n")
                    val placeholder = when (segment.type) {
                        ContentBlockType.AUDIO -> "\uD83C\uDFB5"
                        ContentBlockType.IMAGE -> "\uD83D\uDDBC\uFE0F"
                        else -> ""
                    }
                    appendInlineContent("${segment.type.name.lowercase()}_${segment.id}", placeholder)
                    hasContent = true
                }
            }
        }
    }
    
    // 构建内联内容映射
    val inlineContentMap = richContent.segments
        .filterIsInstance<ContentSegment.MediaSegment>()
        .associate { segment ->
            val key = "${segment.type.name.lowercase()}_${segment.id}"
            
            key to InlineTextContent(
                Placeholder(
                    width = 350.sp,
                    height = when (segment.type) {
                        ContentBlockType.AUDIO -> 70.sp
                        ContentBlockType.IMAGE -> 200.sp
                        else -> 40.sp
                    },
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Top
                )
            ) {
                when (segment.type) {
                    ContentBlockType.AUDIO -> {
                        EnhancedInlineAudioPlayer(
                            mediaItem = segment.mediaItem,
                            onDelete = { onDeleteMedia(segment.id) }
                        )
                    }
                    ContentBlockType.IMAGE -> {
                        EnhancedInlineImageViewer(
                            mediaItem = segment.mediaItem,
                            onDelete = { onDeleteMedia(segment.id) }
                        )
                    }
                    else -> {}
                }
            }
        }
    
    return annotatedString to inlineContentMap
}

// 辅助函数：从文本更新内容块
private fun updateContentFromText(
    newText: String,
    currentBlocks: List<ContentBlock>,
    onContentChange: (List<ContentBlock>) -> Unit
) {
    val mediaBlocks = currentBlocks.filter { it.type != ContentBlockType.TEXT }
    val updatedBlocks = mediaBlocks.toMutableList()
    
    if (newText.isNotEmpty()) {
        val textBlock = ContentBlock(
            id = currentBlocks.firstOrNull { it.type == ContentBlockType.TEXT }?.id 
                ?: java.util.UUID.randomUUID().toString(),
            type = ContentBlockType.TEXT,
            content = newText,
            textFieldValue = TextFieldValue(newText),
            timestamp = currentBlocks.firstOrNull { it.type == ContentBlockType.TEXT }?.timestamp 
                ?: System.currentTimeMillis()
        )
        updatedBlocks.add(0, textBlock)
    }
    
    onContentChange(updatedBlocks)
}

// 增强版音频播放器组件
@Composable
private fun EnhancedInlineAudioPlayer(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableStateOf(0f) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 顶部控制栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放按钮
                Card(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { 
                            audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                                isPlaying = playing
                                playProgress = 0f
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 时长和文件信息
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (mediaItem.duration > 0) {
                            val minutes = mediaItem.duration / 1000 / 60
                            val seconds = (mediaItem.duration / 1000) % 60
                            "%02d:%02d".format(minutes, seconds)
                        } else "未知时长",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "音频文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 删除按钮
                Card(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onDelete() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // 播放进度条和波形
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 简化的波形显示
                repeat(20) { index ->
                    val height = (10..25).random().dp
                    val alpha = if (playProgress > index / 20f) 1f else 0.4f
                    
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(height)
                            .padding(horizontal = 0.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                            )
                    )
                }
            }
        }
    }
}

// 增强版图片查看器组件  
@Composable
private fun EnhancedInlineImageViewer(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // 主图片显示
            AsyncImage(
                model = File(mediaItem.path),
                contentDescription = "图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp)
                    .clickable { showPreview = true },
                contentScale = ContentScale.Crop
            )
            
            // 顶部工具栏
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(bottomStart = 12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 预览按钮
                Card(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { showPreview = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👁",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // 删除按钮
                Card(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDelete() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // 底部信息条
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "点击查看大图",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }
        }
    }
    
    // 全屏预览对话框
    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    // 预览图片
                    AsyncImage(
                        model = File(mediaItem.path),
                        contentDescription = "图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                            .clickable { showPreview = false },
                        contentScale = ContentScale.Fit
                    )
                    
                    // 底部按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { showPreview = false }
                        ) {
                            Text("关闭")
                        }
                        
                        TextButton(
                            onClick = { 
                                showPreview = false
                                onDelete()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}