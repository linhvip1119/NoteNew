package com.example.colorphone.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.colorphone.model.ColorItem

@Entity(tableName = "type_table")
class NoteTypeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("ids")
    var id: Int? = null,
    @ColumnInfo("listColor")
    val listColor: ArrayList<ColorItem>? = null,
)