package com.example.colorphone.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.colorphone.room.dao.NoteInfoDao
import com.example.colorphone.room.entities.NoteInfoEntity
import com.example.colorphone.room.entities.NoteTypeEntity
import com.example.colorphone.room.dao.NoteTypeDao

@Database(entities = [NoteInfoEntity::class, NoteTypeEntity::class], version = 1)
@TypeConverters(DataConverter::class, ColorConverter::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNoteInfoDao(): NoteInfoDao
    abstract fun getNoteTypeDao(): NoteTypeDao

    companion object {
        val DATABASE_NAME = "blog_db"
    }
}