package com.dailynotes.data

import androidx.room.*
import javax.inject.Singleton

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        const val DATABASE_NAME = "note_database"
    }
}