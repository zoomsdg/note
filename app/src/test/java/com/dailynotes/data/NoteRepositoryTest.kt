package com.dailynotes.data

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class NoteRepositoryTest {
    
    @Mock
    private lateinit var noteDao: NoteDao
    
    private lateinit var repository: NoteRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = NoteRepository(noteDao)
    }
    
    @Test
    fun `getAllNotes should return flow from dao`() = runTest {
        val testNotes = listOf(
            NoteEntity(1, "Title 1", "Content 1", "daily", Date(), Date()),
            NoteEntity(2, "Title 2", "Content 2", "work", Date(), Date())
        )
        `when`(noteDao.getAllNotes()).thenReturn(flowOf(testNotes))
        
        val result = repository.getAllNotes().first()
        
        assertEquals(testNotes, result)
        verify(noteDao).getAllNotes()
    }
    
    @Test
    fun `getNotesByCategory should return filtered flow from dao`() = runTest {
        val category = "work"
        val testNotes = listOf(
            NoteEntity(1, "Work Note", "Work Content", "work", Date(), Date())
        )
        `when`(noteDao.getNotesByCategory(category)).thenReturn(flowOf(testNotes))
        
        val result = repository.getNotesByCategory(category).first()
        
        assertEquals(testNotes, result)
        verify(noteDao).getNotesByCategory(category)
    }
    
    @Test
    fun `insertNote should delegate to dao`() = runTest {
        val note = NoteEntity(0, "Test", "Content", "daily", Date(), Date())
        val expectedId = 1L
        `when`(noteDao.insertNote(note)).thenReturn(expectedId)
        
        val result = repository.insertNote(note)
        
        assertEquals(expectedId, result)
        verify(noteDao).insertNote(note)
    }
    
    @Test
    fun `updateNote should delegate to dao`() = runTest {
        val note = NoteEntity(1, "Updated", "Content", "daily", Date(), Date())
        
        repository.updateNote(note)
        
        verify(noteDao).updateNote(note)
    }
    
    @Test
    fun `deleteNote should delegate to dao`() = runTest {
        val note = NoteEntity(1, "Test", "Content", "daily", Date(), Date())
        
        repository.deleteNote(note)
        
        verify(noteDao).deleteNote(note)
    }
}