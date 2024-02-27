package com.example.colorphone.model

import android.os.Parcelable
import com.example.colorphone.util.Const
import com.example.colorphone.util.RepeatType
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.TypeItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteModel(
    var ids: Int? = null,
    var token: String? = null,
    var isUpdate: String? = null,
    var content: String = "",
    var title: String = "",
    var typeItem: String? = TypeItem.TEXT.name,
    var listCheckList: ArrayList<CheckList>? = arrayListOf(),
    var dateCreateNote: Long? = null,
    var isPinned: Boolean? = false,
    var datePinned: Long? = null,
    var typeColor: String = if (Const.currentType == TypeColorNote.DEFAULT.name) TypeColorNote.BLUE.name else Const.currentType,
    var modifiedTime: Long? = null,
    var isArchive: Boolean? = false,
    var isDelete: Boolean? = false,
    var dateReminder: Long? = null,
    var typeRepeat: String? = RepeatType.DOES_NOT_REPEAT.name,
    var repeatValue: Int? = null,
    var isAlarm: Boolean? = false,
    var isLock: String? = "0",
    @Transient var isSelected: Boolean = false,
) : Parcelable {
    fun uniqueWorkName() = "Reminder_$ids"

    fun isLock() = isLock == "1"

    fun changeStatusLock() {
        isLock = if (isLock()) "0" else "1"
    }

    fun isCheckList() = typeItem == TypeItem.CHECK_LIST.name

}