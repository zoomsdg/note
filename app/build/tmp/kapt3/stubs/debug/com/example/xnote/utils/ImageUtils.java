package com.example.xnote.utils;

import java.lang.System;

/**
 * 图片工具类
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u0004H\u0002J.\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\n2\b\b\u0002\u0010\u0007\u001a\u00020\u00042\b\b\u0002\u0010\b\u001a\u00020\u00042\b\b\u0002\u0010\f\u001a\u00020\u0004J\u001c\u0010\r\u001a\u0010\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000e2\u0006\u0010\u0011\u001a\u00020\u0012J\u001a\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u000e2\u0006\u0010\u000b\u001a\u00020\nJ,\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u00102\b\b\u0002\u0010\u0007\u001a\u00020\u00042\b\b\u0002\u0010\b\u001a\u00020\u0004J\"\u0010\u0017\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u00152\b\b\u0002\u0010\f\u001a\u00020\u0004\u00a8\u0006\u0019"}, d2 = {"Lcom/example/xnote/utils/ImageUtils;", "", "()V", "calculateSampleSize", "", "width", "height", "maxWidth", "maxHeight", "compressImageFile", "", "filePath", "quality", "createTempCameraFile", "Lkotlin/Pair;", "Ljava/io/File;", "Landroid/net/Uri;", "context", "Landroid/content/Context;", "getImageDimensions", "loadBitmapFromUri", "Landroid/graphics/Bitmap;", "uri", "saveBitmapToFile", "bitmap", "app_debug"})
public final class ImageUtils {
    @org.jetbrains.annotations.NotNull
    public static final com.example.xnote.utils.ImageUtils INSTANCE = null;
    
    private ImageUtils() {
        super();
    }
    
    /**
     * 从URI加载位图
     */
    @org.jetbrains.annotations.Nullable
    public final android.graphics.Bitmap loadBitmapFromUri(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.net.Uri uri, int maxWidth, int maxHeight) {
        return null;
    }
    
    /**
     * 保存位图到本地文件
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.String saveBitmapToFile(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.graphics.Bitmap bitmap, int quality) {
        return null;
    }
    
    /**
     * 创建临时拍照文件
     */
    @org.jetbrains.annotations.Nullable
    public final kotlin.Pair<java.io.File, android.net.Uri> createTempCameraFile(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    /**
     * 计算缩放比例
     */
    private final int calculateSampleSize(int width, int height, int maxWidth, int maxHeight) {
        return 0;
    }
    
    /**
     * 获取图片尺寸信息
     */
    @org.jetbrains.annotations.NotNull
    public final kotlin.Pair<java.lang.Integer, java.lang.Integer> getImageDimensions(@org.jetbrains.annotations.NotNull
    java.lang.String filePath) {
        return null;
    }
    
    /**
     * 压缩图片文件
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.String compressImageFile(@org.jetbrains.annotations.NotNull
    java.lang.String filePath, int maxWidth, int maxHeight, int quality) {
        return null;
    }
}