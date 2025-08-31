package com.example.xnote.utils;

import java.lang.System;

/**
 * 文件工具类
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\t\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u000b2\u0006\u0010\u0005\u001a\u00020\u0006J\u0018\u0010\r\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0006J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/example/xnote/utils/FileUtils;", "", "()V", "deleteFile", "", "filePath", "", "getAudioDuration", "", "getFileSize", "getImageSize", "Lkotlin/Pair;", "", "saveAudioToPrivateStorage", "context", "Landroid/content/Context;", "sourcePath", "saveImageToPrivateStorage", "uri", "Landroid/net/Uri;", "app_debug"})
public final class FileUtils {
    @org.jetbrains.annotations.NotNull
    public static final com.example.xnote.utils.FileUtils INSTANCE = null;
    
    private FileUtils() {
        super();
    }
    
    /**
     * 保存图片到应用私有目录
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.String saveImageToPrivateStorage(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * 保存音频到应用私有目录
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.String saveAudioToPrivateStorage(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String sourcePath) {
        return null;
    }
    
    /**
     * 获取音频时长
     */
    public final long getAudioDuration(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return 0L;
    }
    
    /**
     * 获取图片尺寸
     */
    @org.jetbrains.annotations.NotNull
    public final kotlin.Pair<java.lang.Integer, java.lang.Integer> getImageSize(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return null;
    }
    
    /**
     * 删除文件
     */
    public final boolean deleteFile(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return false;
    }
    
    /**
     * 获取文件大小（字节）
     */
    public final long getFileSize(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return 0L;
    }
}