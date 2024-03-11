package com.example.colorphone.ui.settings.recyclerBin.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemCheckListBinding
import com.example.colorphone.databinding.ItemTextBinding
import com.example.colorphone.databinding.ItemTextDetailsBinding
import com.example.colorphone.databinding.ItemTextListBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.TypeView
import com.example.colorphone.util.ext.convertLongToDateYYMMDD
import com.example.colorphone.util.ext.loadUrl
import com.wecan.inote.util.changeBackgroundColor
import com.wecan.inote.util.gone
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.mapIdColorWidget
import com.wecan.inote.util.setBgItemNote
import com.wecan.inote.util.show

class RecycleBinAdapter(val isStatusSelected: Boolean? = false) : ListAdapter<NoteModel, RecyclerView.ViewHolder>(DiffCallback()) {

    var isDarkTheme = false

    var mOnClickItem: ((NoteModel) -> Unit)? = null

    var onSelectItem: ((sizeListSelected: Int) -> Unit)? = null

    var onClickSelectedEvent: (() -> Unit)? = null

    var typeItem: String = TypeItem.TEXT.name

    var typeView: Int = TypeView.Details.type

    private var listLocal = mutableListOf<NoteModel>()

    fun getListSelected(): ArrayList<NoteModel> = ArrayList(currentList.filter { it.isSelected })
    fun getListNotSelected(): ArrayList<NoteModel> = ArrayList(currentList.filter { !it.isSelected })

    fun updateListAfterUnArchive() {
        listLocal.apply {
            clear()
            addAll(currentList)
        }
        submitList(currentList.filter { it.isArchive == true })
        onSelectItem?.invoke(0)
    }

    override fun getItemViewType(position: Int): Int {
        return typeView
    }

    fun updateListAfterUnRecycleBin() {
        listLocal.apply {
            clear()
            addAll(currentList)
        }
        submitList(currentList.filter { it.isDelete == true })
        onSelectItem?.invoke(0)
    }

    fun updateListDelete() {
        listLocal.apply {
            clear()
            addAll(currentList)
        }
        submitList(currentList.filter { !it.isSelected })
        onSelectItem?.invoke(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TypeView.List.type -> {
                val binding = ItemTextListBinding.inflate(LayoutInflater.from(parent.context))
                return TextListViewHolder(binding)
            }

            TypeView.Grid.type -> {
                val binding = ItemTextBinding.inflate(LayoutInflater.from(parent.context))
                return TextGridViewHolder(binding)
            }

            else -> {
                val binding = ItemTextDetailsBinding.inflate(LayoutInflater.from(parent.context))
                return TextDetailsViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextListViewHolder -> (holder as? TextListViewHolder)?.bind(
                currentList[position]
            )

            is TextGridViewHolder -> (holder as? TextGridViewHolder)?.bind(
                currentList[position]
            )

            is TextDetailsViewHolder -> (holder as? TextDetailsViewHolder)?.bind(
                currentList[position]
            )
        }
    }

    fun handleListSelectedAll(isAll: Boolean) {
        val listReset = arrayListOf<NoteModel>()
        currentList.forEach {
            listReset.add(it.apply { isSelected = !isAll })
        }
        submitList(listReset)
        onSelectItem?.invoke(listReset.filter { it.isSelected }.size)
        notifyDataSetChanged()
    }

    inner class TextListViewHolder(private val binding: ItemTextListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor?.let {
                    mapIdColor(it) { _, _, idColor, idColorBody, _ ->
                        startView.changeBackgroundColor(idColor)
                        if (item.background != null) {
                            llBody.setBackgroundResource(item.background!!)
                        } else {
                            llBody.changeBackgroundColor(idColorBody)
                        }
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                ivReminderTime.isVisible = item.isAlarm == true
                ivPinned.isVisible = false
                tvTittle.apply {
                    isVisible = !item.title.isNullOrEmpty()
                    text = item.title
                }
                tvContent.apply {
                    isVisible = item.title.isNullOrEmpty()
                    text = if (typeItem == TypeItem.TEXT.name) item.content else item.listCheckList?.firstOrNull()?.body ?: ""
                }
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(currentList.filter { it.isSelected }.size)
                        onClickSelectedEvent?.invoke()
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
            }
        }
    }

    inner class TextGridViewHolder(private val binding: ItemTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor?.let {
                    mapIdColor(it, bgColor = { bg ->
                        if (item.background != null) {
                            llBody.setBackgroundColor(Color.TRANSPARENT)
                            ivBg.loadUrl(item.background!!)
                        } else {
                            llBody.setBackgroundResource(bg)
                            ivBg.gone()
                        }
                    }) { _, _, idColor, _, _ ->
                        tvTittle.compoundDrawableTintList = ContextCompat.getColorStateList(root.context, idColor)
                        ivPinned.compoundDrawableTintList = ContextCompat.getColorStateList(root.context, idColor)

                    }
                    mapIdColorWidget(item.typeColor) { _, idIcon ->
                        ivColorWidget.setBackgroundResource(idIcon)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                ivPinned.isVisible = false
                tvTittle.text = item.title
                ivReminderTime.isVisible = item.isAlarm == true
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                if (typeItem == TypeItem.TEXT.name) {
                    tvContent.text = item.content
                    tvContent.show()
                    llCheckList.gone()
                } else {
                    tvContent.gone()
                    llCheckList.show()
                    llCheckList.removeAllViews()
                    item.listCheckList?.take(5)?.forEach {
                        val binding = ItemCheckListBinding.inflate(LayoutInflater.from(root.context))
                        binding.apply {
                            ivCheckBox.setImageResource(if (it.checked) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp)
                            editText.text = it.body
                            editText.setOnClickListener {
                                if (isStatusSelected == true) {
                                    viewBgSelected.isVisible = !viewBgSelected.isVisible
                                    item.isSelected = viewBgSelected.isVisible
                                    onSelectItem?.invoke(
                                        currentList.filter { it.isSelected }.size
                                    )
                                    onClickSelectedEvent?.invoke()
                                } else {
                                    mOnClickItem?.invoke(item)
                                }
                            }
                        }
                        llCheckList.addView(binding.root)
                    }
                }
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(
                            currentList.filter { it.isSelected }.size
                        )
                        onClickSelectedEvent?.invoke()
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
            }
        }
    }

    inner class TextDetailsViewHolder(private val binding: ItemTextDetailsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor?.let {
                    mapIdColor(it, bgColor = { bg ->
                        if (item.background != null) {
                            llBody.setBackgroundColor(Color.TRANSPARENT)
                            ivBg.loadUrl(item.background!!)
                        } else {
                            ivBg.gone()
                            llBody.setBackgroundResource(bg)
                        }
                    }) { _, _, idColor, idColorBody, _ ->
                        tvTittle.compoundDrawableTintList = ContextCompat.getColorStateList(root.context, idColor)
                    }
                    mapIdColorWidget(item.typeColor) { _, idIcon ->
                        ivColorWidget.setBackgroundResource(idIcon)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                ivReminderTime.isVisible = item.isAlarm == true
                ivPinned.isVisible = false
                tvTittle.text = item.title
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                if (item.typeItem == TypeItem.TEXT.name) {
                    tvContent.text = item.content
                    tvContent.show()
                    llCheckList.gone()
                } else {
                    tvContent.gone()
                    llCheckList.show()
                    llCheckList.removeAllViews()
                    item.listCheckList?.take(6)?.forEach {
                        val binding = ItemCheckListBinding.inflate(LayoutInflater.from(root.context))
                        binding.apply {
                            ivCheckBox.setImageResource(if (it.checked) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp)
                            editText.text = it.body
                            editText.setOnClickListener {
                                if (isStatusSelected == true) {
                                    viewBgSelected.isVisible = !viewBgSelected.isVisible
                                    item.isSelected = viewBgSelected.isVisible
                                    onSelectItem?.invoke(currentList.filter { it.isSelected }.size)
                                } else {
                                    mOnClickItem?.invoke(item)
                                }
                            }
                        }
                        llCheckList.addView(binding.root)
                    }
                }
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(currentList.filter { it.isSelected }.size)
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NoteModel>() {
        override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel) = oldItem.ids == newItem.ids

        override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel) =
            oldItem.title == newItem.title && oldItem.dateCreateNote == newItem.dateCreateNote && oldItem.typeColor == newItem.typeColor && oldItem.content == newItem.content && oldItem.isArchive == newItem.isArchive && oldItem.isPinned == newItem.isPinned && oldItem.isDelete == newItem.isDelete && oldItem.isSelected == newItem.isSelected && oldItem.datePinned == newItem.datePinned && oldItem.modifiedTime == newItem.modifiedTime && oldItem.background == newItem.background
    }
}