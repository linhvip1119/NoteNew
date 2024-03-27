package com.example.colorphone.ui.edit.bottomBackgroundEdit.vp2

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentListBackgroundBinding
import com.example.colorphone.model.Background
import com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter.BackgroundAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ListBackgroundFragment(
) : BaseFragment<FragmentListBackgroundBinding>(FragmentListBackgroundBinding::inflate) {


    private var currentBg: Int = -1
    private var currentCate: Int = -1
    private var bgClick: ((Background) -> Unit)? = null

    private lateinit var adapterBackground: BackgroundAdapter

    private var listBg = arrayListOf<Background>()

    override fun init(view: View) {
        initRecyclerBackground()
        viewModelTextNote.getBackgroundNote()
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.backgroundLiveData.observe(viewLifecycleOwner) {
            listBg = ArrayList(it)
            filterBackground()
        }
        shareViewModel.changeBgLiveData.observe(viewLifecycleOwner) {
            adapterBackground.currentBackgroundId = it
            adapterBackground.notifyDataSetChanged()
        }
    }

    private fun initRecyclerBackground() {
        adapterBackground = BackgroundAdapter {
            bgClick?.invoke(it)
        }
        adapterBackground.currentBackgroundId = currentBg
        binding.rvBg.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = adapterBackground
            isSaveFromParentEnabled = false
        }
    }

    private fun filterBackground() {
        val l = listBg.filter {
            it.category == currentCate
        }
        adapterBackground.submitList(l)
    }

    companion object {
        fun getInstance(idBg: Int, category: Int, bgClick: (Background) -> Unit): ListBackgroundFragment {
            return ListBackgroundFragment().apply {
                currentBg = idBg
                currentCate = category
                this.bgClick = bgClick
            }
        }
    }
}