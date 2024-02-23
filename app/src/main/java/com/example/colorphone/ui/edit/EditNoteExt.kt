package com.example.colorphone.ui.edit

import androidx.core.view.isVisible
import com.example.colorphone.model.CheckList
import com.example.colorphone.ui.edit.adapter.MakeListVH
import com.example.colorphone.util.showKeyboard

fun BaseEditNote.getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}

fun BaseEditNote.moveToNext(currentPosition: Int) {
    val viewHolder = binding.rvCheckList.findViewHolderForAdapterPosition(currentPosition + 1) as MakeListVH?
    if (viewHolder != null) {
        if (viewHolder.binding.checkBox.isChecked) {
            moveToNext(currentPosition + 1)
        } else viewHolder.binding.editText.requestFocus()
    } else addListItem()
}

fun BaseEditNote.addListItem(isShowKeyBoard: Boolean = false) {
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

fun BaseEditNote.handleViewText() {
    binding.etContent.isVisible = isNoteText
    binding.clMakeList.isVisible = !isNoteText
}