package com.example.colorphone.ui.settings.advanced.bottomLanguage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.ItemLanguageBinding
import com.example.colorphone.model.LanguageModel

class BottomLanguageAdapter(val onClick: (LanguageModel) -> Unit) :
    ListAdapter<LanguageModel, BottomLanguageAdapter.LanguageVH>(DiffCallback()) {

    var currentLanguage: String = ""

    private class DiffCallback : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel) =
            oldItem.name == newItem.name && oldItem.text == newItem.text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageVH {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context))
        return LanguageVH(binding)
    }

    override fun onBindViewHolder(holder: LanguageVH, position: Int) {
        holder.bind(currentList[position])
    }

    inner class LanguageVH(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LanguageModel) {
            binding.tvView.apply {
                text = item.text
                isSelected = item.name == currentLanguage
                setOnClickListener { onClick.invoke(item) }
            }
        }
    }
}