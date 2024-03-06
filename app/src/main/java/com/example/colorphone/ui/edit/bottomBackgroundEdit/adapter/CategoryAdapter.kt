package com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.ItemCategoryBinding
import com.example.colorphone.model.CategoryNote

class CategoryAdapter(val onClick: (CategoryNote) -> Unit) :
    ListAdapter<CategoryNote, CategoryAdapter.CategoryNoteVH>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<CategoryNote>() {
        override fun areItemsTheSame(oldItem: CategoryNote, newItem: CategoryNote) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: CategoryNote, newItem: CategoryNote) =
            oldItem.id == newItem.id && oldItem.name == newItem.name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryNoteVH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context))
        return CategoryNoteVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryNoteVH, position: Int) {
        holder.bind(currentList[position])
    }

    inner class CategoryNoteVH(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryNote) {
            binding.apply {
                tvName.text = item.name
                ivBgSelected.visibility = if (item.isSelected) View.INVISIBLE else View.VISIBLE
            }
        }
    }
}