package com.example.xnote.ui;

import java.lang.System;

/**
 * 富文本编辑器
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 %2\u00020\u0001:\u0001%B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\fJ\"\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\f2\b\b\u0002\u0010\u0017\u001a\u00020\u0007H\u0002J\u000e\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\fJ\"\u0010\u0019\u001a\u00020\u00102\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\f2\b\b\u0002\u0010\u0017\u001a\u00020\u0007H\u0002J\u0014\u0010\u001a\u001a\u00020\u00102\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000fJ\b\u0010\u001c\u001a\u00020\u0010H\u0002J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0016J \u0010!\u001a\u00020\u00102\u0018\u0010\"\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000f\u0012\u0004\u0012\u00020\u00100\u000eJ\u001a\u0010#\u001a\u00020\u00102\u0012\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00100\u000eJ\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\f0\u000fR\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\r\u001a\u0016\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000f\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0011\u001a\u0010\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/example/xnote/ui/RichEditText;", "Landroidx/appcompat/widget/AppCompatEditText;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "blockMap", "", "", "Lcom/example/xnote/data/NoteBlock;", "onContentChangedListener", "Lkotlin/Function1;", "", "", "onMediaClickListener", "insertAudio", "block", "insertAudioPlaceholder", "builder", "Landroid/text/SpannableStringBuilder;", "position", "insertImage", "insertImagePlaceholder", "loadFromBlocks", "blocks", "notifyContentChanged", "onTouchEvent", "", "event", "Landroid/view/MotionEvent;", "setOnContentChangedListener", "listener", "setOnMediaClickListener", "toBlocks", "Companion", "app_debug"})
public final class RichEditText extends androidx.appcompat.widget.AppCompatEditText {
    private final java.util.Map<java.lang.String, com.example.xnote.data.NoteBlock> blockMap = null;
    private kotlin.jvm.functions.Function1<? super java.util.List<com.example.xnote.data.NoteBlock>, kotlin.Unit> onContentChangedListener;
    private kotlin.jvm.functions.Function1<? super com.example.xnote.data.NoteBlock, kotlin.Unit> onMediaClickListener;
    @org.jetbrains.annotations.NotNull
    public static final com.example.xnote.ui.RichEditText.Companion Companion = null;
    private static final char OBJ_REPLACEMENT_CHAR = '\ufffc';
    
    @kotlin.jvm.JvmOverloads
    public RichEditText(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads
    public RichEditText(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads
    public RichEditText(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    /**
     * 设置内容变更监听器
     */
    public final void setOnContentChangedListener(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.util.List<com.example.xnote.data.NoteBlock>, kotlin.Unit> listener) {
    }
    
    /**
     * 设置媒体点击监听器
     */
    public final void setOnMediaClickListener(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.xnote.data.NoteBlock, kotlin.Unit> listener) {
    }
    
    /**
     * 从块列表加载内容
     */
    public final void loadFromBlocks(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.xnote.data.NoteBlock> blocks) {
    }
    
    /**
     * 转换为块列表
     */
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.example.xnote.data.NoteBlock> toBlocks() {
        return null;
    }
    
    /**
     * 在光标位置插入图片
     */
    public final void insertImage(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.NoteBlock block) {
    }
    
    /**
     * 在光标位置插入音频
     */
    public final void insertAudio(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.NoteBlock block) {
    }
    
    private final void insertImagePlaceholder(android.text.SpannableStringBuilder builder, com.example.xnote.data.NoteBlock block, int position) {
    }
    
    private final void insertAudioPlaceholder(android.text.SpannableStringBuilder builder, com.example.xnote.data.NoteBlock block, int position) {
    }
    
    private final void notifyContentChanged() {
    }
    
    @java.lang.Override
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull
    android.view.MotionEvent event) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\f\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/example/xnote/ui/RichEditText$Companion;", "", "()V", "OBJ_REPLACEMENT_CHAR", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}