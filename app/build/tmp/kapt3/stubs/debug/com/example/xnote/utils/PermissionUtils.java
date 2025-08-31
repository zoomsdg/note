package com.example.xnote.utils;

import java.lang.System;

/**
 * 权限工具类
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\r\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/example/xnote/utils/PermissionUtils;", "", "()V", "REQUEST_AUDIO_PERMISSION", "", "REQUEST_CAMERA_PERMISSION", "REQUEST_STORAGE_PERMISSION", "hasAllPermissions", "", "context", "Landroid/content/Context;", "hasAudioPermission", "hasCameraPermission", "hasStoragePermission", "requestAllPermissions", "", "activity", "Landroid/app/Activity;", "requestAudioPermission", "requestCameraPermission", "requestStoragePermission", "app_debug"})
public final class PermissionUtils {
    @org.jetbrains.annotations.NotNull
    public static final com.example.xnote.utils.PermissionUtils INSTANCE = null;
    public static final int REQUEST_AUDIO_PERMISSION = 1001;
    public static final int REQUEST_STORAGE_PERMISSION = 1002;
    public static final int REQUEST_CAMERA_PERMISSION = 1003;
    
    private PermissionUtils() {
        super();
    }
    
    /**
     * 检查录音权限
     */
    public final boolean hasAudioPermission(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * 请求录音权限
     */
    public final void requestAudioPermission(@org.jetbrains.annotations.NotNull
    android.app.Activity activity) {
    }
    
    /**
     * 检查存储权限
     */
    public final boolean hasStoragePermission(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * 请求存储权限
     */
    public final void requestStoragePermission(@org.jetbrains.annotations.NotNull
    android.app.Activity activity) {
    }
    
    /**
     * 检查相机权限
     */
    public final boolean hasCameraPermission(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * 请求相机权限
     */
    public final void requestCameraPermission(@org.jetbrains.annotations.NotNull
    android.app.Activity activity) {
    }
    
    /**
     * 检查所有需要的权限
     */
    public final boolean hasAllPermissions(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * 请求所有权限
     */
    public final void requestAllPermissions(@org.jetbrains.annotations.NotNull
    android.app.Activity activity) {
    }
}