package com.dailynotes.ui.screens.note_list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailynotes.data.NoteEntity
import com.dailynotes.utils.ExportResult
import com.dailynotes.utils.ImportResult
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNavigateToEdit: (Long) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showCustomExportDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var exportSelectedOnly by remember { mutableStateOf(false) }
    var importReplaceMode by remember { mutableStateOf(false) }
    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    // ZIP文件导入选择器
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            pendingImportUri = it
            showImportPasswordDialog = true
        }
    }
    
    // 导出文件创建器
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { 
            val selectedIds = if (exportSelectedOnly && isSelectionMode) {
                selectedNotes.toList()
            } else {
                null
            }
            viewModel.exportNotesToUserLocation(it, selectedIds)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("已选择 ${selectedNotes.size} 项")
                    } else {
                        Text("日记本")
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "取消选择")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        // 批量删除模式的操作按钮
                        if (selectedNotes.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    viewModel.deleteSelectedNotes()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "删除选中")
                            }
                        }
                        
                        IconButton(onClick = { viewModel.selectAllNotes() }) {
                            Icon(Icons.Default.Check, contentDescription = "全选")
                        }
                    } else {
                        // 正常模式的菜单
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("批量删除") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.enterSelectionMode()
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("导出数据") },
                                    onClick = {
                                        showMenu = false
                                        showExportDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("导入数据") },
                                    onClick = {
                                        showMenu = false
                                        showImportDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(-1) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记事")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("搜索") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // 分类筛选
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) },
                        selected = selectedCategory == category
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 记事列表
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有记事，点击添加按钮创建第一条记事吧！",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedNotes.contains(note.id),
                            onClick = { 
                                if (isSelectionMode) {
                                    viewModel.toggleNoteSelection(note.id)
                                } else {
                                    onNavigateToEdit(note.id)
                                }
                            },
                            onDelete = { viewModel.deleteNote(note) },
                            onSelectionToggle = { viewModel.toggleNoteSelection(note.id) }
                        )
                    }
                }
            }
        }
    }
    
    // 导出结果提示
    exportResult?.let { result ->
        when (result) {
            is ExportResult.Success -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearExportResult() },
                    title = { Text("导出成功") },
                    text = { Text("数据已导出到: ${result.filePath}") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearExportResult() }) {
                            Text("确定")
                        }
                    }
                )
            }
            is ExportResult.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearExportResult() },
                    title = { Text("导出失败") },
                    text = { Text(result.message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearExportResult() }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }
    
    // 导入结果提示
    importResult?.let { result ->
        when (result) {
            is ImportResult.Success -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearImportResult() },
                    title = { Text("导入成功") },
                    text = { Text(result.message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearImportResult() }) {
                            Text("确定")
                        }
                    }
                )
            }
            is ImportResult.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearImportResult() },
                    title = { Text("导入失败") },
                    text = { Text(result.message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearImportResult() }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }
    
    // 导出选择对话框
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("选择导出方式") },
            text = {
                Column {
                    Text("请选择要导出的内容：")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isSelectionMode && selectedNotes.isNotEmpty()) {
                        Text(
                            "当前已选择 ${selectedNotes.size} 条记事",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    if (isSelectionMode && selectedNotes.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showExportDialog = false
                                exportSelectedOnly = true
                                pendingExportAction = {
                                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
                                    val fileName = "选中记事备份_${dateFormat.format(java.util.Date())}.zip"
                                    exportLauncher.launch(fileName)
                                }
                                showPasswordDialog = true
                            }
                        ) {
                            Text("导出选中")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            pendingExportAction = {
                                showCustomExportDialog = true
                            }
                            showPasswordDialog = true
                        }
                    ) {
                        Text("自定义选择")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportSelectedOnly = false
                            pendingExportAction = {
                                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
                                val fileName = "全部记事备份_${dateFormat.format(java.util.Date())}.zip"
                                exportLauncher.launch(fileName)
                            }
                            showPasswordDialog = true
                        }
                    ) {
                        Text("导出全部")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 导入选择对话框
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("选择导入方式") },
            text = {
                Column {
                    Text("请选择导入方式：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• 追加导入：保留现有记事，添加新记事",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• 替换导入：删除现有记事，仅保留导入的记事",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importReplaceMode = false
                            importLauncher.launch("application/zip")
                        }
                    ) {
                        Text("追加导入")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importReplaceMode = true
                            importLauncher.launch("application/zip")
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("替换导入")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 自定义导出选择对话框
    if (showCustomExportDialog) {
        CustomExportDialog(
            notes = notes,
            onDismiss = { showCustomExportDialog = false },
            onExport = { selectedNoteIds ->
                showCustomExportDialog = false
                exportSelectedOnly = true
                pendingExportAction = {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
                    val fileName = "自定义记事备份_${dateFormat.format(java.util.Date())}.zip"
                    // 设置要导出的记事ID
                    viewModel.setCustomExportIds(selectedNoteIds)
                    exportLauncher.launch(fileName)
                }
                showPasswordDialog = true
            }
        )
    }
    
    // 导出密码输入对话框
    if (showPasswordDialog) {
        PasswordDialog(
            title = "设置导出密码",
            onDismiss = { 
                showPasswordDialog = false
                pendingExportAction = null
            },
            onConfirm = { password ->
                showPasswordDialog = false
                viewModel.setExportPassword(password)
                pendingExportAction?.invoke()
                pendingExportAction = null
            },
            viewModel = viewModel
        )
    }
    
    // 导入密码输入对话框
    if (showImportPasswordDialog) {
        PasswordDialog(
            title = "输入导入密码",
            onDismiss = { 
                showImportPasswordDialog = false
                pendingImportUri = null
            },
            onConfirm = { password ->
                showImportPasswordDialog = false
                pendingImportUri?.let { uri ->
                    viewModel.importNotesWithPassword(uri, password, importReplaceMode)
                }
                pendingImportUri = null
            },
            viewModel = viewModel,
            isImport = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    viewModel: NoteListViewModel,
    isImport: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isImport) {
                    Text(
                        text = viewModel.getPasswordRequirements(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = ""
                    },
                    label = { Text(if (isImport) "请输入密码" else "设置密码") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Done else Icons.Default.Check,
                                contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (!isImport) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            passwordError = ""
                        },
                        label = { Text("确认密码") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                if (passwordError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        password.isEmpty() -> {
                            passwordError = "密码不能为空"
                        }
                        !isImport && !viewModel.validatePassword(password) -> {
                            passwordError = "密码不符合要求"
                        }
                        !isImport && password != confirmPassword -> {
                            passwordError = "两次输入的密码不一致"
                        }
                        else -> {
                            onConfirm(password)
                        }
                    }
                },
                enabled = password.isNotEmpty() && (isImport || confirmPassword.isNotEmpty())
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CustomExportDialog(
    notes: List<NoteEntity>,
    onDismiss: () -> Unit,
    onExport: (List<Long>) -> Unit
) {
    var selectedNotes by remember { mutableStateOf(setOf<Long>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择要导出的记事") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "共 ${notes.size} 条记事，已选择 ${selectedNotes.size} 条",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    TextButton(
                        onClick = {
                            selectedNotes = if (selectedNotes.size == notes.size) {
                                emptySet()
                            } else {
                                notes.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(if (selectedNotes.size == notes.size) "取消全选" else "全选")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                LazyColumn {
                    items(notes) { note ->
                        val isSelected = selectedNotes.contains(note.id)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNotes = if (isSelected) {
                                        selectedNotes - note.id
                                    } else {
                                        selectedNotes + note.id
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedNotes = if (checked) {
                                        selectedNotes + note.id
                                    } else {
                                        selectedNotes - note.id
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title.ifEmpty { "无标题" },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (note.content.isNotEmpty()) {
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(note.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onExport(selectedNotes.toList()) },
                enabled = selectedNotes.isNotEmpty()
            ) {
                Text("导出 (${selectedNotes.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: NoteEntity,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSelectionToggle: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { 
                    if (!isSelectionMode) {
                        showDeleteDialog = true 
                    }
                }
            ),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 选择指示器
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectionToggle() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifEmpty { "无标题" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (note.content.isNotEmpty()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = dateFormatter.format(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (note.mediaItems.isNotEmpty()) {
                    Text(
                        text = "${note.mediaItems.size} 个媒体文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记事") },
            text = { Text("确定要删除这条记事吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}