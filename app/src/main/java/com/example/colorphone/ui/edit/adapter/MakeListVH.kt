package com.example.colorphone.ui.edit.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.RecyclerListItemBinding
import com.example.colorphone.model.CheckList
import com.example.colorphone.ui.edit.adapter.MakeListAdapter.Companion.CLICK_CONTENT_ITEM
import com.example.colorphone.ui.edit.adapter.MakeListAdapter.Companion.CLICK_DELETE_ITEM
import com.example.colorphone.ui.edit.adapter.MakeListAdapter.Companion.CLICK_DRAG_ITEM
import com.example.colorphone.ui.edit.utils.ListItemListener
import com.wecan.inote.util.setOnClickAnim
import com.wecan.inote.util.setOnNextAction

@SuppressLint("ClickableViewAccessibility")
class MakeListVH(
    val binding: RecyclerListItemBinding,
    listener: ListItemListener,
    touchHelper: ItemTouchHelper,
) : RecyclerView.ViewHolder(binding.root) {

    init {

        binding.editText.setOnNextAction {
            listener.moveToNext(adapterPosition)
        }

        binding.editText.doAfterTextChanged { text ->
            listener.textChanged(adapterPosition, requireNotNull(text).trim().toString())
        }
        binding.editText.setOnClickListener { listener.clickView(CLICK_CONTENT_ITEM) }

        binding.ivCloseDelete.setOnClickAnim {
            listener.delete(adapterPosition)
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            listener.clickView(CLICK_DELETE_ITEM)
            binding.editText.isEnabled = !isChecked
            listener.checkedChanged(adapterPosition, isChecked)
        }

        binding.ivDragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                listener.clickView(CLICK_DRAG_ITEM)
                touchHelper.startDrag(this)
            }
            false
        }
    }

    fun bind(item: CheckList) {
        binding.editText.setText(item.body)
//        binding.editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) v.showKeyboard() else v.hideKeyboard()
//        }
        binding.checkBox.isChecked = item.checked
    }
}