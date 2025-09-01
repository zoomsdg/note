package com.dailynotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun BlockBasedEditor(
    blocks: List<ContentBlock>,
    onBlocksChange: (List<ContentBlock>) -> Unit,
    onAddBlock: (BlockType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 渲染所有块，确保它们在垂直流中正确排列
        blocks.forEachIndexed { index, block ->
            when (block) {
                is ContentBlock.TextBlock -> {
                    TextBlockEditor(
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
                        onAddBlock = { type -> onAddBlock(type, index + 1) }
                    )
                }
                is ContentBlock.ImageBlock -> {
                    ImageBlockDisplay(
                        block = block,
                        onDeleteBlock = {
                            val updatedBlocks = blocks.filterIndexed { i, _ -> i != index }
                            onBlocksChange(updatedBlocks)
                        },
                        onAddBlock = { type -> onAddBlock(type, index + 1) }
                    )
                }
                is ContentBlock.AudioBlock -> {
                    AudioBlockDisplay(
                        block = block,
                        onDeleteBlock = {
                            val updatedBlocks = blocks.filterIndexed { i, _ -> i != index }
                            onBlocksChange(updatedBlocks)
                        },
                        onAddBlock = { type -> onAddBlock(type, index + 1) }
                    )
                }
            }
        }
        
        // 添加新块按钮
        AddBlockButton(
            onAddBlock = { type -> 
                onAddBlock(type, blocks.size)
            }
        )
    }
}

@Composable
private fun TextBlockEditor(
    block: ContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onDeleteBlock: () -> Unit,
    onAddBlock: (BlockType) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(block.text)) }
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(block.text) {
        if (textFieldValue.text != block.text) {
            textFieldValue = TextFieldValue(block.text)
        }
    }
    
    // 如果是新创建的空文本块，自动聚焦
    LaunchedEffect(block.id) {
        if (block.text.isEmpty()) {
            focusRequester.requestFocus()
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onTextChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
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
            
            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 添加图片按钮
                IconButton(onClick = { onAddBlock(BlockType.IMAGE) }) {
                    Icon(Icons.Default.Add, "添加图片", modifier = Modifier.size(16.dp))
                }
                
                // 添加音频按钮
                IconButton(onClick = { onAddBlock(BlockType.AUDIO) }) {
                    Icon(Icons.Default.PlayArrow, "添加音频", modifier = Modifier.size(16.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 删除按钮（仅当文本为空时显示）
                if (block.text.isEmpty()) {
                    IconButton(onClick = onDeleteBlock) {
                        Icon(
                            Icons.Default.Delete, 
                            "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // 长按菜单
        if (showMenu) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("复制文本") },
                    onClick = {
                        // TODO: 实现复制功能
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除块") },
                    onClick = {
                        onDeleteBlock()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageBlockDisplay(
    block: ContentBlock.ImageBlock,
    onDeleteBlock: () -> Unit,
    onAddBlock: (BlockType) -> Unit
) {
    var showPreview by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPreview = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = if (block.localPath.isNotEmpty()) File(block.localPath) else block.url,
                contentDescription = block.alt.ifEmpty { "图片" },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 300.dp),
                contentScale = ContentScale.Fit
            )
            
            // 删除按钮
            IconButton(
                onClick = onDeleteBlock,
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
        
        // 操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = { onAddBlock(BlockType.TEXT) }) {
                Text("添加文本")
            }
            TextButton(onClick = { onAddBlock(BlockType.AUDIO) }) {
                Text("添加音频")
            }
        }
    }
    
    // 图片预览
    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AsyncImage(
                        model = if (block.localPath.isNotEmpty()) File(block.localPath) else block.url,
                        contentDescription = "图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    TextButton(
                        onClick = { showPreview = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioBlockDisplay(
    block: ContentBlock.AudioBlock,
    onDeleteBlock: () -> Unit,
    onAddBlock: (BlockType) -> Unit
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                IconButton(onClick = onDeleteBlock) {
                    Icon(
                        Icons.Default.Delete,
                        "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onAddBlock(BlockType.TEXT) }) {
                    Text("添加文本")
                }
                TextButton(onClick = { onAddBlock(BlockType.IMAGE) }) {
                    Text("添加图片")
                }
            }
        }
    }
}

@Composable
private fun AddBlockButton(
    onAddBlock: (BlockType) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                "添加",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加内容块",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("文本") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = {
                    onAddBlock(BlockType.TEXT)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("图片") },
                leadingIcon = { Icon(Icons.Default.Add, null) },
                onClick = {
                    onAddBlock(BlockType.IMAGE)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("音频") },
                leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                onClick = {
                    onAddBlock(BlockType.AUDIO)
                    showMenu = false
                }
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}