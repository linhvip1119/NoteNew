package com.example.colorphone.ui.main.listNote.adapter

import android.graphics.Color
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

    var listLocal = mutableListOf<NoteModel>()

    fun setLockNote(isLock: Boolean) {
        isLockNote = isLock
    }

    fun getListSelected(): ArrayList<NoteModel> = ArrayList(currentList.filter { it.isSelected })


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
                            try {
                                llBody.setBackgroundResource(item.background!!)
                            } catch (_: Exception) {
                            }
                        }
                        ivPinned.compoundDrawableTintList =
                            ContextCompat.getColorStateList(root.context, idColor)
                    }
                    viewBgSelected.setBgItemNote(isDarkTheme, it)
                }
                if (isLockNote) {
                    ivLock.isVisible = item.isLock()
                }

                val colorText = ContextCompat.getColor(root.context, if (item.background != null) R.color.neutral500 else R.color.neutral500every)
                tvTittle.setTextColor(colorText)
                tvContent.setTextColor(colorText)
                tvDate.setTextColor(colorText)

                ivReminderTime.isVisible = item.isAlarm == true
                ivReminderTime.setImageResource(if (item.background != null) R.drawable.ic_reminder_time_bg else R.drawable.ic_reminder_time)

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
                            try {
                                ivBg.loadUrl(item.background!!)
                                llBody.setBackgroundColor(Color.TRANSPARENT)
                            } catch (_: Exception) {
                                ivBg.gone()
                                llBody.setBackgroundResource(bg)
                            }
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

                val colorText = ContextCompat.getColor(root.context, if (item.background != null) R.color.neutral500 else R.color.neutral500every)
                tvTittle.setTextColor(colorText)
                tvContent.setTextColor(colorText)
                tvDate.setTextColor(colorText)

                ivReminderTime.isVisible = item.isAlarm == true
                ivReminderTime.setImageResource(if (item.background != null) R.drawable.ic_reminder_time_bg else R.drawable.ic_reminder_time)

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
                            val checkedTrue = if (item.background != null) R.drawable.ic_checkbox_true_15dp_bg else R.drawable.ic_checkbox_true_15dp
                            val checkedFalse = if (item.background != null) R.drawable.ic_checkbox_false_15dp_bg else R.drawable.ic_checkbox_false_15dp
                            ivCheckBox.setImageResource(if (it.checked) checkedTrue else checkedFalse)
                            editText.text = it.body
                            editText.setTextColor(colorText)
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
                            try {
                                ivBg.loadUrl(item.background!!, 16.px)
                                llBody.setBackgroundColor(Color.TRANSPARENT)
                            } catch (_: Exception) {
                                ivBg.gone()
                                llBody.setBackgroundResource(bg)
                            }
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

                val colorText = ContextCompat.getColor(root.context, if (item.background != null) R.color.neutral500 else R.color.neutral500every)
                tvTittle.setTextColor(colorText)
                tvContent.setTextColor(colorText)
                tvDate.setTextColor(colorText)

                ivPinned.isVisible = item.isPinned == true
                tvTittle.text = item.title

                ivReminderTime.isVisible = item.isAlarm == true
                ivReminderTime.setImageResource(if (item.background != null) R.drawable.ic_reminder_time_bg else R.drawable.ic_reminder_time)

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
                            val checkedTrue = if (item.background != null) R.drawable.ic_checkbox_true_15dp_bg else R.drawable.ic_checkbox_true_15dp
                            val checkedFalse = if (item.background != null) R.drawable.ic_checkbox_false_15dp_bg else R.drawable.ic_checkbox_false_15dp
                            ivCheckBox.setImageResource(if (it.checked) checkedTrue else checkedFalse)
                            editText.apply {
                                setTextColor(colorText)
                                text = it.body
                                setOnClickListener {
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