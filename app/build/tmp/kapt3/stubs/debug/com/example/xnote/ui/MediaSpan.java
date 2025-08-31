package com.example.xnote.ui;

import java.lang.System;

/**
 * 媒体占位符基类
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\r\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b&\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\bH&J4\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\t2\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0013"}, d2 = {"Lcom/example/xnote/ui/MediaSpan;", "Landroid/text/style/ReplacementSpan;", "block", "Lcom/example/xnote/data/NoteBlock;", "(Lcom/example/xnote/data/NoteBlock;)V", "getBlock", "()Lcom/example/xnote/data/NoteBlock;", "getDisplaySize", "Lkotlin/Pair;", "", "getSize", "paint", "Landroid/graphics/Paint;", "text", "", "start", "end", "fm", "Landroid/graphics/Paint$FontMetricsInt;", "app_debug"})
public abstract class MediaSpan extends android.text.style.ReplacementSpan {
    @org.jetbrains.annotations.NotNull
    private final com.example.xnote.data.NoteBlock block = null;
    
    public MediaSpan(@org.jetbrains.annotations.NotNull
    com.example.xnote.data.NoteBlock block) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.xnote.data.NoteBlock getBlock() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlin.Pair<java.lang.Integer, java.lang.Integer> getDisplaySize();
    
    @java.lang.Override
    public int getSize(@org.jetbrains.annotations.NotNull
    android.graphics.Paint paint, @org.jetbrains.annotations.Nullable
    java.lang.CharSequence text, int start, int end, @org.jetbrains.annotations.Nullable
    android.graphics.Paint.FontMetricsInt fm) {
        return 0;
    }
}