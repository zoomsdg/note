package com.example.xnote.data;

import java.lang.System;

/**
 * 记事数据访问对象
 */
@androidx.room.Dao
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\t\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u0097@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000fH\'J\u001f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00102\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u001b\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\b\u001a\u00020\u0005H\u0097@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u001b\u0010\u0015\u001a\u0004\u0018\u00010\f2\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00100\u000fH\'J\u0019\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u0019\u001a\u00020\u0012H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aJ\u001f\u0010\u001b\u001a\u00020\u00032\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00120\u0010H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001dJ\u0019\u0010\u001e\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0019\u0010\u001f\u001a\u00020\u00032\u0006\u0010 \u001a\u00020\u0014H\u0097@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010!J\u0019\u0010\"\u001a\u00020\u00032\u0006\u0010\u0019\u001a\u00020\u0012H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aJ\u0019\u0010#\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\r\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006$"}, d2 = {"Lcom/example/xnote/data/NoteDao;", "", "deleteBlock", "", "blockId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBlocksByNoteId", "noteId", "deleteFullNote", "deleteNote", "note", "Lcom/example/xnote/data/Note;", "(Lcom/example/xnote/data/Note;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllNotes", "Lkotlinx/coroutines/flow/Flow;", "", "getBlocksByNoteId", "Lcom/example/xnote/data/NoteBlock;", "getFullNote", "Lcom/example/xnote/data/FullNote;", "getNoteById", "getNoteSummaries", "Lcom/example/xnote/data/NoteSummary;", "insertBlock", "block", "(Lcom/example/xnote/data/NoteBlock;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertBlocks", "blocks", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertNote", "saveFullNote", "fullNote", "(Lcom/example/xnote/data/FullNote;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateBlock", "updateNote", "app_debug"})
public abstract interface NoteDao {
    
    @org.jetbrains.annotations.NotNull
    @androidx.room.Query(value = "SELECT * FROM notes ORDER BY updatedAt DESC")
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.xnote.data.Note>> getAllNotes();
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "SELECT * FROM notes WHERE id = :noteId")
    public abstract java.lang.Object getNoteById(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.xnote.data.Note> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "SELECT * FROM note_blocks WHERE noteId = :noteId ORDER BY `order` ASC")
    public abstract java.lang.Object getBlocksByNoteId(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.xnote.data.NoteBlock>> continuation);
    
    @org.jetbrains.annotations.NotNull
    @androidx.room.Query(value = "\n        SELECT \n        n.id as id,\n        n.title as title,\n        COALESCE(GROUP_CONCAT(CASE WHEN b.type = \'TEXT\' THEN b.text ELSE \'\' END, \' \'), \'\') as preview,\n        n.updatedAt as lastModified,\n        COUNT(b.id) as blockCount\n        FROM notes n \n        LEFT JOIN note_blocks b ON n.id = b.noteId \n        GROUP BY n.id \n        ORDER BY n.updatedAt DESC\n    ")
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.xnote.data.NoteSummary>> getNoteSummaries();
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Insert(onConflict = 1)
    public abstract java.lang.Object insertNote(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.Note note, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Insert(onConflict = 1)
    public abstract java.lang.Object insertBlocks(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.xnote.data.NoteBlock> blocks, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Insert(onConflict = 1)
    public abstract java.lang.Object insertBlock(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.NoteBlock block, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Update
    public abstract java.lang.Object updateNote(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.Note note, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Update
    public abstract java.lang.Object updateBlock(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.NoteBlock block, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Delete
    public abstract java.lang.Object deleteNote(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.Note note, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "DELETE FROM note_blocks WHERE noteId = :noteId")
    public abstract java.lang.Object deleteBlocksByNoteId(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "DELETE FROM note_blocks WHERE id = :blockId")
    public abstract java.lang.Object deleteBlock(@org.jetbrains.annotations.NotNull
    java.lang.String blockId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Transaction
    public abstract java.lang.Object saveFullNote(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.FullNote fullNote, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Transaction
    public abstract java.lang.Object getFullNote(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.xnote.data.FullNote> continuation);
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Transaction
    public abstract java.lang.Object deleteFullNote(@org.jetbrains.annotations.NotNull
    java.lang.String noteId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    /**
     * 记事数据访问对象
     */
    @kotlin.Metadata(mv = {1, 7, 1}, k = 3)
    public final class DefaultImpls {
        
        @org.jetbrains.annotations.Nullable
        @androidx.room.Transaction
        public static java.lang.Object saveFullNote(@org.jetbrains.annotations.NotNull
        com.example.xnote.data.NoteDao $this, @org.jetbrains.annotations.NotNull
        com.example.xnote.data.FullNote fullNote, @org.jetbrains.annotations.NotNull
        kotlin.coroutines.Continuation<? super kotlin.Unit> p2) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        @androidx.room.Transaction
        public static java.lang.Object getFullNote(@org.jetbrains.annotations.NotNull
        com.example.xnote.data.NoteDao $this, @org.jetbrains.annotations.NotNull
        java.lang.String noteId, @org.jetbrains.annotations.NotNull
        kotlin.coroutines.Continuation<? super com.example.xnote.data.FullNote> p2) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        @androidx.room.Transaction
        public static java.lang.Object deleteFullNote(@org.jetbrains.annotations.NotNull
        com.example.xnote.data.NoteDao $this, @org.jetbrains.annotations.NotNull
        java.lang.String noteId, @org.jetbrains.annotations.NotNull
        kotlin.coroutines.Continuation<? super kotlin.Unit> p2) {
            return null;
        }
    }
}