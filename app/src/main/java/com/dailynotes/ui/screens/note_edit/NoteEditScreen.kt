package com.dailynotes.ui.screens.note_edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 分类选择
            Text(
                text = "分类",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        onClick = { viewModel.updateCategory(category) },
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
            
            // 内容输入
            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::updateContent,
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            // 媒体控制按钮
            MediaControls(
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
                Text(
                    text = "媒体文件",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                SimpleMediaDisplay(
                    mediaItems = uiState.mediaItems,
                    onDeleteItem = viewModel::removeMediaItem
                )
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