package com.example.colorphone.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.colorphone.room.entities.NoteInfoEntity

@Dao
interface NoteInfoDao {

    @RawQuery
    fun query(query: SupportSQLiteQuery): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(baseNotes: List<NoteInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: NoteInfoEntity) : Long

    @Update
    suspend fun update(note: NoteInfoEntity)

    @Query("SELECT * FROM note_info")
    suspend fun getAllData(): List<NoteInfoEntity>

    @Query("SELECT * FROM note_info WHERE typeItem = :keyNote")
    suspend fun getAllNote(keyNote: String): List<NoteInfoEntity>

    @Query("SELECT * FROM note_info WHERE ids = :ids")
    fun getNoteWithIds(ids: Int): List<NoteInfoEntity>

    @Query("SELECT * FROM note_info WHERE ids = :ids")
    fun getNoteWithIdsWidget(ids: Int): LiveData<List<NoteInfoEntity>>

    @Query("SELECT * FROM note_info WHERE isDelete = :isDelete")
    suspend fun getListRecycleBin(isDelete: Boolean = true): List<NoteInfoEntity>

    @Query("SELECT * FROM note_info WHERE isArchive = :isArchive")
    suspend fun getListArchive(isArchive: Boolean = true): List<NoteInfoEntity>

    @Query("SELECT * FROM note_info where isPinned =:isPinnedTop")
    fun getNotePinned(isPinnedTop: Boolean): List<NoteInfoEntity>

    @Query("DELETE FROM note_info WHERE ids = :id")
    suspend fun delete(id: Int)
}