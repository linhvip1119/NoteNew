package com.example.colorphone.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.colorphone.ui.main.listNote.BaseListNote
import com.example.colorphone.ui.main.listNote.ListCheckList
import com.example.colorphone.ui.main.listNote.ListText
import com.example.colorphone.util.Const

class DashBoardPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) ListText.newInstance() else ListCheckList.newInstance()

}