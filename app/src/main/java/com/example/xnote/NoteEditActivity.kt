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
        binding.toolbar.setNavigationOnClickListener {
            finish()
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
                startRecording()
            }
        }
        
        binding.btnStopRecording.setOnClickListener {
            stopRecording()
        }
        
        binding.btnCancelRecording.setOnClickListener {
            cancelRecording()
        }
        
        // 富文本编辑器设置
        binding.richEditText.setOnContentChangedListener { blocks ->
            // 内容变更时的处理
        }
        
        binding.richEditText.setOnMediaClickListener { block ->
            handleMediaClick(block)
        }
        
        // 文本变更监听
        binding.editTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // 自动保存标题
            }
        })
    }
    
    private fun loadNote() {
        lifecycleScope.launch {
            try {
                currentNote = viewModel.loadNote(noteId)
                currentNote?.let { note ->
                    binding.editTitle.setText(note.note.title)
                    binding.richEditText.loadFromBlocks(note.blocks)
                    binding.toolbar.title = if (note.note.title.isEmpty()) "编辑记事" else note.note.title
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
            viewModel.saveNote(noteId, title, blocks)
        }
    }
    
    private fun showImageOptions() {
        val options = arrayOf("相册选择", "拍照")
        AlertDialog.Builder(this)
            .setTitle("添加图片")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImageFromGallery()
                    1 -> takePhoto()
                }
            }
            .show()
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
        saveNote()
        if (isRecording) {
            stopRecording()
        }
        audioPlayer.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.cancelRecording()
        audioPlayer.release()
        stopRecordingTimer()
    }
}