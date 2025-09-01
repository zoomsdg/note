package com.dailynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.dailynotes.data.ContentBlock
import com.dailynotes.data.BlockType
import com.dailynotes.utils.AudioPlayerManager
import java.io.File

@Composable
fun ContinuousTextEditor(
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit,
    onAddBlock: (BlockType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var showAddMenu by remember { mutableStateOf(false) }
    var addMenuPosition by remember { mutableStateOf(0) }
    
    // 将所有文本块合并为连续文本，并记录媒体块位置
    val (combinedText, mediaBlocks) = remember(blocks) {
        buildCombinedTextWithMediaPlaceholders(blocks)
    }
    
    var textFieldValue by remember(combinedText) { 
        mutableStateOf(TextFieldValue(combinedText))
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 主要的连续文本编辑区
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        // 解析文本变化并更新块结构
                        updateBlocksFromText(newValue.text, blocks, onBlocksChange)
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
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = "开始输入...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // 渲染混合内容（文本 + 内嵌媒体）
                            MixedContentRenderer(
                                text = textFieldValue.text,
                                mediaBlocks = mediaBlocks,
                                onMediaClick = { mediaBlock ->
                                    // 处理媒体点击，光标移动到媒体后
                                },
                                onDeleteMedia = { mediaBlock ->
                                    deleteMediaBlock(mediaBlock, blocks, onBlocksChange)
                                }
                            )
                            
                            // 原始文本输入（透明）
                            Box(modifier = Modifier.fillMaxSize()) {
                                innerTextField()
                            }
                        }
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
                
                // 添加内容按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { 
                        addMenuPosition = getCurrentCursorPosition(textFieldValue)
                        showAddMenu = true 
                    }) {
                        Icon(Icons.Default.Add, "添加内容")
                    }
                    
                    IconButton(onClick = { onAddBlock(BlockType.IMAGE, addMenuPosition) }) {
                        Icon(Icons.Default.Add, "添加图片")
                    }
                    
                    IconButton(onClick = { onAddBlock(BlockType.AUDIO, addMenuPosition) }) {
                        Icon(Icons.Default.PlayArrow, "添加音频")
                    }
                }
            }
        }
    }
    
    // 添加内容菜单
    if (showAddMenu) {
        AlertDialog(
            onDismissRequest = { showAddMenu = false },
            title = { Text("添加内容") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onAddBlock(BlockType.IMAGE, addMenuPosition)
                            showAddMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("图片")
                    }
                    
                    TextButton(
                        onClick = {
                            onAddBlock(BlockType.AUDIO, addMenuPosition)
                            showAddMenu = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("音频")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMenu = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MixedContentRenderer(
    text: String,
    mediaBlocks: Map<Int, ContentBlock>,
    onMediaClick: (ContentBlock) -> Unit,
    onDeleteMedia: (ContentBlock) -> Unit
) {
    Column {
        // 按行渲染文本，遇到媒体占位符时插入媒体组件
        val lines = text.split('\n')
        lines.forEachIndexed { lineIndex, line ->
            when {
                line.startsWith("[图片") -> {
                    // 查找对应的图片块并渲染
                    mediaBlocks[lineIndex]?.let { mediaBlock ->
                        if (mediaBlock is ContentBlock.ImageBlock) {
                            InlineImageBlock(
                                block = mediaBlock,
                                onDelete = { onDeleteMedia(mediaBlock) },
                                onClick = { onMediaClick(mediaBlock) }
                            )
                        }
                    }
                }
                line.startsWith("[音频") -> {
                    // 查找对应的音频块并渲染
                    mediaBlocks[lineIndex]?.let { mediaBlock ->
                        if (mediaBlock is ContentBlock.AudioBlock) {
                            InlineAudioBlock(
                                block = mediaBlock,
                                onDelete = { onDeleteMedia(mediaBlock) },
                                onClick = { onMediaClick(mediaBlock) }
                            )
                        }
                    }
                }
                line.isNotBlank() -> {
                    // 渲染普通文本（但这里不应该渲染，因为BasicTextField已经处理了文本）
                    // 这个函数主要用于媒体块的位置占用
                }
            }
        }
    }
}

@Composable
private fun InlineImageBlock(
    block: ContentBlock.ImageBlock,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        AsyncImage(
            model = if (block.localPath.isNotEmpty()) File(block.localPath) else block.url,
            contentDescription = block.alt.ifEmpty { "图片" },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 300.dp)
                .clickable { onClick() },
            contentScale = ContentScale.Fit
        )
        
        // 删除按钮
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    MaterialTheme.colorScheme.errorContainer,
                    RoundedCornerShape(16.dp)
                )
                .size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                "删除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InlineAudioBlock(
    block: ContentBlock.AudioBlock,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 播放按钮
            IconButton(
                onClick = {
                    audioPlayerManager.playAudio(
                        block.localPath.ifEmpty { block.url }
                    ) { playing, _ ->
                        isPlaying = playing
                    }
                },
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(24.dp)
                    )
                    .size(48.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                    "播放/暂停",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // 音频信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "音频文件",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatDuration(block.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// 帮助函数
private fun buildCombinedTextWithMediaPlaceholders(blocks: List<ContentBlock>): Pair<String, Map<Int, ContentBlock>> {
    val textBuilder = StringBuilder()
    val mediaBlocksMap = mutableMapOf<Int, ContentBlock>()
    var lineIndex = 0
    
    blocks.forEach { block ->
        when (block) {
            is ContentBlock.TextBlock -> {
                if (block.text.isNotEmpty()) {
                    textBuilder.append(block.text)
                    if (textBuilder.isNotEmpty() && !textBuilder.endsWith('\n')) {
                        textBuilder.append('\n')
                    }
                    lineIndex++
                }
            }
            is ContentBlock.ImageBlock -> {
                textBuilder.append("[图片: ${block.alt.ifEmpty { "图片" }}]\n")
                mediaBlocksMap[lineIndex] = block
                lineIndex++
            }
            is ContentBlock.AudioBlock -> {
                textBuilder.append("[音频: ${formatDuration(block.duration)}]\n")
                mediaBlocksMap[lineIndex] = block
                lineIndex++
            }
        }
    }
    
    return Pair(textBuilder.toString().trimEnd('\n'), mediaBlocksMap)
}

private fun updateBlocksFromText(
    text: String, 
    originalBlocks: List<ContentBlock>, 
    onBlocksChange: (List<ContentBlock>) -> Unit
) {
    val lines = text.split('\n')
    val newBlocks = mutableListOf<ContentBlock>()
    var mediaIndex = 0
    
    lines.forEachIndexed { index, line ->
        when {
            line.startsWith("[图片") -> {
                // 保持原有的媒体块
                val originalMediaBlocks = originalBlocks.filterIsInstance<ContentBlock.ImageBlock>()
                if (mediaIndex < originalMediaBlocks.size) {
                    newBlocks.add(originalMediaBlocks[mediaIndex].copy(order = index))
                }
                mediaIndex++
            }
            line.startsWith("[音频") -> {
                // 保持原有的媒体块
                val originalMediaBlocks = originalBlocks.filterIsInstance<ContentBlock.AudioBlock>()
                if (mediaIndex < originalMediaBlocks.size) {
                    newBlocks.add(originalMediaBlocks[mediaIndex])
                }
            }
            line.isNotBlank() -> {
                newBlocks.add(ContentBlock.TextBlock(order = index, text = line))
            }
        }
    }
    
    onBlocksChange(newBlocks)
}

private fun deleteMediaBlock(
    mediaBlock: ContentBlock,
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit
) {
    val updatedBlocks = blocks.filter { it.id != mediaBlock.id }
    onBlocksChange(updatedBlocks)
}

private fun getCurrentCursorPosition(textFieldValue: TextFieldValue): Int {
    val textBeforeCursor = textFieldValue.text.substring(0, textFieldValue.selection.start)
    return textBeforeCursor.count { it == '\n' }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}