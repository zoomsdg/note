package com.example.xnote.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.example.xnote.data.BlockType
import com.example.xnote.data.NoteBlock
import com.example.xnote.utils.SecurityLog
import java.util.*

/**
 * 富文本编辑器
 */
class RichEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    
    private val blockMap = mutableMapOf<String, NoteBlock>()
    private var onContentChangedListener: ((List<NoteBlock>) -> Unit)? = null
    private var onMediaClickListener: ((NoteBlock) -> Unit)? = null
    
    companion object {
        private const val OBJ_REPLACEMENT_CHAR = '\uFFFC' // 对象替换字符
    }
    
    init {
        // 设置文本监听器
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                notifyContentChanged()
            }
        })
    }
    
    /**
     * 设置内容变更监听器
     */
    fun setOnContentChangedListener(listener: (List<NoteBlock>) -> Unit) {
        onContentChangedListener = listener
    }
    
    /**
     * 设置媒体点击监听器
     */
    fun setOnMediaClickListener(listener: (NoteBlock) -> Unit) {
        onMediaClickListener = listener
    }
    
    /**
     * 从块列表加载内容
     */
    fun loadFromBlocks(blocks: List<NoteBlock>) {
        blockMap.clear()
        val builder = SpannableStringBuilder()
        
        for (block in blocks) {
            when (block.type) {
                BlockType.TEXT -> {
                    builder.append(block.text ?: "")
                }
                BlockType.IMAGE -> {
                    insertImagePlaceholder(builder, block)
                }
                BlockType.AUDIO -> {
                    insertAudioPlaceholder(builder, block)
                }
            }
        }
        
        setText(builder)
    }
    
    /**
     * 转换为块列表
     */
    fun toBlocks(): List<NoteBlock> {
        val blocks = mutableListOf<NoteBlock>()
        val text = text ?: return blocks
        val spans = text.getSpans(0, text.length, MediaSpan::class.java)
        
        var currentPos = 0
        var order = 0
        
        for (span in spans.sortedBy { text.getSpanStart(it) }) {
            val spanStart = text.getSpanStart(span)
            
            // 添加span前的文本
            if (currentPos < spanStart) {
                val textContent = text.substring(currentPos, spanStart)
                if (textContent.isNotEmpty()) {
                    blocks.add(NoteBlock(
                        id = UUID.randomUUID().toString(),
                        noteId = "",
                        type = BlockType.TEXT,
                        order = order++,
                        text = textContent
                    ))
                }
            }
            
            // 添加媒体块
            blocks.add(span.block.copy(order = order++))
            currentPos = text.getSpanEnd(span)
        }
        
        // 添加剩余文本
        if (currentPos < text.length) {
            val textContent = text.substring(currentPos)
            if (textContent.isNotEmpty()) {
                blocks.add(NoteBlock(
                    id = UUID.randomUUID().toString(),
                    noteId = "",
                    type = BlockType.TEXT,
                    order = order,
                    text = textContent
                ))
            }
        }
        
        return blocks
    }
    
    /**
     * 在光标位置插入图片
     */
    fun insertImage(block: NoteBlock) {
        val start = selectionStart
        val builder = SpannableStringBuilder(text)
        
        insertImagePlaceholder(builder, block, start)
        setText(builder)
        setSelection(start + 1)
        
        // 强制刷新显示
        post {
            invalidate()
            requestLayout()
        }
        
        notifyContentChanged()
    }
    
    /**
     * 在光标位置插入音频
     */
    fun insertAudio(block: NoteBlock) {
        val start = selectionStart
        val builder = SpannableStringBuilder(text)
        
        insertAudioPlaceholder(builder, block, start)
        setText(builder)
        setSelection(start + 1)
        
        notifyContentChanged()
    }
    
    private fun insertImagePlaceholder(builder: SpannableStringBuilder, block: NoteBlock, position: Int = builder.length) {
        val span = ImageMediaSpan(context, block)
        blockMap[block.id] = block
        
        builder.insert(position, OBJ_REPLACEMENT_CHAR.toString())
        builder.setSpan(span, position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    
    private fun insertAudioPlaceholder(builder: SpannableStringBuilder, block: NoteBlock, position: Int = builder.length) {
        val span = AudioMediaSpan(context, block)
        blockMap[block.id] = block
        
        builder.insert(position, OBJ_REPLACEMENT_CHAR.toString())
        builder.setSpan(span, position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    
    private fun notifyContentChanged() {
        onContentChangedListener?.invoke(toBlocks())
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val offset = getOffsetForPosition(event.x, event.y)
            val spans = text?.getSpans(offset, offset, MediaSpan::class.java)
            
            spans?.firstOrNull()?.let { span ->
                onMediaClickListener?.invoke(span.block)
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
}

/**
 * 媒体占位符基类
 */
abstract class MediaSpan(val block: NoteBlock) : ReplacementSpan() {
    
    abstract fun getDisplaySize(): Pair<Int, Int>
    
    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val (width, height) = getDisplaySize()
        fm?.let {
            it.ascent = -height
            it.descent = 0
            it.top = it.ascent
            it.bottom = it.descent
        }
        return width
    }
}

/**
 * 图片占位符
 */
class ImageMediaSpan(
    private val context: Context,
    block: NoteBlock
) : MediaSpan(block) {
    
    private var imageBitmap: Bitmap? = null
    private var displayWidth = 200
    private var displayHeight = 150
    
    init {
        loadImage()
    }
    
    private fun loadImage() {
        block.url?.let { imagePath ->
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(imagePath, options)
                
                // 计算缩放比例
                val maxWidth = 300
                val maxHeight = 200
                val scaleFactor = calculateScaleFactor(options.outWidth, options.outHeight, maxWidth, maxHeight)
                
                // 加载实际图片
                val actualOptions = BitmapFactory.Options().apply {
                    inSampleSize = scaleFactor
                }
                imageBitmap = BitmapFactory.decodeFile(imagePath, actualOptions)
                
                // 更新显示尺寸
                imageBitmap?.let { bitmap ->
                    displayWidth = bitmap.width
                    displayHeight = bitmap.height
                }
            } catch (e: Exception) {
                SecurityLog.e("RichEditText", "Failed to load image for display", e)
                // 加载失败时使用默认尺寸
                displayWidth = 200
                displayHeight = 150
            }
        }
    }
    
    private fun calculateScaleFactor(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        var scaleFactor = 1
        
        if (width > maxWidth || height > maxHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            
            while (halfWidth / scaleFactor >= maxWidth && halfHeight / scaleFactor >= maxHeight) {
                scaleFactor *= 2
            }
        }
        
        return scaleFactor
    }
    
    override fun getDisplaySize(): Pair<Int, Int> {
        return Pair(displayWidth, displayHeight)
    }
    
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        canvas.save()
        canvas.translate(x, top.toFloat())
        
        val bitmap = imageBitmap
        if (bitmap != null && !bitmap.isRecycled) {
            // 绘制实际图片
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        } else {
            // 如果图片加载失败，绘制占位符
            val placeholder = context.getDrawable(android.R.drawable.ic_menu_gallery)
            placeholder?.setBounds(0, 0, displayWidth, displayHeight)
            placeholder?.draw(canvas)
        }
        
        // 绘制边框
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.GRAY
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, displayWidth.toFloat(), displayHeight.toFloat(), borderPaint)
        
        canvas.restore()
    }
}

/**
 * 音频占位符
 */
class AudioMediaSpan(
    private val context: Context,
    block: NoteBlock
) : MediaSpan(block) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 14f * context.resources.displayMetrics.density
    }
    
    override fun getDisplaySize(): Pair<Int, Int> {
        return Pair(400, 80)
    }
    
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        canvas.save()
        canvas.translate(x, top.toFloat())
        
        // 绘制音频背景
        this.paint.style = Paint.Style.FILL
        this.paint.color = Color.parseColor("#FFF3E0")
        canvas.drawRoundRect(0f, 0f, 400f, 80f, 10f, 10f, this.paint)
        
        // 绘制播放按钮
        this.paint.color = Color.parseColor("#FF9800")
        val centerY = 40f
        val playButton = Path().apply {
            moveTo(25f, centerY - 15f)
            lineTo(50f, centerY)
            lineTo(25f, centerY + 15f)
            close()
        }
        canvas.drawPath(playButton, this.paint)
        
        // 绘制时长
        this.paint.color = Color.BLACK
        this.paint.style = Paint.Style.FILL
        this.paint.textSize = 16f * context.resources.displayMetrics.density
        val duration = block.duration ?: 0
        val durationText = String.format("%02d:%02d", duration / 60, duration % 60)
        canvas.drawText(durationText, 80f, centerY + 6f, this.paint)
        
        // 绘制波形线（简化）
        this.paint.strokeWidth = 2f
        this.paint.style = Paint.Style.STROKE
        this.paint.color = Color.parseColor("#FFCC80")
        for (i in 0..20) {
            val x1 = 120f + i * 8f
            val height = (Math.random() * 20 + 5).toFloat()
            canvas.drawLine(x1, centerY - height/2, x1, centerY + height/2, this.paint)
        }
        
        canvas.restore()
    }
}