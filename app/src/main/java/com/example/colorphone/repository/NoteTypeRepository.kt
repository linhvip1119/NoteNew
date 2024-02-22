package com.example.colorphone.repository

import com.example.colorphone.model.NoteType
import com.example.colorphone.room.mapper.NoteTypeMapper
import com.example.colorphone.util.DataState
import com.example.colorphone.room.dao.NoteTypeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NoteTypeRepository(
    private val noteCacheMapper: NoteTypeMapper,
    private val type: NoteTypeDao
) {

    suspend fun getAllType(): Flow<DataState<List<NoteType>>> = flow {
        emit(DataState.Loading)
        try {
            val cacheNote = type.getAllNote()
            emit(DataState.Success(noteCacheMapper.mapFromListEntity(cacheNote)))
        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun updateType(textNote: NoteType) = type.update(noteCacheMapper.mapToEntity(textNote))

    suspend fun updateListType(list : List<NoteType>) = type.updateList(noteCacheMapper.mapFromListLocal(list))

    suspend fun addType(textNote: NoteType) =
        type.insertNote(noteCacheMapper.mapToEntity(textNote))
}