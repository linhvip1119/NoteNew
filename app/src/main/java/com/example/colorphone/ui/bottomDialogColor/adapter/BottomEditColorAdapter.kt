package com.example.colorphone.ui.bottomDialogColor.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemTypeNoteBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.ui.edit.adapter.MakeListAdapter
import com.example.colorphone.ui.edit.utils.ListItemListener
import com.example.colorphone.util.TypeColorNote
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.gone
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.setOnClickAnim
import com.wecan.inote.util.setOnNextAction
import com.wecan.inote.util.setWidth
import com.wecan.inote.util.show

class BottomEditColorAdapter(
    var listColor: ArrayList<ColorItem> = arrayListOf(),
    mElevation: Float = 0F,
    private val listener: ListItemListener
) :
    RecyclerView.Adapter<BottomEditColorAdapter.MakeColorViewHolder>() {

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
        return MakeColorViewHolder(binding, listener, touchHelper)
    }

    override fun onBindViewHolder(holder: MakeColorViewHolder, position: Int) {
        val item = listColor[position]
        holder.bind(item, position)
    }

    inner class MakeColorViewHolder(
        val binding: ItemTypeNoteBinding,
        listener: ListItemListener,
        touchHelper: ItemTouchHelper,
    ) : ViewHolder(binding.root) {


        init {

            binding.edtTittle.doAfterTextChanged { text ->
                listener.textChanged(adapterPosition, requireNotNull(text).trim().toString())
            }

            binding.ivDrag.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    listener.clickView(MakeListAdapter.CLICK_DRAG_ITEM)
                    touchHelper.startDrag(this)
                }
                false
            }
        }

        fun bind(item: ColorItem, pos: Int) {
            binding.apply {
                ivBoxView.isSelected = true
                tvTittle.gone()
                ivDrag.show()

                edtTittle.apply {
                    setText(item.tittle)
                    setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (item.color == TypeColorNote.DEFAULT.name) R.color.neutral500 else R.color.blackEvery
                        )
                    )
                }

                llItemType.apply {
                    root.context.getDisplayWidth()?.div(1.2)?.toInt()?.let { setWidth(it) }
                    isSelected = false
                }

                mapIdColor(nameColor = item.color) { idIcon, idBg, _, _, _ ->
                    ivBoxView.setImageResource(idIcon)
                    llItemType.setBackgroundResource(idBg)
                }
            }
        }
    }
}