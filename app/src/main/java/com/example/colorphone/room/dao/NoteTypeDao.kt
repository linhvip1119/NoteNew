package com.example.colorphone.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.colorphone.room.entities.NoteTypeEntity

@Dao
interface NoteTypeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: NoteTypeEntity)

    @Update
    suspend fun update(note: NoteTypeEntity)

    @Update
    suspend fun updateList(note: List<NoteTypeEntity>)

    @Delete
    suspend fun delete(note: NoteTypeEntity)

    @Query("SELECT * FROM type_table")
    suspend fun getAllNote(): List<NoteTypeEntity>

}