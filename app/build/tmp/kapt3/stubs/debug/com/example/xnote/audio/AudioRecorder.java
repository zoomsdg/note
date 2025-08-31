package com.example.xnote.audio;

import java.lang.System;

/**
 * 音频录制器
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\r\u001a\u00020\u000eJ\b\u0010\u000f\u001a\u00020\u000eH\u0002J\u0006\u0010\u0010\u001a\u00020\fJ\u0006\u0010\u0005\u001a\u00020\u0006J\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012J\u0014\u0010\u0013\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0012\u0012\u0004\u0012\u00020\f0\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/example/xnote/audio/AudioRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "isRecording", "", "mediaRecorder", "Landroid/media/MediaRecorder;", "outputFile", "Ljava/io/File;", "startTime", "", "cancelRecording", "", "cleanup", "getCurrentDuration", "startRecording", "", "stopRecording", "Lkotlin/Pair;", "app_debug"})
public final class AudioRecorder {
    private final android.content.Context context = null;
    private android.media.MediaRecorder mediaRecorder;
    private java.io.File outputFile;
    private boolean isRecording = false;
    private long startTime = 0L;
    
    public AudioRecorder(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    /**
     * 开始录音
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.String startRecording() {
        return null;
    }
    
    /**
     * 停止录音
     */
    @org.jetbrains.annotations.NotNull
    public final kotlin.Pair<java.lang.String, java.lang.Long> stopRecording() {
        return null;
    }
    
    /**
     * 取消录音
     */
    public final void cancelRecording() {
    }
    
    /**
     * 获取当前录音时长（秒）
     */
    public final long getCurrentDuration() {
        return 0L;
    }
    
    /**
     * 是否正在录音
     */
    public final boolean isRecording() {
        return false;
    }
    
    private final void cleanup() {
    }
}