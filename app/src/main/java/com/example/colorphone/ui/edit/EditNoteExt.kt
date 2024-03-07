package com.example.colorphone.ui.edit

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.databinding.PopupMenuEditBinding
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.edit.adapter.MakeListVH
import com.example.colorphone.ui.edit.utils.TextViewUndoRedo
import com.example.colorphone.ui.select.SelectScreen.Companion.ITEM_FROM_SELECTED_SCREEN
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.getCurrentTimeToLong
import com.example.colorphone.util.ext.hideKeyboard
import com.example.colorphone.util.ext.isNotNullOfEmpty
import com.example.colorphone.util.ext.showKeyboard
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.showCustomToast
import com.wecan.inote.util.showCustomToastPinned
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

fun EditNoteScreen.getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}

fun EditNoteScreen.moveToNext(currentPosition: Int) {
    val viewHolder = binding.rvCheckList.findViewHolderForAdapterPosition(currentPosition + 1) as MakeListVH?
    if (viewHolder != null) {
        if (viewHolder.binding.checkBox.isSelected) {
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
            updateWidgetWithId(getDataNote("0", "0", model?.typeItem ?: typeDefault))
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
            updateWidgetWithId(getDataNote("0", "0", model?.typeItem ?: typeDefault))
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

@OptIn(ExperimentalCoroutinesApi::class)
fun EditNoteScreen.initiatePopupMenu(): PopupWindow? {
    var mDropdown: PopupWindow? = null
    try {
        val mInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bindingPopup = PopupMenuEditBinding.inflate(mInflater)
        val layout = bindingPopup.root

        bindingPopup.apply {

            item1.apply {
                if (model?.isPinned == true) {
                    tvText.text = getString(R.string.unPinLabel)
                    ivIcon.setImageResource(R.drawable.ic_unpin_edit)
                } else {
                    tvText.text = getString(R.string.pin)
                    ivIcon.setImageResource(R.drawable.ic_pin_edit)
                }

                root.setOnClickListener {
                    model?.isPinned = model?.isPinned == false
                    context?.let { ct ->
                        Toast(ct).showCustomToastPinned(
                            ct,
                            model?.isPinned == true
                        )
                    }
                    mDropdown?.dismiss()
                }
            }

            item2.apply {
                tvText.text = getString(R.string.reminderTime)
                ivIcon.setImageResource(R.drawable.ic_remindertime_primary)

                root.setOnClickListener {
                    navigationWithAnim(
                        R.id.action_editFragment_to_reminderFragment, bundleOf(
                            ITEM_FROM_SELECTED_SCREEN to model?.ids
                        )
                    )
                    mDropdown?.dismiss()
                }
            }

            item3.apply {
                tvText.text = getString(R.string.addToHome)
                ivIcon.setImageResource(R.drawable.ic_add_to_home)

                root.setOnClickListener {
                    handleSaveNote {
                        addPhotoWidget(model)
                    }
                    mDropdown?.dismiss()
                }
            }

            item4.apply {
                tvText.text = getString(R.string.swapText)
                ivIcon.setImageResource(R.drawable.ic_swap_text)

                root.setOnClickListener {
                    swapNote()
                    mDropdown?.dismiss()
                }
            }

            item5.apply {
                tvText.text = getString(R.string.archiveLabel)
                ivIcon.setImageResource(R.drawable.ic_archive_primary)

                root.setOnClickListener {
                    lifecycleScope.launch {
                        model?.let { viewModelTextNote.archiveNote(it) }
                    }
                    activity?.let {
                        Toast(context).showCustomToast(
                            it,
                            getString(R.string.noteArchiveLabel).plus(".")
                        )
                    }
                    mDropdown?.dismiss()
                    navController?.popBackStack()
                }
            }

            item6.apply {
                tvText.text = getString(R.string.share)
                ivIcon.setImageResource(R.drawable.ic_share)

                root.setOnClickListener {
                    shareNote()
                    mDropdown?.dismiss()
                }
            }

            item7.apply {
                tvText.text = getString(R.string.deleteLabel)
                ivIcon.setImageResource(R.drawable.ic_delete_primary)

                root.setOnClickListener {
                    lifecycleScope.launch {
                        model?.let { viewModelTextNote.deleteLocalNote(it) }
                    }
                    activity?.let {
                        Toast(context).showCustomToast(
                            it, getString(R.string.noteDeletedLabel).plus(".")
                        )
                    }
                    navController?.popBackStack()
                    mDropdown?.dismiss()
                }
            }
        }
        layout.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        mDropdown = PopupWindow(
            layout, FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT, true
        )
        mDropdown.showAsDropDown(binding.ivMenu, 0, 0, Gravity.END)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return mDropdown
}

fun EditNoteScreen.swapNote() {
    binding.apply {
        lifecycleScope.launch {
            isNoteText = !isNoteText
            handleViewText()
            if (isNoteText) {
                swapCheckListToText()
            } else {
                swapTextToCheckList()
            }
        }
    }
}

private fun EditNoteScreen.swapCheckListToText() {
    lifecycleScope.launch {
        var newText = ""
        model?.typeItem = TypeItem.TEXT.name
        model?.listCheckList?.forEachIndexed() { pos, it ->
            newText = if (pos != (model?.listCheckList?.size?.minus(1) ?: 0)) newText.plus(it.body).plus("\n") else newText.plus(it.body)
        }
        model?.content = newText
        binding.etContent.setText(newText)
        model?.listCheckList?.clear()
    }
}

private fun EditNoteScreen.swapTextToCheckList() {
    lifecycleScope.launch {
        val newListCL = arrayListOf<CheckList>()
        val listText = binding.etContent.text.toString().split("\n")
        listText.forEach {
            newListCL.add(CheckList(it, false, false, getRandomString(50)))
        }
        model?.listCheckList = newListCL
        model?.typeItem = TypeItem.CHECK_LIST.name
        setupRecyclerView()
    }
}

fun EditNoteScreen.shareNote() {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, model?.title?.plus("\n").plus(model?.content))
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun EditNoteScreen.handeReadMode() {
    binding.apply {
        etTittle.isEnabled = !onReadMode
        etContent.isEnabled = !onReadMode

        if (onReadMode) {
            ivUndo.isEnabled = !onReadMode
            ivRedo.isEnabled = !onReadMode
        } else {
            handleEnableIconDo()
        }

        tvAddItem.isEnabled = !onReadMode
        mapIdColor(currentColor) { idIcon, _, _, _, _ ->
            ivReadMode.setBackgroundResource(if (onReadMode) idIcon else 0)
        }
        ivReadMode.alpha = if (onReadMode) 0.5f else 1f
        if (model.typeItem == TypeItem.CHECK_LIST.name) {
            adapter.changeReadMode(onReadMode)
            adapter.notifyDataSetChanged()
        }
    }
}

fun EditNoteScreen.handleEnableIconDo() {
    binding.apply {
        when {
            isFocusTittle -> {
                ivUndo.isEnabled = helperTittle?.canUndo == true
                ivRedo.isEnabled = helperTittle?.canRedo == true
            }

            isFocusContent -> {
                ivUndo.isEnabled = helperContent?.canUndo == true
                ivRedo.isEnabled = helperContent?.canRedo == true
            }

            else -> {
                ivUndo.isEnabled = false
                ivRedo.isEnabled = false
            }
        }
    }
}

fun EditNoteScreen.handleRedoUndo() {

    helperTittle = TextViewUndoRedo(binding.etTittle)
    helperContent = TextViewUndoRedo(binding.etContent)

    binding.ivRedo.setOnClickListener {
        if (isFocusTittle) helperTittle?.redo() else helperContent?.redo()
    }
    binding.ivUndo.setOnClickListener {
        if (isFocusTittle) helperTittle?.undo() else helperContent?.undo()
    }

    binding.etTittle.apply {
        onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            isFocusTittle = hasFocus
        }
    }
    binding.etContent.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus) v.showKeyboard() else v.hideKeyboard()
        isFocusContent = hasFocus
    }
}