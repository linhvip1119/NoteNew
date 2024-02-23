package com.example.colorphone.ui.edit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.colorphone.databinding.RecyclerListItemBinding
import com.example.colorphone.model.CheckList
import com.example.colorphone.ui.edit.utils.ListItemListener

class MakeListAdapter(
    elevation: Float,
    val list: ArrayList<CheckList>,
    private val listener: ListItemListener
) : RecyclerView.Adapter<MakeListVH>() {

    private val callback = DragCallback(elevation, this)
    private val touchHelper = ItemTouchHelper(callback)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MakeListVH, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MakeListVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerListItemBinding.inflate(inflater, parent, false)
        binding.root.background = parent.background
        return MakeListVH(binding, listener, touchHelper)
    }

    companion object {
        const val CLICK_CONTENT_ITEM = "CLICK_CONTENT_ITEM"
        const val CLICK_DELETE_ITEM = "CLICK_DELETE_ITEM"
        const val CLICK_DRAG_ITEM = "CLICK_DRAG_ITEM"
    }
}