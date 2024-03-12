package com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemBackgroundBinding
import com.example.colorphone.model.Background
import com.example.colorphone.util.ext.loadUrl
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.px

class BackgroundAdapter(val onClick: (Background) -> Unit) :
    ListAdapter<Background, BackgroundAdapter.BackgroundVH>(DiffCallback()) {

    var currentBackgroundId: Int = -1

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
                    context.getDisplayWidth()?.div(4) ?: 72.px
                )
                layoutParams = params
                requestFocus()

            }
            binding.apply {
//                ivBg.isSelected = item.url == currentBackgroundId
                if (item.url != -1) {
                    ivBg.loadUrl(item.url)
//                    ivBg.setBackgroundResource(0)
                } else {
                    ivBg.loadUrl(R.drawable.bg_default)
                }
                flBg.visibility = if (item.isSelected) View.INVISIBLE else View.VISIBLE
                root.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}