package com.example.colorphone.di

import android.content.Context
import androidx.room.Room
import com.example.colorphone.room.NoteDatabase
import com.example.colorphone.room.dao.NoteInfoDao
import com.example.colorphone.room.dao.NoteTypeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {
    @Singleton
    @Provides
    fun provideNoteDb(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            NoteDatabase.DATABASE_NAME
        )
                .build()
    }

    @Singleton
    @Provides
    fun provideNoteDao(noteDatabase: NoteDatabase): NoteInfoDao {
        return noteDatabase.getNoteInfoDao()
    }

    @Singleton
    @Provides
    fun provideNoteType(noteTypeDatabase: NoteDatabase): NoteTypeDao {
        return noteTypeDatabase.getNoteTypeDao()
    }
}