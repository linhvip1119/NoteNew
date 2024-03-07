package com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.ItemBackgroundBinding
import com.example.colorphone.model.Background
import com.example.colorphone.util.ext.loadUrl
import com.wecan.inote.util.px

class BackgroundAdapter(val onClick: (Background) -> Unit) :
    ListAdapter<Background, BackgroundAdapter.BackgroundVH>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Background>() {
        override fun areItemsTheSame(oldItem: Background, newItem: Background) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Background, newItem: Background) =
            oldItem.id == newItem.id && oldItem.url == newItem.url
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundVH {
        val binding = ItemBackgroundBinding.inflate(LayoutInflater.from(parent.context))
        return BackgroundVH(binding)
    }

    override fun onBindViewHolder(holder: BackgroundVH, position: Int) {
        holder.bind(currentList[position])
    }

    inner class BackgroundVH(val binding: ItemBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Background) {
            binding.apply {
                ivBg.loadUrl(item.thumb, 8.px)
                flBg.visibility = if (item.isSelected) View.INVISIBLE else View.VISIBLE
            }
        }
    }
}