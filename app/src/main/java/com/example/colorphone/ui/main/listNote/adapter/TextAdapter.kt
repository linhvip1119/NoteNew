package com.example.colorphone.ui.main.listNote.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.wecan.inote.util.px
import com.wecan.inote.util.setBgItemNote
import com.wecan.inote.util.show

class TextAdapter(var isStatusSelected: Boolean? = false) :
    ListAdapter<NoteModel, RecyclerView.ViewHolder>(DiffCallback()) {

    var isDarkTheme = false

    var mOnClickItem: ((NoteModel) -> Unit)? = null

    var onLongClickItem: ((NoteModel, Int) -> Unit)? = null

    var onSelectItem: ((sizeListSelected: Int, item: NoteModel?) -> Unit)? = null

    var onClickSelectedEvent: (() -> Unit)? = null

    var typeView: Int = TypeView.Details.type

    var typeItem: String = TypeItem.TEXT.name

    private var isLockNote: Boolean = false

    private var listLocal = mutableListOf<NoteModel>()

    fun setLockNote(isLock: Boolean) {
        isLockNote = isLock
    }

    fun getListSelected(): ArrayList<NoteModel> = ArrayList(currentList.filter { it.isSelected })

    fun updateListAfterArchive() {
        listLocal.apply {
            clear()
            addAll(currentList)
        }
        submitList(currentList.filter { it.isArchive == false })
        notifyDataSetChanged()
        onSelectItem?.invoke(0, null)
    }

    fun updateListAfterDelete() {
        listLocal.apply {
            clear()
            addAll(currentList)
        }
        submitList(currentList.filter { it.isDelete == false })
        notifyDataSetChanged()
        onSelectItem?.invoke(0, null)
    }

    fun updateListAfterChangeColor(typeColor: String, listBefore: List<NoteModel>) {
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

    fun revertListArchive() {
        val list = arrayListOf<NoteModel>()
        listLocal.forEach {
            list.add(it.apply { it.isSelected = false })
        }
        onSelectItem?.invoke(0, null)
        submitList(list)
    }

    fun revertListChangeColor() {
        val list = arrayListOf<NoteModel>()
        listLocal.forEach {
            list.add(it.apply { it.isSelected = false })
        }
        onSelectItem?.invoke(0, null)
        submitList(list)
    }

    fun selectAllItem(isSelectAll: Boolean) {
        val list = arrayListOf<NoteModel>()
        currentList.forEach {
            list.add(it.apply { it.isSelected = isSelectAll })
        }
        submitList(list)
        notifyDataSetChanged()
        onSelectItem?.invoke(if (isSelectAll) currentList.size else 0, null)
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
            is TextListViewHolder -> (holder as? TextListViewHolder)?.bind(currentList[position])
            is TextGridViewHolder -> (holder as? TextGridViewHolder)?.bind(currentList[position])
            is TextDetailsViewHolder -> (holder as? TextDetailsViewHolder)?.bind(currentList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return typeView
    }

    inner class TextListViewHolder(private val binding: ItemTextListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor?.let {
                    mapIdColor(it) { _, _, idColor, idColorBody, _ ->
                        startView.changeBackgroundColor(idColor)
                        if (item.background == null) {
                            llBody.changeBackgroundColor(idColorBody)
                        } else {
                            llBody.setBackgroundResource(item.background!!)
                        }
                        ivPinned.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                if (isLockNote) {
                    ivLock.isVisible = item.isLock()
                }
                ivReminderTime.isVisible = item.isAlarm == true
                ivPinned.isVisible = item.isPinned == true
                tvTittle.apply {
                    isVisible = !item.title.isNullOrEmpty()
                    text = item.title
                }
                tvContent.apply {
                    isVisible = item.title.isNullOrEmpty()
                    text =
                        if (typeItem == TypeItem.TEXT.name) item.content else item.listCheckList?.firstOrNull()?.body
                                ?: ""
                }
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(currentList.filter { it.isSelected }.size,
                                             currentList.firstOrNull { it.isSelected })
                        onClickSelectedEvent?.invoke()
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
                if (isStatusSelected == false) root.setOnLongClickItem(item, adapterPosition)
            }
        }
    }

    inner class TextDetailsViewHolder(private val binding: ItemTextDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor.let {
                    mapIdColor(it, bgColor = { bg ->
                        if (item.background == null) {
                            ivBg.gone()
                            llBody.setBackgroundResource(bg)
                        } else {
                            llBody.setBackgroundColor(Color.TRANSPARENT)
                            ivBg.loadUrl(item.background!!)
                        }
                    }) { _, _, idColor, _, _ ->
                        tvTittle.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)
                        ivPinned.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                mapIdColorWidget(item.typeColor) { _, idIcon ->
                    ivColorWidget.setBackgroundResource(idIcon)
                }
                Log.i("456234", "bind: ${item.ids}")
                ivReminderTime.isVisible = item.isAlarm == true
                ivPinned.isVisible = item.isPinned == true
                tvTittle.text = item.title
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                if (typeItem == TypeItem.TEXT.name) {
                    tvContent.text = item.content
                    if (item.isLock() && isLockNote) {
                        tvContent.visibility = View.INVISIBLE
                        ivLockText.show()
                    } else {
                        tvContent.show()
                        ivLockText.gone()
                    }
                    llCheckList.gone()
                } else {
                    tvContent.gone()
                    if (item.isLock() && isLockNote) {
                        llCheckList.visibility = View.INVISIBLE
                        ivLockText.show()
                    } else {
                        llCheckList.show()
                        ivLockText.gone()
                    }
                    llCheckList.removeAllViews()
                    item.listCheckList?.take(10)?.forEach {
                        val binding =
                            ItemCheckListBinding.inflate(LayoutInflater.from(root.context))
                        binding.apply {
                            ivCheckBox.setImageResource(if (it.checked) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp)
                            editText.text = it.body
                            editText.setOnClickListener {
                                if (isStatusSelected == true) {
                                    viewBgSelected.isVisible = !viewBgSelected.isVisible
                                    item.isSelected = viewBgSelected.isVisible
                                    onSelectItem?.invoke(currentList.filter { it.isSelected }.size,
                                                         currentList.firstOrNull { it.isSelected })
                                    onClickSelectedEvent?.invoke()
                                } else {
                                    mOnClickItem?.invoke(item)
                                }
                            }
                            if (isStatusSelected == false) editText.setOnLongClickItem(
                                item,
                                adapterPosition
                            )
                        }
                        llCheckList.addView(binding.root)
                    }
                }
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(currentList.filter { it.isSelected }.size,
                                             currentList.firstOrNull { it.isSelected })
                        onClickSelectedEvent?.invoke()
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
                if (isStatusSelected == false) root.setOnLongClickItem(item, adapterPosition)
            }
        }
    }

    inner class TextGridViewHolder(private val binding: ItemTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteModel) {
            binding.apply {
                item.typeColor?.let {
                    mapIdColor(it, bgColor = { bg ->
                        if (item.background == null) {
                            llBody.setBackgroundResource(bg)
                            ivBg.gone()
                        } else {
                            llBody.setBackgroundColor(Color.TRANSPARENT)
                            ivBg.loadUrl(item.background!!)
                        }
                    }) { _, _, idColor, _, _ ->
                        tvTittle.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)
                        ivPinned.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)

                    }
                    mapIdColorWidget(item.typeColor) { _, idIcon ->
                        ivColorWidget.setBackgroundResource(idIcon)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                ivPinned.isVisible = item.isPinned == true
                tvTittle.text = item.title
                ivReminderTime.isVisible = item.isAlarm == true
                tvDate.text = convertLongToDateYYMMDD(item.dateCreateNote ?: 0)
                if (typeItem == TypeItem.TEXT.name) {
                    tvContent.text = item.content
                    if (item.isLock() && isLockNote) {
                        tvContent.visibility = View.INVISIBLE
                        ivLock.show()
                    } else {
                        tvContent.show()
                        ivLock.gone()
                    }
                    llCheckList.gone()
                } else {
                    tvContent.gone()
                    if (item.isLock() && isLockNote) {
                        llCheckList.visibility = View.INVISIBLE
                        ivLock.show()
                    } else {
                        llCheckList.show()
                        ivLock.gone()
                    }
                    llCheckList.removeAllViews()
                    item.listCheckList?.take(5)?.forEach {
                        val binding =
                            ItemCheckListBinding.inflate(LayoutInflater.from(root.context))
                        binding.apply {
                            ivCheckBox.setImageResource(if (it.checked) R.drawable.ic_checkbox_true_15dp else R.drawable.ic_checkbox_false_15dp)
                            editText.text = it.body
                            editText.setOnClickListener {
                                if (isStatusSelected == true) {
                                    viewBgSelected.isVisible = !viewBgSelected.isVisible
                                    item.isSelected = viewBgSelected.isVisible
                                    onSelectItem?.invoke(currentList.filter { it.isSelected }.size,
                                                         currentList.firstOrNull { it.isSelected })
                                    onClickSelectedEvent?.invoke()
                                } else {
                                    mOnClickItem?.invoke(item)
                                }
                            }
                            if (isStatusSelected == false) editText.setOnLongClickItem(
                                item,
                                adapterPosition
                            )
                        }
                        llCheckList.addView(binding.root)
                    }
                }
                viewBgSelected.isVisible = item.isSelected
                root.setOnClickListener {
                    if (isStatusSelected == true) {
                        viewBgSelected.isVisible = !viewBgSelected.isVisible
                        item.isSelected = viewBgSelected.isVisible
                        onSelectItem?.invoke(currentList.filter { it.isSelected }.size,
                                             currentList.firstOrNull { it.isSelected })
                        onClickSelectedEvent?.invoke()
                    } else {
                        mOnClickItem?.invoke(item)
                    }
                }
                if (isStatusSelected == false) root.setOnLongClickItem(item, adapterPosition)
            }
        }
    }

    private fun View.setOnLongClickItem(model: NoteModel, position: Int) {
        this.setOnLongClickListener {
            onLongClickItem?.invoke(model, position)
            return@setOnLongClickListener true
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NoteModel>() {
        override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel) =
            oldItem.ids == newItem.ids

        override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel) =
            oldItem.title == newItem.title && oldItem.background == newItem.background && oldItem.dateCreateNote == newItem.dateCreateNote && oldItem.typeColor == newItem.typeColor && oldItem.content == newItem.content && oldItem.isArchive == newItem.isArchive && oldItem.isPinned == newItem.isPinned && oldItem.isDelete == newItem.isDelete && oldItem.isSelected == newItem.isSelected && oldItem.datePinned == newItem.datePinned && oldItem.modifiedTime == newItem.modifiedTime
    }
}