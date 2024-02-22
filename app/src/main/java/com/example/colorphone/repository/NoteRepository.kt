package com.example.colorphone.repository

import android.util.Log
import com.example.colorphone.model.NoteModel
import com.example.colorphone.model.Post
import com.example.colorphone.retrofit.ApiRetrofit
import com.example.colorphone.retrofit.NetWorkMapper
import com.example.colorphone.room.dao.NoteInfoDao
import com.example.colorphone.room.mapper.NoteCacheMapper
import com.example.colorphone.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NoteRepository(
    private val noteCacheMapper: NoteCacheMapper,
    private val noteInfoDao: NoteInfoDao
) {

    suspend fun getAllNote(key: String): Flow<DataState<List<NoteModel>>> = flow {
        emit(DataState.Loading)
        try {
            val cacheNote = noteInfoDao.getAllNote(key)
            emit(DataState.Success(noteCacheMapper.mapFromListEntity(cacheNote)))
        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun getNoteWithIds(ids: Int): Flow<DataState<List<NoteModel>>> = flow {
        emit(DataState.Loading)
        try {
            val cacheNote = noteInfoDao.getNoteWithIds(ids)
            emit(DataState.Success(noteCacheMapper.mapFromListEntity(cacheNote)))
        } catch (e: Exception) {
            try {
                emit(DataState.Error(e))
            } catch (e: Exception) {
                Log.i("TAG", "getNoteWithIds: $e")
            }
        }
    }

    suspend fun getAllData(): Flow<DataState<List<NoteModel>>> = flow {
        emit(DataState.Loading)
        try {
            val cacheNote = noteInfoDao.getAllData()
            emit(DataState.Success(noteCacheMapper.mapFromListEntity(cacheNote)))
        } catch (e: Exception) {
            try {
                emit(DataState.Error(e))
            } catch (e: Exception) {
                Log.i("TAG", "getNoteWithIds: $e")
            }
        }
    }

    suspend fun getListRecycleArchive(isListArchive: Boolean): Flow<DataState<List<NoteModel>>> =
        flow {
            emit(DataState.Loading)
            try {
                val cacheNote =
                    if (isListArchive) noteInfoDao.getListArchive() else noteInfoDao.getListRecycleBin()
                emit(DataState.Success(noteCacheMapper.mapFromListEntity(cacheNote)))
            } catch (e: Exception) {
                emit(DataState.Error(e))
            }
        }

    suspend fun addNote(textNote: NoteModel, newId: (Int) -> Unit) {
        newId.invoke(noteInfoDao.insertNote(noteCacheMapper.mapToEntity(textNote)).toInt())

    }

    suspend fun updateNote(textNote: NoteModel) =
        noteInfoDao.update(noteCacheMapper.mapToEntity(textNote))

    suspend fun deleteNote(id: Int) =
        noteInfoDao.delete(id)
}