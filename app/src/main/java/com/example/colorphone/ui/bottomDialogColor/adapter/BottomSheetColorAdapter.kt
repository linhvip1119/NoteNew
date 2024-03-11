package com.example.colorphone.ui.bottomDialogColor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemTypeNoteBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.TypeClick
import com.example.colorphone.util.TypeColorNote
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.setWidth

class BottomSheetColorAdapter() :
    RecyclerView.Adapter<BottomSheetColorAdapter.MakeColorViewHolder>() {
    var listColor: ArrayList<ColorItem> = arrayListOf()
    var mIsCanSelected: Boolean? = null
    var mElevation: Float = 0F
    var isEdited = false
    var isCurrentSelect: String? = currentType
    var onClick: ((model: ColorItem?, typeClick: TypeClick) -> Unit)? = null
    var onEditEvent: ((String) -> Unit)? = null

    private val callback = DragCallbackColor(mElevation, this)
    private val touchHelper = ItemTouchHelper(callback)

    fun setListColorType(list: ArrayList<ColorItem>?) {
        this.listColor.apply {
            clear()
            addAll(list ?: arrayListOf())
        }
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemCount() = listColor.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MakeColorViewHolder {
        val binding =
            ItemTypeNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.background = parent.background
        return MakeColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MakeColorViewHolder, position: Int) {
        val item = listColor[position]
        holder.bind(item, position, isEdited)
    }

    inner class MakeColorViewHolder(val binding: ItemTypeNoteBinding) : ViewHolder(binding.root) {

        fun bind(item: ColorItem, pos: Int, edit: Boolean) {
            binding.apply {
                ivBoxView.isSelected = true
                viewItem.isVisible = !edit
                ivDrag.apply {
                    isVisible = edit
                    setOnTouchListener { _, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            touchHelper.startDrag(this@MakeColorViewHolder)
                        }
                        false
                    }
                }

                etTittle.apply {
                    setText(item.tittle)
                    setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (item.color == TypeColorNote.DEFAULT.name) R.color.neutral500 else R.color.blackEvery
                        )
                    )
                    setOnClickListener {
                        onEditEvent?.invoke(item.color)
                    }
                    doAfterTextChanged { text ->
                        listColor[adapterPosition].tittle = requireNotNull(text).trim().toString()
                    }
                }

                llItemType.apply {
                    root.context.getDisplayWidth()?.div(1.2)?.toInt()?.let { setWidth(it) }
                    isSelected =
                        if (mIsCanSelected == true) false else isCurrentSelect == item.color
                }

                mapIdColor(nameColor = item.color) { idIcon, idBg, _, _, _ ->
                    ivBoxView.setImageResource(idIcon)
                    llItemType.setBackgroundResource(idBg)
                }

                viewItem.setOnClickListener {
                    if (mIsCanSelected == false) {
                        isCurrentSelect = item.color
                        notifyItemRangeChanged(0, itemCount)
                        onClick?.invoke(item, TypeClick.CLICK_SELECTED)
                    } else {
                        onClick?.invoke(item, TypeClick.CLICK_CHANGE_COLOR_ITEM)
                    }
                }
            }
        }
    }
}