package com.dailynotes.ui.screens.note_edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailynotes.data.MediaItem
import com.dailynotes.data.MediaType
import com.dailynotes.ui.components.MediaControls
import com.dailynotes.ui.components.SimpleMediaDisplay
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
    var categoryExpanded by remember { mutableStateOf(false) }
    var mediaExpanded by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 将选中的图片复制到应用私有目录
            val imageFile = MediaUtils.createImageFile(context, "image_${System.currentTimeMillis()}")
            if (MediaUtils.copyImageFromUri(context, uri, imageFile)) {
                val mediaItem = MediaItem(type = MediaType.IMAGE, path = imageFile.absolutePath)
                viewModel.addMediaItem(mediaItem)
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
                viewModel.addMediaItem(mediaItem)
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
                title = { 
                    Text(
                        if (noteId == -1L) "添加记事" 
                        else "编辑记事"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.saveAndExit(onNavigateBack)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            viewModel.saveAndExit(onNavigateBack)
                        }
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 移除标题输入框，标题将自动从内容第一行第一句提取
            
            // 分类选择 - 可折叠
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = !categoryExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "分类: ${uiState.category}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (categoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (categoryExpanded) "收起" else "展开"
                        )
                    }
                    
                    if (categoryExpanded) {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    onClick = { 
                                        viewModel.updateCategory(category)
                                        categoryExpanded = false
                                    },
                                    label = { Text(category) },
                                    selected = uiState.category == category
                                )
                            }
                            
                            item {
                                FilterChip(
                                    onClick = { 
                                        showAddCategoryDialog = true
                                        newCategoryText = ""
                                    },
                                    label = { Text("添加新分类") },
                                    selected = false
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            // 内容输入 - 支持光标位置和媒体插入
            OutlinedTextField(
                value = uiState.contentField,
                onValueChange = viewModel::updateContent,
                label = { Text("内容（标题将自动从第一行第一句提取）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp), // 由于移除标题框，可以更大
                maxLines = Int.MAX_VALUE, // 支持多行文本
                placeholder = { Text("在此输入记事内容...") }
            )
            
            // 媒体区域 - 可折叠
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mediaExpanded = !mediaExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "媒体 (${uiState.mediaItems.size})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (mediaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (mediaExpanded) "收起" else "展开"
                        )
                    }
                    
                    if (mediaExpanded) {
                        // 媒体控制按钮
                        MediaControls(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onImageClick = {
                                // 打开图片选择器
                                imagePickerLauncher.launch("image/*")
                            },
                            onAudioClick = {
                                // 打开音频选择器
                                audioPickerLauncher.launch("audio/*")
                            }
                        )
                        
                        // 媒体文件显示
                        if (uiState.mediaItems.isNotEmpty()) {
                            SimpleMediaDisplay(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                mediaItems = uiState.mediaItems,
                                onDeleteItem = viewModel::removeMediaItem
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
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