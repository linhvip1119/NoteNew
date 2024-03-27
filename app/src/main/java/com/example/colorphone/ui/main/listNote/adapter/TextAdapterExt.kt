package com.example.colorphone.ui.main.listNote.adapter

import com.example.colorphone.model.NoteModel
import org.w3c.dom.Text

fun TextAdapter.updateListAfterArchive() {
    listLocal.apply {
        clear()
        addAll(currentList)
    }
    submitList(currentList.filter { it.isArchive == false })
    notifyDataSetChanged()
    onSelectItem?.invoke(0, null)
}

fun TextAdapter.updateListAfterDelete() {
    listLocal.apply {
        clear()
        addAll(currentList)
    }
    submitList(currentList.filter { it.isDelete == false })
    notifyDataSetChanged()
    onSelectItem?.invoke(0, null)
}

fun TextAdapter.updateListAfterChangeColor(typeColor: String, listBefore: List<NoteModel>) {
    listLocal.apply {
        clear()
        addAll(listBefore)
    }
    val list = arrayListOf<NoteModel>()
    currentList.forEach {
        val model =
            if (getListSelected().contains(it)) it.apply { this.typeColor = typeColor } else it
        list.add(model?.apply { it.isSelected = false } ?: NoteModel(dateCreateNote = 0))
    }
    submitList(list)
    notifyDataSetChanged()
    onSelectItem?.invoke(0, null)
}

fun TextAdapter.revertListArchive() {
    val list = arrayListOf<NoteModel>()
    listLocal.forEach {
        list.add(it.apply { it.isSelected = false })
    }
    onSelectItem?.invoke(0, null)
    submitList(list)
}

fun TextAdapter.revertListChangeColor() {
    val list = arrayListOf<NoteModel>()
    listLocal.forEach {
        list.add(it.apply { it.isSelected = false })
    }
    onSelectItem?.invoke(0, null)
    submitList(list)
}

fun TextAdapter.selectAllItem(isSelectAll: Boolean) {
    val list = arrayListOf<NoteModel>()
    currentList.forEach {
        list.add(it.apply { it.isSelected = isSelectAll })
    }
    submitList(list)
    notifyDataSetChanged()
    onSelectItem?.invoke(if (isSelectAll) currentList.size else 0, null)
}