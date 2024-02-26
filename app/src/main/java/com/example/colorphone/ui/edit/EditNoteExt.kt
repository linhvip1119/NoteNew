package com.example.colorphone.ui.edit

import android.util.Log
import androidx.core.view.isVisible
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.edit.adapter.MakeListVH
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.getCurrentTimeToLong
import com.example.colorphone.util.isNotNullOfEmpty
import com.example.colorphone.util.showKeyboard
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun EditNoteScreen.getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}

fun EditNoteScreen.moveToNext(currentPosition: Int) {
    val viewHolder = binding.rvCheckList.findViewHolderForAdapterPosition(currentPosition + 1) as MakeListVH?
    if (viewHolder != null) {
        if (viewHolder.binding.checkBox.isChecked) {
            moveToNext(currentPosition + 1)
        } else viewHolder.binding.editText.requestFocus()
    } else addListItem()
}

fun EditNoteScreen.addListItem(isShowKeyBoard: Boolean = false) {
    val position = model?.listCheckList?.size
    val checkList = CheckList(String(), false, false, getRandomString(50))
    model?.listCheckList?.add(checkList)
    position?.let {
        adapter.notifyItemInserted(it)
        binding.rvCheckList.post {
            val viewHolder = binding.rvCheckList.findViewHolderForAdapterPosition(it) as MakeListVH?
            viewHolder?.binding?.editText?.requestFocus()
            if (isShowKeyBoard) {
                activity?.showKeyboard()
            }
        }
    }
}

fun EditNoteScreen.handleViewText() {
    binding.etContent.isVisible = isNoteText
    binding.clMakeList.isVisible = !isNoteText
}

fun EditNoteScreen.getDataNote(
    update: String, isUpdate: String, typeNote: String, isCheck: Boolean = false
): NoteModel {
    val title = binding.etTittle.text.toString()
    val content = binding.etContent.text.toString()
    val dateNote = model?.dateCreateNote ?: getCurrentTimeToLong()
    val isPinned = model?.isPinned ?: false
    val modifiedTime = if (!isCheck) getCurrentTimeToLong() else model?.modifiedTime
    val items = model?.listCheckList?.filter { item -> item.body.isNotEmpty() }
    var token = getRandomString(50)
    if (isUpdate == "0") {
        token = model?.token.toString()
    }
    return NoteModel(
        ids = idReCreateNoteWidget ?: model?.ids, token = token, isUpdate = update, content = content, title = title, typeItem = typeNote, listCheckList = ArrayList(items ?: arrayListOf()), dateCreateNote = dateNote, isPinned = isPinned, datePinned = getDatePinned(isPinned),
        typeColor = currentColor, modifiedTime = modifiedTime, isArchive = model?.isArchive, isDelete = model?.isDelete, dateReminder = model?.dateReminder, typeRepeat = model?.typeRepeat, repeatValue = model?.repeatValue, isAlarm = model?.isAlarm, isLock = model?.isLock
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
fun EditNoteScreen.handleUpdateNote(typeDefault: String, isLocking: Boolean = false) {
    val title = binding.etTittle.text.toString()
    val content = binding.etContent.text.toString()
    val isPinned = model?.isPinned ?: false
    if (typeDefault == TypeItem.TEXT.name) {
        if (!isSame(title, content, isPinned, typeDefault, currentColor) || isLocking) {
            viewModelTextNote.updateNote(
                getDataNote(
                    "0",
                    "0",
                    model.typeItem ?: typeDefault
                )
            ) {
            }
//            updateWidgetWithId(getDataNote("0", "0", model?.typeItem ?: typeDefault))
        }

    } else {
        if (!isSameCheckList(title, isPinned, typeDefault, currentColor) || isLocking) {
            for (item in listCheckList) {
                val model = model.listCheckList?.find { it.token == item.token }
                if (model?.body != item.body || model.checked != item.checked) {
                    model?.isUpdate = true
                }
            }
            viewModelTextNote.updateNote(
                getDataNote(
                    "0",
                    "0",
                    model.typeItem ?: typeDefault
                )
            ) {}
//            updateWidgetWithId(getDataNote("0", "0", model?.typeItem ?: typeDefault))
        }
    }
}

fun EditNoteScreen.isSame(
    title: String, content: String, isPinned: Boolean, typeNote: String, typeColor: String
): Boolean {
    Log.d("TAVBNNN", title)
    modelOld?.let {
        Log.d("TAVBNNN", it.title.toString())
        return !(it.title != title || it.content != content || it.isPinned != isPinned || it.typeColor != typeColor || it.typeItem != typeNote)
    }
    return false
}

fun EditNoteScreen.isSameCheckList(
    title: String, isPinned: Boolean, typeNote: String, typeColor: String
): Boolean {
    if (listCheckList.size != modelOld?.listCheckList?.size) return false
    for (item in listCheckList) {
        val model = modelOld?.listCheckList?.find { it.token == item.token }
        if (model?.body != item.body || model.checked != item.checked) {
            return false
        }
    }
    modelOld?.let {
        return !(it.title != title || it.isPinned != isPinned || it.typeColor != typeColor || it.typeItem != typeNote)
    }
    return false
}

fun EditNoteScreen.getDatePinned(isPinned: Boolean): Long? {
    return if (isPinned) {
        if (model.datePinned.toString().isNotNullOfEmpty()) {
            model.datePinned
        } else {
            getCurrentTimeToLong()
        }
    } else {
        null
    }
}