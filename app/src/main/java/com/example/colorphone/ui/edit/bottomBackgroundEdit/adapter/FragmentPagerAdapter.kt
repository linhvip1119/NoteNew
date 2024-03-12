package com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.colorphone.model.Background
import com.example.colorphone.ui.edit.bottomBackgroundEdit.vp2.ListBackgroundFragment

class FragmentPagerAdapter(fragment: Fragment, private var currentBg: Int, private var bgClick: (Background) -> Unit) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = ListBackgroundFragment(currentBg = currentBg, currentCate = position) {
        bgClick(it)
    }

}