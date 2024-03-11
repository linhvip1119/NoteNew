package com.example.colorphone.ui.bottomDialogColor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemTypeNoteBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.TypeColorNote
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.gone
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.setWidth

class BottomTypeColorAdapter(
    var listColor: ArrayList<ColorItem> = arrayListOf(),
    private var isCurrentSelect: String? = currentType,
    var onClick: ((model: ColorItem?) -> Unit)? = null
) :
    RecyclerView.Adapter<BottomTypeColorAdapter.MakeColorViewHolder>() {

    fun setListColorType(list: ArrayList<ColorItem>?) {
        this.listColor.apply {
            clear()
            addAll(list ?: arrayListOf())
        }
        notifyDataSetChanged()
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
        holder.bind(item, position)
    }

    inner class MakeColorViewHolder(val binding: ItemTypeNoteBinding) : ViewHolder(binding.root) {

        fun bind(item: ColorItem, pos: Int) {
            binding.apply {
                ivBoxView.isSelected = true
                ivDrag.gone()

                tvTittle.apply {
                    text = item.tittle
                    setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (item.color == TypeColorNote.DEFAULT.name) R.color.neutral500 else R.color.blackEvery
                        )
                    )
                }

                edtTittle.gone()

                llItemType.apply {
                    root.context.getDisplayWidth()?.div(1.2)?.toInt()?.let { setWidth(it) }
                    isSelected = isCurrentSelect == item.color
                }

                mapIdColor(nameColor = item.color) { idIcon, idBg, _, _, _ ->
                    ivBoxView.setImageResource(idIcon)
                    llItemType.setBackgroundResource(idBg)
                }

                viewItem.setOnClickListener {
                    onClick?.invoke(item)
                }
            }
        }
    }
}