package com.example.xnote.viewmodel;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u0019B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0010J\u001b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0010J$\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u000f2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001a"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/example/xnote/repository/NoteRepository;", "(Lcom/example/xnote/repository/NoteRepository;)V", "_saveState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "saveState", "Lkotlinx/coroutines/flow/StateFlow;", "getSaveState", "()Lkotlinx/coroutines/flow/StateFlow;", "deleteNote", "", "noteId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadNote", "Lcom/example/xnote/data/FullNote;", "saveNote", "", "title", "blocks", "", "Lcom/example/xnote/data/NoteBlock;", "SaveState", "app_debug"})
public final class NoteEditViewModel extends androidx.lifecycle.ViewModel {
    private final com.example.xnote.repository.NoteRepository repository = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.xnote.viewmodel.NoteEditViewModel.SaveState> _saveState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.xnote.viewmodel.NoteEditViewModel.SaveState> saveState = null;
    
    public NoteEditViewModel(@org.jetbrains.annotations.NotNull
    com.example.xnote.repository.NoteRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.example.xnote.viewmodel.NoteEditViewModel.SaveState> getSaveState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadNote(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.xnote.data.FullNote> continuation) {
        return null;
    }
    
    public final void saveNote(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.NotNull
    java.util.List<com.example.xnote.data.NoteBlock> blocks) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteNote(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> continuation) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "", "()V", "Error", "Idle", "Saving", "Success", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Error;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Idle;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Saving;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Success;", "app_debug"})
    public static abstract class SaveState {
        
        private SaveState() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Idle;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "()V", "app_debug"})
        public static final class Idle extends com.example.xnote.viewmodel.NoteEditViewModel.SaveState {
            @org.jetbrains.annotations.NotNull
            public static final com.example.xnote.viewmodel.NoteEditViewModel.SaveState.Idle INSTANCE = null;
            
            private Idle() {
                super();
            }
        }
        
        @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Saving;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "()V", "app_debug"})
        public static final class Saving extends com.example.xnote.viewmodel.NoteEditViewModel.SaveState {
            @org.jetbrains.annotations.NotNull
            public static final com.example.xnote.viewmodel.NoteEditViewModel.SaveState.Saving INSTANCE = null;
            
            private Saving() {
                super();
            }
        }
        
        @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Success;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "()V", "app_debug"})
        public static final class Success extends com.example.xnote.viewmodel.NoteEditViewModel.SaveState {
            @org.jetbrains.annotations.NotNull
            public static final com.example.xnote.viewmodel.NoteEditViewModel.SaveState.Success INSTANCE = null;
            
            private Success() {
                super();
            }
        }
        
        @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState$Error;", "Lcom/example/xnote/viewmodel/NoteEditViewModel$SaveState;", "message", "", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
        public static final class Error extends com.example.xnote.viewmodel.NoteEditViewModel.SaveState {
            @org.jetbrains.annotations.NotNull
            private final java.lang.String message = null;
            
            @org.jetbrains.annotations.NotNull
            public final com.example.xnote.viewmodel.NoteEditViewModel.SaveState.Error copy(@org.jetbrains.annotations.NotNull
            java.lang.String message) {
                return null;
            }
            
            @java.lang.Override
            public boolean equals(@org.jetbrains.annotations.Nullable
            java.lang.Object other) {
                return false;
            }
            
            @java.lang.Override
            public int hashCode() {
                return 0;
            }
            
            @org.jetbrains.annotations.NotNull
            @java.lang.Override
            public java.lang.String toString() {
                return null;
            }
            
            public Error(@org.jetbrains.annotations.NotNull
            java.lang.String message) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull
            public final java.lang.String getMessage() {
                return null;
            }
        }
    }
}