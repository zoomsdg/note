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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dailynotes.data.ContentBlock
import com.dailynotes.data.BlockType
import com.dailynotes.utils.AudioPlayerManager
import java.io.File

@Composable
fun SimpleInlineEditor(
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit,
    onAddBlock: (BlockType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 渲染所有块，但在文本流中显示
                blocks.forEachIndexed { index, block ->
                    when (block) {
                        is ContentBlock.TextBlock -> {
                            InlineTextBlock(
                                block = block,
                                onTextChange = { newText ->
                                    val updatedBlocks = blocks.toMutableList()
                                    updatedBlocks[index] = block.copy(text = newText)
                                    onBlocksChange(updatedBlocks)
                                },
                                onDeleteBlock = {
                                    val updatedBlocks = blocks.filterIndexed { i, _ -> i != index }
                                    onBlocksChange(updatedBlocks)
                                },
                                onAddBlock = { type -> onAddBlock(type, index + 1) },
                                focusRequester = if (index == blocks.lastIndex && block.text.isEmpty()) focusRequester else null
                            )
                        }
                        is ContentBlock.ImageBlock -> {
                            InlineImageBlock(
                                block = block,
                                onDelete = {
                                    val updatedBlocks = blocks.filterIndexed { i, _ -> i != index }
                                    onBlocksChange(updatedBlocks)
                                }
                            )
                        }
                        is ContentBlock.AudioBlock -> {
                            InlineAudioBlock(
                                block = block,
                                onDelete = {
                                    val updatedBlocks = blocks.filterIndexed { i, _ -> i != index }
                                    onBlocksChange(updatedBlocks)
                                }
                            )
                        }
                    }
                }
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
                    onAddBlock(BlockType.IMAGE, blocks.size)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("图片")
            }
            
            OutlinedButton(
                onClick = {
                    onAddBlock(BlockType.AUDIO, blocks.size)
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
private fun InlineTextBlock(
    block: ContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onDeleteBlock: () -> Unit,
    onAddBlock: (BlockType) -> Unit,
    focusRequester: FocusRequester?
) {
    var textFieldValue by remember(block.text) { 
        mutableStateOf(TextFieldValue(block.text))
    }
    
    // 自动聚焦新创建的空文本块
    LaunchedEffect(block.id) {
        if (block.text.isEmpty() && focusRequester != null) {
            focusRequester.requestFocus()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onTextChange(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .padding(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "输入文本...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
        
        // 空文本块的删除按钮
        if (block.text.isEmpty()) {
            IconButton(
                onClick = onDeleteBlock,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
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
    
    // 添加内容按钮（在文本块后）
    if (block.text.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onAddBlock(BlockType.IMAGE) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    "添加图片",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            IconButton(
                onClick = { onAddBlock(BlockType.AUDIO) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow, 
                    "添加音频",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InlineImageBlock(
    block: ContentBlock.ImageBlock,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            AsyncImage(
                model = if (block.localPath.isNotEmpty()) File(block.localPath) else block.url,
                contentDescription = block.alt.ifEmpty { "图片" },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 250.dp),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        RoundedCornerShape(12.dp)
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
}

@Composable
private fun InlineAudioBlock(
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
                    .size(36.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                    "播放",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
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
                modifier = Modifier.size(28.dp)
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

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}