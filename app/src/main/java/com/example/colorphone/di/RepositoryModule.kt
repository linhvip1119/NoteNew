package com.example.colorphone.di

import com.example.colorphone.repository.MailRepository
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.repository.NoteTypeRepository
import com.example.colorphone.retrofit.ApiRetrofit
import com.example.colorphone.room.dao.NoteInfoDao
import com.example.colorphone.room.mapper.NoteCacheMapper
import com.example.colorphone.room.mapper.NoteTypeMapper
import com.example.colorphone.room.dao.NoteTypeDao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTextNoteRepository(
        noteDao: NoteInfoDao,
        noteCacheMapper: NoteCacheMapper
    ): NoteRepository {
        return NoteRepository(noteCacheMapper, noteDao)
    }

    @Singleton
    @Provides
    fun provideNoteTypeRepository(
        noteDao: NoteTypeDao,
        noteCacheMapper: NoteTypeMapper
    ): NoteTypeRepository {
        return NoteTypeRepository(noteCacheMapper, noteDao)
    }

    @Singleton
    @Provides
    fun provideMailRepository(
        apiRetrofit: ApiRetrofit
    ): MailRepository {
        return MailRepository(apiRetrofit)
    }
}