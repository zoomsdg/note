package com.example.xnote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 记事数据访问对象
 */
@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?
    
    @Query("SELECT * FROM note_blocks WHERE noteId = :noteId ORDER BY `order` ASC")
    suspend fun getBlocksByNoteId(noteId: String): List<NoteBlock>
    
    @Query("""
        SELECT 
        n.id as id,
        n.title as title,
        COALESCE(GROUP_CONCAT(CASE WHEN b.type = 'TEXT' THEN b.text ELSE '' END, ' '), '') as preview,
        n.updatedAt as lastModified,
        COUNT(b.id) as blockCount
        FROM notes n 
        LEFT JOIN note_blocks b ON n.id = b.noteId 
        GROUP BY n.id 
        ORDER BY n.updatedAt DESC
    """)
    fun getNoteSummaries(): Flow<List<NoteSummary>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<NoteBlock>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: NoteBlock)
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Update
    suspend fun updateBlock(block: NoteBlock)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM note_blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksByNoteId(noteId: String)
    
    @Query("DELETE FROM note_blocks WHERE id = :blockId")
    suspend fun deleteBlock(blockId: String)
    
    @Transaction
    suspend fun saveFullNote(fullNote: FullNote) {
        insertNote(fullNote.note)
        deleteBlocksByNoteId(fullNote.note.id)
        insertBlocks(fullNote.blocks)
    }
    
    @Transaction
    suspend fun getFullNote(noteId: String): FullNote? {
        val note = getNoteById(noteId) ?: return null
        val blocks = getBlocksByNoteId(noteId)
        return FullNote(note, blocks)
    }
    
    @Transaction
    suspend fun deleteFullNote(noteId: String) {
        val note = getNoteById(noteId)
        if (note != null) {
            deleteNote(note)
            deleteBlocksByNoteId(noteId)
        }
    }
}