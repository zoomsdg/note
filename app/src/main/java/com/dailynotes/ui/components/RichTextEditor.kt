package com.dailynotes.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
    val mediaItem: MediaItem? = null
)

enum class ContentBlockType {
    TEXT, IMAGE, AUDIO
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RichTextEditor(
    contentBlocks: List<ContentBlock>,
    onContentChange: (List<ContentBlock>) -> Unit,
    onRequestFocus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val fallbackFocusRequester = remember { FocusRequester() }
    
    // 监听内容变化并自动滚动到底部
    LaunchedEffect(contentBlocks.size, contentBlocks.lastOrNull()?.content) {
        if (contentBlocks.isNotEmpty()) {
            delay(100) // 等待UI更新
            // 滚动到最后一个元素（包括fallback text field）
            val lastIndex = if (contentBlocks.isEmpty() || 
                             contentBlocks.last().type != ContentBlockType.TEXT || 
                             contentBlocks.last().content.isNotEmpty()) {
                contentBlocks.size // fallback text field 的位置
            } else {
                contentBlocks.size - 1 // 最后一个真实内容块
            }
            listState.animateScrollToItem(maxOf(0, lastIndex))
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                // 点击内容框任意位置时，强制聚焦并显示键盘
                try {
                    fallbackFocusRequester.requestFocus()
                    keyboardController?.show()
                } catch (e: Exception) {
                    // 如果焦点请求失败，直接尝试显示键盘
                    try {
                        keyboardController?.show()
                    } catch (e2: Exception) {
                        // 忽略所有错误，确保不会崩溃
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            items = contentBlocks,
            key = { it.id }
        ) { block ->
            when (block.type) {
                ContentBlockType.TEXT -> {
                    val focusRequester = remember(block.id) { FocusRequester() }
                    
                    // 处理外部焦点请求
                    LaunchedEffect(onRequestFocus) {
                        // 这里我们依赖ViewModel通过其他方式来触发焦点
                    }
                    
                    // 文本块 - 无边框，更自然的编辑体验
                    BasicTextField(
                        value = block.textFieldValue,
                        onValueChange = { newValue ->
                            val updatedBlocks = contentBlocks.map { existingBlock ->
                                if (existingBlock.id == block.id) {
                                    existingBlock.copy(
                                        content = newValue.text,
                                        textFieldValue = newValue
                                    )
                                } else {
                                    existingBlock
                                }
                            }
                            onContentChange(updatedBlocks)
                            
                            // 确保键盘保持显示
                            keyboardController?.show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.None
                        ),
                        keyboardActions = KeyboardActions(),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (block.content.isEmpty()) {
                                    Text(
                                        text = if (contentBlocks.indexOf(block) == 0) "开始输入记事内容..." else "继续输入...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
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
                ContentBlockType.IMAGE -> {
                    block.mediaItem?.let { media ->
                        InlineImageDisplay(
                            mediaItem = media,
                            onDelete = {
                                val updatedBlocks = contentBlocks.filter { it.id != block.id }
                                onContentChange(updatedBlocks)
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                ContentBlockType.AUDIO -> {
                    block.mediaItem?.let { media ->
                        InlineAudioDisplay(
                            mediaItem = media,
                            onDelete = {
                                val updatedBlocks = contentBlocks.filter { it.id != block.id }
                                onContentChange(updatedBlocks)
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // 始终有一个fallback输入框，用于处理点击聚焦
        item {
            BasicTextField(
                value = TextFieldValue(""),
                onValueChange = { newValue ->
                    if (newValue.text.isNotEmpty()) {
                        val newBlock = ContentBlock(
                            type = ContentBlockType.TEXT,
                            content = newValue.text,
                            textFieldValue = newValue
                        )
                        onContentChange(contentBlocks + newBlock)
                    }
                    
                    // 确保键盘保持显示
                    keyboardController?.show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 4.dp)
                    .focusRequester(fallbackFocusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.None
                ),
                keyboardActions = KeyboardActions(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (contentBlocks.isNotEmpty()) {
                            Text(
                                text = "继续输入...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "开始输入记事内容...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
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
    
    // 当内容为空时，自动聚焦到第一个输入框
    LaunchedEffect(contentBlocks.isEmpty()) {
        if (contentBlocks.isEmpty()) {
            delay(300) // 给UI时间渲染
            try {
                fallbackFocusRequester.requestFocus()
                // 延迟显示键盘确保焦点已设置
                delay(100)
                keyboardController?.show()
            } catch (e: Exception) {
                // 忽略初始焦点设置错误
            }
        }
    }
}

@Composable
private fun InlineImageDisplay(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = File(mediaItem.path),
            contentDescription = "图片",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showPreview = true },
            contentScale = ContentScale.Crop
        )
        
        // 删除按钮 - 更美观的设计
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(32.dp)
        ) {
            Card(
                modifier = Modifier.size(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(16.dp)
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
    }
    
    // 图片预览对话框
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

@Composable
private fun InlineAudioDisplay(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播放按钮和信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // 播放按钮
                Card(
                    modifier = Modifier.size(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    IconButton(
                        onClick = { 
                            audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                                isPlaying = playing
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isPlaying) {
                            Text(
                                text = "⏸",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "播放",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 音频信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎵 音频文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (mediaItem.duration > 0) {
                        val minutes = mediaItem.duration / 1000 / 60
                        val seconds = (mediaItem.duration / 1000) % 60
                        Text(
                            text = if (isPlaying) "播放中... %d:%02d".format(minutes, seconds) else "时长: %d:%02d".format(minutes, seconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = if (isPlaying) "播放中..." else "点击播放",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Card(
                    modifier = Modifier.size(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(14.dp)
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
        }
    }
}

