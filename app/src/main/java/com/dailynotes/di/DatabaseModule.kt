package com.dailynotes.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailynotes.data.NoteDatabase
import com.dailynotes.data.NoteDao
import com.dailynotes.data.security.DatabaseSecurity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE notes ADD COLUMN blocks TEXT NOT NULL DEFAULT '[]'")
        }
    }

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            NoteDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }
}