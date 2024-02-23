package com.example.colorphone.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.colorphone.model.CheckList
import com.example.colorphone.util.RepeatType
import com.example.colorphone.util.TypeColorNote

@Entity(tableName = "note_info")
class NoteInfoEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("ids") var ids: Int? = null,
    @ColumnInfo(name = "token") var token: String? = "",
    @ColumnInfo(name = "isUpdate") var isUpdate: String? = "",
    @ColumnInfo(name = "bodyNote") var bodyNote: String = "",
    @ColumnInfo(name = "titleNote") var titleNote: String = "",
    @ColumnInfo(name = "typeItem") var typeItem: String? = null,
    @ColumnInfo(name = "listCheckList") var listCheckList: ArrayList<CheckList>? = null,
    @ColumnInfo(name = "dateNote") var dateNote: Long? = null,
    @ColumnInfo(name = "isPinned") var isPinned: Boolean? = false,
    @ColumnInfo(name = "datePinned") var datePinned: Long? = null,
    @ColumnInfo(name = "type") var typeColor: String = TypeColorNote.BLUE.name,
    @ColumnInfo(name = "isArchive") var isArchive: Boolean? = false,
    @ColumnInfo(name = "modifiedTime") var modifiedTime: Long? = null,
    @ColumnInfo(name = "isDelete") var isDelete: Boolean? = false,
    @ColumnInfo(name = "dateReminder") var dateReminder: Long? = null,
    @ColumnInfo(name = "typeRepeat") var typeRepeat: String? = RepeatType.DOES_NOT_REPEAT.name,
    @ColumnInfo(name = "repeat") var valueRepeat: Int? = null,
    @ColumnInfo(name = "isAlarm") var isAlarm: Boolean? = false,
    @ColumnInfo(name = "lock") var isLock: String? = "0",
)