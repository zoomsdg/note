package com.dailynotes.ui.screens.note_edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                        }
                        
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("选择分类") },
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
        // 富文本编辑器 - 占据全屏空间，支持内嵌媒体显示
        RichTextEditor(
            contentBlocks = uiState.contentBlocks,
            onContentChange = viewModel::updateContentBlocks,
            onRequestFocus = { /* TODO: 实现焦点管理 */ },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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