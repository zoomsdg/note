package com.example.xnote;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u000e\u0018\u0000 @2\u00020\u0001:\u0001@B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\u0017H\u0002J\u0010\u0010\"\u001a\u00020\u001f2\u0006\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020\u001fH\u0002J\b\u0010&\u001a\u00020\u001fH\u0002J\b\u0010\'\u001a\u00020\u001fH\u0002J\u0012\u0010(\u001a\u00020\u001f2\b\u0010)\u001a\u0004\u0018\u00010*H\u0014J\b\u0010+\u001a\u00020\u001fH\u0014J\b\u0010,\u001a\u00020\u001fH\u0014J-\u0010-\u001a\u00020\u001f2\u0006\u0010.\u001a\u00020/2\u000e\u00100\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u000e012\u0006\u00102\u001a\u000203H\u0016\u00a2\u0006\u0002\u00104J\b\u00105\u001a\u00020\u001fH\u0002J\b\u00106\u001a\u00020\u001fH\u0002J\b\u00107\u001a\u00020\u001fH\u0002J\b\u00108\u001a\u00020\u001fH\u0002J\b\u00109\u001a\u00020\u001fH\u0002J\b\u0010:\u001a\u00020\u001fH\u0002J\b\u0010;\u001a\u00020\u001fH\u0002J\b\u0010<\u001a\u00020\u001fH\u0002J\b\u0010=\u001a\u00020\u001fH\u0002J\u0010\u0010>\u001a\u00020\u001f2\u0006\u0010?\u001a\u00020\fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000f\u001a\u0010\u0012\f\u0012\n \u0011*\u0004\u0018\u00010\u000e0\u000e0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0016\u001a\u0010\u0012\f\u0012\n \u0011*\u0004\u0018\u00010\u00170\u00170\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0018\u001a\u00020\u00198BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\u001d\u001a\u0004\b\u001a\u0010\u001b\u00a8\u0006A"}, d2 = {"Lcom/example/xnote/NoteEditActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "audioPlayer", "Lcom/example/xnote/audio/AudioPlayer;", "audioRecorder", "Lcom/example/xnote/audio/AudioRecorder;", "binding", "Lcom/example/xnote/databinding/ActivityNoteEditBinding;", "currentNote", "Lcom/example/xnote/data/FullNote;", "isRecording", "", "noteId", "", "pickImageLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "kotlin.jvm.PlatformType", "recordingHandler", "Landroid/os/Handler;", "recordingRunnable", "Ljava/lang/Runnable;", "takePictureLauncher", "Landroid/net/Uri;", "viewModel", "Lcom/example/xnote/viewmodel/NoteEditViewModel;", "getViewModel", "()Lcom/example/xnote/viewmodel/NoteEditViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "cancelRecording", "", "handleImageSelected", "uri", "handleMediaClick", "block", "Lcom/example/xnote/data/NoteBlock;", "initComponents", "loadNote", "observeViewModel", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onRequestPermissionsResult", "requestCode", "", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "saveNote", "selectImageFromGallery", "setupUI", "showImageOptions", "startRecording", "startRecordingTimer", "stopRecording", "stopRecordingTimer", "takePhoto", "updateRecordingUI", "recording", "Companion", "app_debug"})
public final class NoteEditActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull
    public static final com.example.xnote.NoteEditActivity.Companion Companion = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String EXTRA_NOTE_ID = "note_id";
    private com.example.xnote.databinding.ActivityNoteEditBinding binding;
    private java.lang.String noteId;
    private com.example.xnote.data.FullNote currentNote;
    private final kotlin.Lazy viewModel$delegate = null;
    private com.example.xnote.audio.AudioRecorder audioRecorder;
    private com.example.xnote.audio.AudioPlayer audioPlayer;
    private boolean isRecording = false;
    private final android.os.Handler recordingHandler = null;
    private java.lang.Runnable recordingRunnable;
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> pickImageLauncher = null;
    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePictureLauncher = null;
    
    public NoteEditActivity() {
        super();
    }
    
    private final com.example.xnote.viewmodel.NoteEditViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initComponents() {
    }
    
    private final void setupUI() {
    }
    
    private final void loadNote() {
    }
    
    private final void observeViewModel() {
    }
    
    private final void saveNote() {
    }
    
    private final void showImageOptions() {
    }
    
    private final void selectImageFromGallery() {
    }
    
    private final void takePhoto() {
    }
    
    private final void handleImageSelected(android.net.Uri uri) {
    }
    
    private final void startRecording() {
    }
    
    private final void stopRecording() {
    }
    
    private final void cancelRecording() {
    }
    
    private final void updateRecordingUI(boolean recording) {
    }
    
    private final void startRecordingTimer() {
    }
    
    private final void stopRecordingTimer() {
    }
    
    private final void handleMediaClick(com.example.xnote.data.NoteBlock block) {
    }
    
    @java.lang.Override
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull
    int[] grantResults) {
    }
    
    @java.lang.Override
    protected void onPause() {
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/example/xnote/NoteEditActivity$Companion;", "", "()V", "EXTRA_NOTE_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}