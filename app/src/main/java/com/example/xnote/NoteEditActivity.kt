package com.example.xnote

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.xnote.audio.AudioPlayer
import com.example.xnote.audio.AudioRecorder
import com.example.xnote.data.BlockType
import com.example.xnote.data.Category
import com.example.xnote.data.FullNote
import com.example.xnote.data.NoteBlock
import com.example.xnote.databinding.ActivityNoteEditBinding
import com.example.xnote.repository.NoteRepository
import com.example.xnote.utils.FileUtils
import com.example.xnote.utils.ImageUtils
import com.example.xnote.utils.PermissionUtils
import com.example.xnote.viewmodel.NoteEditViewModel
import com.example.xnote.viewmodel.NoteEditViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoteEditActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_NOTE_ID = "note_id"
    }
    
    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var noteId: String
    private var currentNote: FullNote? = null
    private var initialTitle: String = ""
    private var initialBlocks: List<NoteBlock> = emptyList()
    private var hasContentChanged = false
    private var currentCategoryId: String = "daily"
    
    private val viewModel: NoteEditViewModel by viewModels {
        NoteEditViewModelFactory(NoteRepository(this))
    }
    
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private var isRecording = false
    
    private val recordingHandler = Handler(Looper.getMainLooper())
    private var recordingRunnable: Runnable? = null
    
    // Activity Result Launchers
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { handleImageSelected(it) } }
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> 
        if (success) {
            // Handle camera result
        }
    }
    
    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { handleAudioSelected(it) } }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        
        initComponents()
        setupUI()
        loadNote()
        observeViewModel()
    }
    
    private fun initComponents() {
        audioRecorder = AudioRecorder(this)
        audioPlayer = AudioPlayer()
        
        audioPlayer.setOnCompletionListener {
            // Handle audio playback completion
        }
    }
    
    private fun setupUI() {
       //./ binding.toolbar.setNavigationOnClickListener {
         //   finish()
       // }
        
        binding.btnCategory.setOnClickListener {
            showCategoryDialog()
        }
        
        binding.btnSave.setOnClickListener {
            saveNote()
        }
        
        binding.btnAddImage.setOnClickListener {
            showImageOptions()
        }
        
        binding.btnAddAudio.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                showAudioOptions()
            }
        }
        
        binding.btnStopRecording.setOnClickListener {
            stopRecording()
        }
        
        binding.btnCancelRecording.setOnClickListener {
            cancelRecording()
        }
        
        // 富文本编辑器设置
        binding.richEditText.setOnContentChangedListener { _ ->
            // 内容变更时的处理
            hasContentChanged = true
        }
        
        binding.richEditText.setOnMediaClickListener { block ->
            handleMediaClick(block)
        }
        
        // 文本变更监听
        binding.editTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // 标题内容有变更
                hasContentChanged = true
            }
        })
        
        // 标题回车键监听
        binding.editTitle.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN) {
                binding.richEditText.requestFocus()
                binding.richEditText.setSelection(0)
                true
            } else {
                false
            }
        }
    }
    
    private fun loadNote() {
        lifecycleScope.launch {
            try {
                currentNote = viewModel.loadNote(noteId)
                currentNote?.let { note ->
                    // 记录初始状态
                    initialTitle = note.note.title
                    initialBlocks = note.blocks.toList()
                    currentCategoryId = note.note.categoryId
                    
                    binding.editTitle.setText(note.note.title)
                    binding.richEditText.loadFromBlocks(note.blocks)
                //./    binding.toolbar.title = if (note.note.title.isEmpty()) "编辑记事" else note.note.title
                    updateModifiedTime(note.note.updatedAt)
                    // updateCategoryDisplay() 已在 updateModifiedTime 中调用
                }
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "加载记事失败", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.saveState.collect { state ->
                when (state) {
                    is NoteEditViewModel.SaveState.Saving -> {
                        binding.tvStatus.text = "正在保存..."
                        binding.tvStatus.visibility = View.VISIBLE
                    }
                    is NoteEditViewModel.SaveState.Success -> {
                        binding.tvStatus.text = "已保存"
                        binding.tvStatus.visibility = View.VISIBLE
                        // 更新修改时间
                        updateModifiedTime(System.currentTimeMillis())
                        // 重置变更标记和初始状态
                        hasContentChanged = false
                        initialTitle = binding.editTitle.text.toString()
                        initialBlocks = binding.richEditText.toBlocks()
                        // 1秒后隐藏状态
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.tvStatus.visibility = View.GONE
                        }, 1000)
                    }
                    is NoteEditViewModel.SaveState.Error -> {
                        binding.tvStatus.visibility = View.GONE
                        Toast.makeText(this@NoteEditActivity, "保存失败: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.tvStatus.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun saveNote() {
        val title = binding.editTitle.text.toString().trim()
        val blocks = binding.richEditText.toBlocks()
        
        lifecycleScope.launch {
            viewModel.saveNote(noteId, title, blocks, currentCategoryId)
        }
    }
    
    private fun updateModifiedTime(timestamp: Long) {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedTime = formatter.format(Date(timestamp))
        binding.tvModifiedTime.text = "修改于 $formattedTime"
        binding.tvModifiedTime.visibility = View.VISIBLE
        
        // 确保分类标签也显示
        updateCategoryDisplay()
    }
    
    private fun updateCategoryDisplay() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("NoteEditActivity", "updateCategoryDisplay: currentCategoryId = $currentCategoryId")
                val category = viewModel.getCategoryById(currentCategoryId)
                if (category != null) {
                    android.util.Log.d("NoteEditActivity", "Found category: ${category.name}")
                    binding.tvCategoryTag.text = category.name
                    binding.tvCategoryTag.visibility = View.VISIBLE
                } else {
                    android.util.Log.d("NoteEditActivity", "Category not found for id: $currentCategoryId")
                    binding.tvCategoryTag.visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("NoteEditActivity", "Error updating category display", e)
                binding.tvCategoryTag.visibility = View.GONE
            }
        }
    }
    
    private fun checkContentChanged(): Boolean {
        val currentTitle = binding.editTitle.text.toString()
        val currentBlocks = binding.richEditText.toBlocks()
        
        // 检查标题是否改变
        if (currentTitle != initialTitle) {
            return true
        }
        
        // 检查块数量是否改变
        if (currentBlocks.size != initialBlocks.size) {
            return true
        }
        
        // 检查每个块是否改变
        for (i in currentBlocks.indices) {
            val current = currentBlocks[i]
            val initial = initialBlocks.getOrNull(i)
            
            if (initial == null) return true
            
            // 比较块的关键属性
            if (current.type != initial.type ||
                current.text != initial.text ||
                current.url != initial.url ||
                current.order != initial.order ||
                current.alt != initial.alt ||
                current.duration != initial.duration ||
                current.width != initial.width ||
                current.height != initial.height) {
                return true
            }
        }
        
        return false
    }
    
    private fun isContentEmpty(): Boolean {
        val title = binding.editTitle.text.toString().trim()
        val blocks = binding.richEditText.toBlocks()
        
        // 如果标题不为空，认为有内容
        if (title.isNotEmpty()) {
            return false
        }
        
        // 检查所有文本块是否为空
        for (block in blocks) {
            when (block.type) {
                BlockType.TEXT -> {
                    if (!block.text.isNullOrBlank()) {
                        return false
                    }
                }
                BlockType.IMAGE, BlockType.AUDIO -> {
                    // 有媒体文件就认为有内容
                    return false
                }
            }
        }
        
        return true
    }
    
    private fun showImageOptions() {
        val options = arrayOf("相册选择", "拍照")
        val dialog = AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImageFromGallery()
                    1 -> takePhoto()
                }
            }
            .create()
        
        dialog.setOnShowListener {
            dialog.listView?.let { listView ->
                for (i in 0 until listView.count) {
                    val textView = listView.getChildAt(i) as? android.widget.TextView
                    textView?.textSize = 18f
                    textView?.setPadding(48, 32, 48, 32)
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showAudioOptions() {
        val options = arrayOf("选择音频文件", "录音")
        val dialog = AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectAudioFromFiles()
                    1 -> startRecording()
                }
            }
            .create()
        
        dialog.setOnShowListener {
            dialog.listView?.let { listView ->
                for (i in 0 until listView.count) {
                    val textView = listView.getChildAt(i) as? android.widget.TextView
                    textView?.textSize = 18f
                    textView?.setPadding(48, 32, 48, 32)
                }
            }
        }
        
        dialog.show()
    }
    
    private fun selectImageFromGallery() {
        if (PermissionUtils.hasStoragePermission(this)) {
            pickImageLauncher.launch("image/*")
        } else {
            PermissionUtils.requestStoragePermission(this)
        }
    }
    
    private fun takePhoto() {
        if (PermissionUtils.hasCameraPermission(this)) {
            ImageUtils.createTempCameraFile(this)?.let { (file, uri) ->
                takePictureLauncher.launch(uri)
            }
        } else {
            PermissionUtils.requestCameraPermission(this)
        }
    }
    
    private fun selectAudioFromFiles() {
        if (PermissionUtils.hasStoragePermission(this)) {
            pickAudioLauncher.launch("audio/*")
        } else {
            PermissionUtils.requestStoragePermission(this)
        }
    }
    
    private fun handleAudioSelected(uri: Uri) {
        lifecycleScope.launch {
            try {
                val filePath = FileUtils.saveAudioToPrivateStorage(this@NoteEditActivity, uri)
                if (filePath != null) {
                    val duration = FileUtils.getAudioDuration(filePath)
                    val block = NoteBlock(
                        id = UUID.randomUUID().toString(),
                        noteId = noteId,
                        type = BlockType.AUDIO,
                        order = 0,
                        url = filePath,
                        duration = duration
                    )
                    binding.richEditText.insertAudio(block)
                } else {
                    Toast.makeText(this@NoteEditActivity, "音频保存失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "处理音频失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleImageSelected(uri: Uri) {
        lifecycleScope.launch {
            try {
                val filePath = FileUtils.saveImageToPrivateStorage(this@NoteEditActivity, uri)
                if (filePath != null) {
                    val (width, height) = FileUtils.getImageSize(filePath)
                    val block = NoteBlock(
                        id = UUID.randomUUID().toString(),
                        noteId = noteId,
                        type = BlockType.IMAGE,
                        order = 0,
                        url = filePath,
                        alt = "图片",
                        width = width,
                        height = height
                    )
                    binding.richEditText.insertImage(block)
                } else {
                    Toast.makeText(this@NoteEditActivity, "图片保存失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "处理图片失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startRecording() {
        if (PermissionUtils.hasAudioPermission(this)) {
            val filePath = audioRecorder.startRecording()
            if (filePath != null) {
                isRecording = true
                updateRecordingUI(true)
                startRecordingTimer()
            } else {
                Toast.makeText(this, "开始录音失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            PermissionUtils.requestAudioPermission(this)
        }
    }
    
    private fun stopRecording() {
        val (filePath, duration) = audioRecorder.stopRecording()
        isRecording = false
        updateRecordingUI(false)
        stopRecordingTimer()
        
        if (filePath != null) {
            val block = NoteBlock(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                type = BlockType.AUDIO,
                order = 0,
                url = filePath,
                duration = duration
            )
            binding.richEditText.insertAudio(block)
        } else {
            Toast.makeText(this, "录音保存失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun cancelRecording() {
        audioRecorder.cancelRecording()
        isRecording = false
        updateRecordingUI(false)
        stopRecordingTimer()
    }
    
    private fun updateRecordingUI(recording: Boolean) {
        binding.recordingPanel.visibility = if (recording) View.VISIBLE else View.GONE
        binding.btnAddAudio.setImageResource(
            if (recording) android.R.drawable.ic_media_pause 
            else android.R.drawable.ic_btn_speak_now
        )
    }
    
    private fun startRecordingTimer() {
        recordingRunnable = object : Runnable {
            override fun run() {
                if (isRecording) {
                    val duration = audioRecorder.getCurrentDuration()
                    val timeText = String.format("%02d:%02d", duration / 60, duration % 60)
                    binding.tvRecordingTime.text = timeText
                    recordingHandler.postDelayed(this, 1000)
                }
            }
        }
        recordingHandler.post(recordingRunnable!!)
    }
    
    private fun stopRecordingTimer() {
        recordingRunnable?.let { recordingHandler.removeCallbacks(it) }
        recordingRunnable = null
    }
    
    private fun handleMediaClick(block: NoteBlock) {
        when (block.type) {
            BlockType.AUDIO -> {
                block.url?.let { url ->
                    if (audioPlayer.isPrepared()) {
                        if (audioPlayer.isPlaying()) {
                            audioPlayer.pause()
                        } else {
                            audioPlayer.play()
                        }
                    } else {
                        audioPlayer.prepare(url)
                        audioPlayer.play()
                    }
                }
            }
            BlockType.IMAGE -> {
                // 处理图片点击，可以显示大图
            }
            else -> {}
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PermissionUtils.REQUEST_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    Toast.makeText(this, getString(R.string.audio_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
            PermissionUtils.REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageFromGallery()
                } else {
                    Toast.makeText(this, getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
            PermissionUtils.REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        handleNoteBeforeExit()
        if (isRecording) {
            stopRecording()
        }
        audioPlayer.pause()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleNoteBeforeExit()
        super.onBackPressed()
    }
    
    private fun handleNoteBeforeExit() {
        if (isContentEmpty()) {
            // 内容为空，删除记事
            lifecycleScope.launch {
                try {
                    viewModel.deleteNote(noteId)
                } catch (e: Exception) {
                    // 静默处理删除失败
                }
            }
        } else if (checkContentChanged()) {
            // 有内容且有变更，保存记事
            saveNote()
        }
    }
    
    private fun showCategoryDialog() {
        lifecycleScope.launch {
            try {
                val categories = viewModel.getAllCategories()
                val categoryNames = categories.map { it.name }.toMutableList()
                categoryNames.add("+ 自定义分类")
                
                // 找到当前分类的索引
                val currentCategory = categories.find { it.id == currentCategoryId }
                val currentIndex = if (currentCategory != null) {
                    categories.indexOf(currentCategory)
                } else {
                    0 // 默认选择第一个
                }
                
                AlertDialog.Builder(this@NoteEditActivity)
                    .setTitle("选择分类")
                    .setSingleChoiceItems(categoryNames.toTypedArray(), currentIndex) { dialog, which ->
                        if (which == categoryNames.size - 1) {
                            // 选择了"自定义分类"
                            dialog.dismiss()
                            showAddCategoryDialog()
                        } else {
                            // 选择了现有分类
                            val selectedCategory = categories[which]
                            currentCategoryId = selectedCategory.id
                            hasContentChanged = true
                            updateCategoryDisplay()
                            dialog.dismiss()
                            Toast.makeText(this@NoteEditActivity, "已设置分类: ${selectedCategory.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
                    
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "加载分类失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddCategoryDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "输入新分类名称"
            maxLines = 1
        }
        
        AlertDialog.Builder(this)
            .setTitle("新建分类")
            .setView(editText)
            .setPositiveButton("创建") { _, _ ->
                val categoryName = editText.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    createNewCategory(categoryName)
                } else {
                    Toast.makeText(this, "分类名称不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun createNewCategory(categoryName: String) {
        lifecycleScope.launch {
            try {
                val newCategoryId = viewModel.createCategory(categoryName)
                currentCategoryId = newCategoryId
                hasContentChanged = true
                updateCategoryDisplay()
                Toast.makeText(this@NoteEditActivity, "已创建并设置分类: $categoryName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "创建分类失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.cancelRecording()
        audioPlayer.release()
        stopRecordingTimer()
    }
}
