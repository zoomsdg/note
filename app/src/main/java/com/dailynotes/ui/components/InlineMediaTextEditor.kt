package com.dailynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dailynotes.data.ContentBlock
import com.dailynotes.data.BlockType
import com.dailynotes.utils.AudioPlayerManager
import java.io.File

@Composable
fun InlineMediaTextEditor(
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit,
    onAddBlock: (BlockType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    
    // 构建包含占位符的文本和媒体块映射
    val (displayText, mediaMap) = remember(blocks) {
        buildTextWithPlaceholders(blocks)
    }
    
    var textFieldValue by remember(displayText) {
        mutableStateOf(TextFieldValue(displayText))
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 自定义布局，将媒体组件放置在文本中的正确位置
                InlineMediaLayout(
                    textFieldValue = textFieldValue,
                    mediaMap = mediaMap,
                    onTextChange = { newValue ->
                        textFieldValue = newValue
                        // 解析文本变化并更新块结构
                        updateBlocksFromInlineText(newValue.text, blocks, onBlocksChange)
                    },
                    onMediaDelete = { mediaBlock ->
                        val updatedBlocks = blocks.filter { it.id != mediaBlock.id }
                        onBlocksChange(updatedBlocks)
                    },
                    focusRequester = focusRequester,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 底部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val cursorPosition = getCursorLinePosition(textFieldValue)
                    onAddBlock(BlockType.IMAGE, cursorPosition)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("图片")
            }
            
            OutlinedButton(
                onClick = {
                    val cursorPosition = getCursorLinePosition(textFieldValue)
                    onAddBlock(BlockType.AUDIO, cursorPosition)
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("音频")
            }
        }
    }
}

@Composable
private fun InlineMediaLayout(
    textFieldValue: TextFieldValue,
    mediaMap: Map<Int, ContentBlock>,
    onTextChange: (TextFieldValue) -> Unit,
    onMediaDelete: (ContentBlock) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Layout(
        modifier = modifier,
        content = {
            // 主文本编辑器
            BasicTextField(
                value = textFieldValue,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "开始输入...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            
            // 为每个媒体块创建组件
            mediaMap.forEach { (lineIndex, mediaBlock) ->
                when (mediaBlock) {
                    is ContentBlock.ImageBlock -> {
                        InlineImageComponent(
                            block = mediaBlock,
                            onDelete = { onMediaDelete(mediaBlock) }
                        )
                    }
                    is ContentBlock.AudioBlock -> {
                        InlineAudioComponent(
                            block = mediaBlock,
                            onDelete = { onMediaDelete(mediaBlock) }
                        )
                    }
                    else -> {}
                }
            }
        }
    ) { measurables, constraints ->
        // 测量文本编辑器
        val textPlaceable = measurables[0].measure(constraints)
        
        val mediaPlaceables = mutableMapOf<Int, Placeable>()
        val mediaConstraints = constraints.copy(maxHeight = Constraints.Infinity)
        
        // 测量所有媒体组件
        mediaMap.entries.forEachIndexed { index, (lineIndex, _) ->
            if (index + 1 < measurables.size) {
                mediaPlaceables[lineIndex] = measurables[index + 1].measure(mediaConstraints)
            }
        }
        
        // 计算总高度（包括媒体组件）
        val totalHeight = calculateTotalHeight(textFieldValue.text, mediaPlaceables)
        
        layout(constraints.maxWidth, totalHeight) {
            // 放置文本编辑器
            textPlaceable.place(0, 0)
            
            // 根据文本行位置放置媒体组件
            placeMediaComponents(
                textFieldValue.text,
                mediaMap,
                mediaPlaceables,
                constraints.maxWidth
            )
        }
    }
}

@Composable
private fun InlineImageComponent(
    block: ContentBlock.ImageBlock,
    onDelete: () -> Unit
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
                .heightIn(min = 100.dp, max = 200.dp),
            contentScale = ContentScale.Fit
        )
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .size(28.dp)
        ) {
            Icon(
                Icons.Default.Close,
                "删除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun InlineAudioComponent(
    block: ContentBlock.AudioBlock,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        RoundedCornerShape(20.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                    "播放",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
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
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// 辅助函数
private fun buildTextWithPlaceholders(blocks: List<ContentBlock>): Pair<String, Map<Int, ContentBlock>> {
    val textBuilder = StringBuilder()
    val mediaMap = mutableMapOf<Int, ContentBlock>()
    var currentLine = 0
    
    blocks.forEach { block ->
        when (block) {
            is ContentBlock.TextBlock -> {
                if (block.text.isNotEmpty()) {
                    val lines = block.text.split('\n')
                    lines.forEachIndexed { index, line ->
                        textBuilder.append(line)
                        if (index < lines.size - 1) {
                            textBuilder.append('\n')
                            currentLine++
                        }
                    }
                    if (textBuilder.isNotEmpty() && !textBuilder.endsWith('\n')) {
                        textBuilder.append('\n')
                        currentLine++
                    }
                }
            }
            is ContentBlock.ImageBlock -> {
                textBuilder.append("[图片]\n")
                mediaMap[currentLine] = block
                currentLine++
            }
            is ContentBlock.AudioBlock -> {
                textBuilder.append("[音频]\n")
                mediaMap[currentLine] = block
                currentLine++
            }
        }
    }
    
    return Pair(textBuilder.toString().trimEnd('\n'), mediaMap)
}

private fun updateBlocksFromInlineText(
    text: String,
    originalBlocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit
) {
    val lines = text.split('\n')
    val newBlocks = mutableListOf<ContentBlock>()
    val originalMediaBlocks = originalBlocks.filter { it !is ContentBlock.TextBlock }
    var mediaIndex = 0
    
    lines.forEachIndexed { index, line ->
        when {
            line.trim() == "[图片]" -> {
                val imageBlocks = originalMediaBlocks.filterIsInstance<ContentBlock.ImageBlock>()
                if (mediaIndex < imageBlocks.size) {
                    newBlocks.add(imageBlocks[mediaIndex].copy(order = index))
                    mediaIndex++
                }
            }
            line.trim() == "[音频]" -> {
                val audioBlocks = originalMediaBlocks.filterIsInstance<ContentBlock.AudioBlock>()
                val audioIndex = mediaIndex - originalMediaBlocks.filterIsInstance<ContentBlock.ImageBlock>().size
                if (audioIndex >= 0 && audioIndex < audioBlocks.size) {
                    newBlocks.add(audioBlocks[audioIndex].copy(order = index))
                }
            }
            line.isNotBlank() -> {
                newBlocks.add(ContentBlock.TextBlock(order = index, text = line))
            }
        }
    }
    
    onBlocksChange(newBlocks)
}

private fun getCursorLinePosition(textFieldValue: TextFieldValue): Int {
    val textBeforeCursor = textFieldValue.text.substring(0, textFieldValue.selection.start)
    return textBeforeCursor.count { it == '\n' }
}

private fun calculateTotalHeight(
    text: String,
    mediaPlaceables: Map<Int, Placeable>
): Int {
    val lines = text.split('\n')
    var totalHeight = lines.size * 24 // 基本行高
    
    mediaPlaceables.values.forEach { placeable ->
        totalHeight += placeable.height
    }
    
    return totalHeight
}

private fun Placeable.PlacementScope.placeMediaComponents(
    text: String,
    mediaMap: Map<Int, ContentBlock>,
    mediaPlaceables: Map<Int, Placeable>,
    maxWidth: Int
) {
    val lines = text.split('\n')
    var yOffset = 0
    
    lines.forEachIndexed { lineIndex, line ->
        if (line.trim() in listOf("[图片]", "[音频]") && mediaPlaceables.containsKey(lineIndex)) {
            mediaPlaceables[lineIndex]?.place(0, yOffset)
            yOffset += mediaPlaceables[lineIndex]?.height ?: 0
        } else {
            yOffset += 24 // 文本行高
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}