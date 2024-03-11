package com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.ItemBackgroundBinding
import com.example.colorphone.model.Background
import com.example.colorphone.util.ext.loadUrl
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.px
import com.wecan.inote.util.setWidth

class BackgroundAdapter(val onClick: (Background) -> Unit) :
    ListAdapter<Background, BackgroundAdapter.BackgroundVH>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Background>() {
        override fun areItemsTheSame(oldItem: Background, newItem: Background) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Background, newItem: Background) =
            oldItem.name == newItem.name && oldItem.url == newItem.url
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

            binding.flBg.apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    context.getDisplayWidth() / 4
                )
                layoutParams = params
                requestFocus()

            }
            binding.apply {
                ivBg.loadUrl(item.url)
                flBg.visibility = if (item.isSelected) View.INVISIBLE else View.VISIBLE
                root.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}