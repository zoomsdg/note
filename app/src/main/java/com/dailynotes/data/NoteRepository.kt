package com.dailynotes.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)
    
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = 
        noteDao.getNotesByCategory(category)
    
    fun searchNotes(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotes("%$query%")
    
    fun getAllCategories(): Flow<List<String>> = noteDao.getAllCategories()
    
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun deleteAllNotes() = noteDao.deleteAllNotes()
}