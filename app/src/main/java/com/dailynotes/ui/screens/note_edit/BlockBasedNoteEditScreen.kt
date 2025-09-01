package com.dailynotes.ui.screens.note_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailynotes.data.BlockType
import com.dailynotes.ui.components.SimpleInlineEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockBasedNoteEditScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: BlockBasedNoteEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var showTitleDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
        viewModel.loadCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.title.isNotEmpty()) uiState.title else "新建记事",
                        maxLines = 1
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
                    // 标题编辑按钮
                    IconButton(onClick = { showTitleDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑标题")
                    }
                    
                    // 菜单按钮
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                        }
                        
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("分类") },
                                leadingIcon = { Icon(Icons.Default.List, null) },
                                onClick = {
                                    showOptionsMenu = false
                                    showCategoryDialog = true
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("导出") },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = {
                                    showOptionsMenu = false
                                    showExportDialog = true
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("保存") },
                                leadingIcon = { Icon(Icons.Default.Check, null) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.saveNote()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 分类显示
            if (uiState.category != "其他") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "分类: ${uiState.category}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 简单内联编辑器
            SimpleInlineEditor(
                blocks = uiState.blocks,
                onBlocksChange = viewModel::updateBlocks,
                onAddBlock = viewModel::addBlock,
                modifier = Modifier.weight(1f)
            )
        }
    }
    
    // 标题编辑对话框
    if (showTitleDialog) {
        var titleText by remember { mutableStateOf(uiState.title) }
        
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("编辑标题") },
            text = {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.updateTitle(titleText)
                        showTitleDialog = false 
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTitleDialog = false }) {
                    Text("取消")
                }
            }
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
                                .padding(vertical = 4.dp),
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
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 导出对话框
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出记事") },
            text = {
                Column {
                    Text(
                        text = "选择导出格式：",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 纯文本导出
                    OutlinedButton(
                        onClick = {
                            val plainText = viewModel.exportToPlainText()
                            // TODO: 实现分享功能
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("纯文本")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Markdown导出
                    OutlinedButton(
                        onClick = {
                            val markdown = viewModel.exportToMarkdown()
                            // TODO: 实现分享功能
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Markdown")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}