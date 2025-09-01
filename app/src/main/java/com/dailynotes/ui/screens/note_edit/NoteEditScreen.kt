package com.dailynotes.ui.screens.note_edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.dailynotes.utils.AudioPlayerManager
import java.io.File
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
import com.dailynotes.ui.components.RichTextEditor
import com.dailynotes.utils.MediaUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 将选中的图片复制到应用私有目录
            val imageFile = MediaUtils.createImageFile(context, "image_${System.currentTimeMillis()}")
            if (MediaUtils.copyImageFromUri(context, uri, imageFile)) {
                val mediaItem = MediaItem(type = MediaType.IMAGE, path = imageFile.absolutePath)
                viewModel.insertMediaAtCursor(mediaItem)
            }
        }
    }
    
    // 音频选择器
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 将选中的音频复制到应用私有目录
            val audioFile = MediaUtils.createAudioFile(context, "audio_${System.currentTimeMillis()}")
            if (MediaUtils.copyAudioFromUri(context, uri, audioFile)) {
                val duration = MediaUtils.getAudioDuration(audioFile.absolutePath)
                val mediaItem = MediaItem(type = MediaType.AUDIO, path = audioFile.absolutePath, duration = duration)
                viewModel.insertMediaAtCursor(mediaItem)
            }
        }
    }
    
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
        viewModel.loadCategories()
    }
    
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 移除标题，给内容更多空间 */ },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.saveAndExit(onNavigateBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                        
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("分类") },
                                onClick = {
                                    showOptionsMenu = false
                                    showCategoryDialog = true
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("添加图片") },
                                onClick = {
                                    showOptionsMenu = false
                                    imagePickerLauncher.launch("image/*")
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("添加音频") },
                                onClick = {
                                    showOptionsMenu = false
                                    audioPickerLauncher.launch("audio/*")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // 真正的单一文本框 + 智能媒体覆盖层
        PureSingleTextEditor(
            textFieldValue = uiState.contentField,
            onTextFieldValueChange = viewModel::updateContent,
            mediaItems = uiState.mediaItems,
            onDeleteMediaItem = viewModel::removeMediaItem,
            onMediaClick = { mediaItem ->
                // 点击图片时光标定位到图片下一行
                viewModel.moveCursorToMediaEnd(mediaItem)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
    
    // 分类选择对话框
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("选择分类") },
            text = {
                LazyColumn {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCategory(category)
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.category == category,
                                onClick = {
                                    viewModel.updateCategory(category)
                                    showCategoryDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = category)
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCategoryDialog = false
                                    showAddCategoryDialog = true
                                    newCategoryText = ""
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加新分类",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "添加新分类",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 添加新分类对话框
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("添加新分类") },
            text = {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryText.isNotBlank()) {
                            viewModel.updateCategory(newCategoryText.trim())
                            showAddCategoryDialog = false
                        }
                    },
                    enabled = newCategoryText.isNotBlank()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// 纯单一文本框编辑器 - 媒体覆盖在文本框之上
@Composable
private fun PureSingleTextEditor(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    mediaItems: List<MediaItem>,
    onDeleteMediaItem: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    // 自动聚焦
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // 忽略焦点请求错误
        }
    }
    
    // 计算媒体项目的位置信息
    val mediaPositions = remember(textFieldValue.text, mediaItems) {
        calculateMediaPositions(textFieldValue.text, mediaItems)
    }
    
    Box(modifier = modifier.padding(16.dp)) {
        // 单一的大文本框 - 占据全部空间
        BasicTextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
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
                    if (textFieldValue.text.isEmpty() && mediaItems.isEmpty()) {
                        Text(
                            text = "开始记录...",
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
        
        // 媒体覆盖层 - 根据光标位置精确定位
        mediaPositions.forEach { mediaPosition ->
            Box(
                modifier = Modifier
                    .offset(
                        x = 0.dp, 
                        y = (mediaPosition.lineNumber * 28).dp // 近似行高
                    )
            ) {
                when (mediaPosition.mediaItem.type) {
                    MediaType.IMAGE -> {
                        OverlayImageComponent(
                            mediaItem = mediaPosition.mediaItem,
                            onDelete = { onDeleteMediaItem(mediaPosition.mediaItem) },
                            onClick = { onMediaClick(mediaPosition.mediaItem) }
                        )
                    }
                    MediaType.AUDIO -> {
                        OverlayAudioComponent(
                            mediaItem = mediaPosition.mediaItem,
                            onDelete = { onDeleteMediaItem(mediaPosition.mediaItem) },
                            onClick = { onMediaClick(mediaPosition.mediaItem) }
                        )
                    }
                }
            }
        }
    }
}

// 媒体位置信息
data class MediaPosition(
    val mediaItem: MediaItem,
    val lineNumber: Int,
    val columnPosition: Int
)

// 计算媒体在文本中的位置
private fun calculateMediaPositions(
    text: String, 
    mediaItems: List<MediaItem>
): List<MediaPosition> {
    val positions = mutableListOf<MediaPosition>()
    val lines = text.split('\n')
    var currentLineNumber = 0
    
    // 简化版本：将媒体项目按时间戳顺序放置在文本行之间
    mediaItems.forEachIndexed { index, mediaItem ->
        val targetLine = minOf(index, lines.size)
        positions.add(
            MediaPosition(
                mediaItem = mediaItem,
                lineNumber = targetLine,
                columnPosition = 0
            )
        )
    }
    
    return positions
}

// 混合内容项目类型
sealed class MixedContentItem {
    data class TextItem(val text: String) : MixedContentItem()
    data class MediaItem(val mediaItem: com.dailynotes.data.MediaItem) : MixedContentItem()
}

// 构建混合内容的函数
private fun buildMixedContent(
    text: String, 
    mediaItems: List<com.dailynotes.data.MediaItem>
): List<MixedContentItem> {
    val result = mutableListOf<MixedContentItem>()
    
    // 如果有文本内容，先添加文本
    if (text.isNotEmpty()) {
        result.add(MixedContentItem.TextItem(text))
    }
    
    // 添加所有媒体项目
    mediaItems.forEach { mediaItem ->
        result.add(MixedContentItem.MediaItem(mediaItem))
    }
    
    return result
}

// 内嵌式图片组件 - 原尺寸显示
@Composable
private fun InlineImageComponent(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // 图片原尺寸显示 - 不裁剪，保持宽高比
            AsyncImage(
                model = File(mediaItem.path),
                contentDescription = "图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { showPreview = true },
                contentScale = ContentScale.FillWidth // 宽度填满，高度自适应，保持原始比例
            )
            
            // 删除按钮
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
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
    }
    
    // 图片预览对话框
    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    AsyncImage(
                        model = File(mediaItem.path),
                        contentDescription = "图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
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

// 内嵌式音频组件
@Composable
private fun InlineAudioComponent(
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
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 播放按钮
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                            isPlaying = playing
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // 音频信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "音频文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (mediaItem.duration > 0) {
                        val minutes = mediaItem.duration / 1000 / 60
                        val seconds = (mediaItem.duration / 1000) % 60
                        "%02d:%02d".format(minutes, seconds)
                    } else "未知时长",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 删除按钮
            Card(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(18.dp),
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// 支持点击定位的覆盖层图片组件
@Composable
private fun OverlayImageComponent(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPreview by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            // 图片原尺寸显示 - 点击时光标定位到图片下方
            AsyncImage(
                model = File(mediaItem.path),
                contentDescription = "图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { 
                        onClick() // 光标定位到图片下一行
                    },
                contentScale = ContentScale.FillWidth // 原尺寸显示
            )
            
            // 删除按钮
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
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
            
            // 预览按钮 - 右下角
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clickable { showPreview = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
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
        }
    }
    
    // 图片预览对话框
    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    AsyncImage(
                        model = File(mediaItem.path),
                        contentDescription = "图片预览",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
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

// 支持点击定位的覆盖层音频组件
@Composable
private fun OverlayAudioComponent(
    mediaItem: MediaItem,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }, // 点击音频也定位光标
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 播放按钮
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                            isPlaying = playing
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // 音频信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "音频文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (mediaItem.duration > 0) {
                        val minutes = mediaItem.duration / 1000 / 60
                        val seconds = (mediaItem.duration / 1000) % 60
                        "%02d:%02d".format(minutes, seconds)
                    } else "未知时长",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 删除按钮
            Card(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(18.dp),
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// 覆盖层音频组件
@Composable
private fun OverlayAudioComponent(
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
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 播放按钮
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { 
                        audioPlayerManager.playAudio(mediaItem.path) { playing, _ ->
                            isPlaying = playing
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // 音频信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "音频文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (mediaItem.duration > 0) {
                        val minutes = mediaItem.duration / 1000 / 60
                        val seconds = (mediaItem.duration / 1000) % 60
                        "%02d:%02d".format(minutes, seconds)
                    } else "未知时长",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 删除按钮
            Card(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onDelete() },
                shape = RoundedCornerShape(18.dp),
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}