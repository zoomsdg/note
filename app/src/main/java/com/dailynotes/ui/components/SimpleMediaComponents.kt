package com.dailynotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.dailynotes.utils.AudioPlayerManager
import java.io.File

@Composable
fun MediaControls(
    onImageClick: () -> Unit,
    onAudioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onImageClick,
            modifier = Modifier.weight(1f)
        ) {
            Text("添加图片")
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        OutlinedButton(
            onClick = onAudioClick,
            modifier = Modifier.weight(1f)
        ) {
            Text("选择音频")
        }
    }
}

@Composable
fun SimpleMediaDisplay(
    mediaItems: List<com.dailynotes.data.MediaItem>,
    onDeleteItem: (com.dailynotes.data.MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showImageDialog by remember { mutableStateOf<String?>(null) }
    var currentPlayingAudio by remember { mutableStateOf<String?>(null) }
    
    if (mediaItems.isNotEmpty()) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(mediaItems) { mediaItem ->
                when (mediaItem.type) {
                    com.dailynotes.data.MediaType.IMAGE -> {
                        ImageCard(
                            imagePath = mediaItem.path,
                            onDelete = { onDeleteItem(mediaItem) },
                            onClick = { showImageDialog = mediaItem.path }
                        )
                    }
                    com.dailynotes.data.MediaType.AUDIO -> {
                        AudioCard(
                            mediaItem = mediaItem,
                            onDelete = { onDeleteItem(mediaItem) },
                            isCurrentlyPlaying = currentPlayingAudio == mediaItem.path,
                            onPlaybackStateChange = { playing, path ->
                                currentPlayingAudio = if (playing) path else null
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 图片放大对话框
    showImageDialog?.let { imagePath ->
        ImagePreviewDialog(
            imagePath = imagePath,
            onDismiss = { showImageDialog = null }
        )
    }
}

@Composable
private fun ImageCard(
    imagePath: String,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(120.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 显示实际的图片
            AsyncImage(
                model = File(imagePath),
                contentDescription = "图片",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() },
                contentScale = ContentScale.Crop
            )
            
            // 删除按钮
            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Text("×", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AudioCard(
    mediaItem: com.dailynotes.data.MediaItem,
    onDelete: () -> Unit,
    isCurrentlyPlaying: Boolean,
    onPlaybackStateChange: (Boolean, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager.getInstance(context) }
    
    Card(
        modifier = modifier.width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "音频文件",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (mediaItem.duration > 0) {
                        Text(
                            text = com.dailynotes.utils.MediaUtils.formatDuration(mediaItem.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                TextButton(
                    onClick = onDelete
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
            
            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        audioPlayerManager.playAudio(mediaItem.path) { playing, path ->
                            onPlaybackStateChange(playing, path)
                        }
                    }
                ) {
                    if (isCurrentlyPlaying) {
                        Text(
                            text = "⏸",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放"
                        )
                    }
                }
                Text(
                    text = if (isCurrentlyPlaying) "播放中..." else "点击播放",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "图片预览",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clickable { onDismiss() },
                    contentScale = ContentScale.Fit
                )
                
                TextButton(
                    onClick = onDismiss,
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