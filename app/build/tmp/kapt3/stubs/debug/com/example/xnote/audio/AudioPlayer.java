package com.example.xnote.audio;

import java.lang.System;

/**
 * 音频播放器
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0013\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0014\u001a\u00020\u000fJ\u0006\u0010\u0015\u001a\u00020\u000fJ\u0006\u0010\u0016\u001a\u00020\u0004J\u0006\u0010\u0007\u001a\u00020\u0004J\u0006\u0010\u0017\u001a\u00020\u0004J\u0006\u0010\u0018\u001a\u00020\u0004J\u000e\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u0006J\u0006\u0010\u001b\u001a\u00020\fJ\u000e\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u000fJ\u0014\u0010\u001e\u001a\u00020\f2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\f0\u000bJ>\u0010 \u001a\u00020\f26\u0010\u001f\u001a2\u0012\u0013\u0012\u00110\u000f\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u000f\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\u0004\u0012\u00020\f0\u000eJ\b\u0010!\u001a\u00020\fH\u0002J\u0006\u0010\"\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R@\u0010\r\u001a4\u0012\u0013\u0012\u00110\u000f\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u000f\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\u0004\u0012\u00020\f\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/example/xnote/audio/AudioPlayer;", "", "()V", "_isPlaying", "", "currentFilePath", "", "isPrepared", "mediaPlayer", "Landroid/media/MediaPlayer;", "onCompletionListener", "Lkotlin/Function0;", "", "onProgressListener", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "current", "total", "getCurrentPosition", "getDuration", "isPlaying", "pause", "play", "prepare", "filePath", "release", "seekTo", "position", "setOnCompletionListener", "listener", "setOnProgressListener", "startProgressTracking", "stop", "app_debug"})
public final class AudioPlayer {
    private android.media.MediaPlayer mediaPlayer;
    private boolean _isPlaying = false;
    private boolean isPrepared = false;
    private java.lang.String currentFilePath;
    private kotlin.jvm.functions.Function0<kotlin.Unit> onCompletionListener;
    private kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Integer, kotlin.Unit> onProgressListener;
    
    public AudioPlayer() {
        super();
    }
    
    /**
     * 设置播放完成监听器
     */
    public final void setOnCompletionListener(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> listener) {
    }
    
    /**
     * 设置播放进度监听器
     */
    public final void setOnProgressListener(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Integer, kotlin.Unit> listener) {
    }
    
    /**
     * 准备播放文件
     */
    public final boolean prepare(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return false;
    }
    
    /**
     * 开始播放
     */
    public final boolean play() {
        return false;
    }
    
    /**
     * 暂停播放
     */
    public final boolean pause() {
        return false;
    }
    
    /**
     * 停止播放
     */
    public final boolean stop() {
        return false;
    }
    
    /**
     * 跳转到指定位置
     */
    public final boolean seekTo(int position) {
        return false;
    }
    
    /**
     * 获取当前播放位置
     */
    public final int getCurrentPosition() {
        return 0;
    }
    
    /**
     * 获取总时长
     */
    public final int getDuration() {
        return 0;
    }
    
    /**
     * 是否正在播放
     */
    public final boolean isPlaying() {
        return false;
    }
    
    /**
     * 是否已准备
     */
    public final boolean isPrepared() {
        return false;
    }
    
    /**
     * 释放资源
     */
    public final void release() {
    }
    
    private final void startProgressTracking() {
    }
}