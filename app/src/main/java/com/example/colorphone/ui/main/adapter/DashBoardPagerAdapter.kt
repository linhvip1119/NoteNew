package com.example.colorphone.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.colorphone.ui.main.listNote.ListNoteScreen
import com.example.colorphone.util.Const

class DashBoardPagerAdapter(fa: FragmentActivity, private val text : String, private val callback : () -> Unit) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) ListNoteScreen.newInstance(Const.TYPE_NOTE, text, callback) else ListNoteScreen.newInstance(Const.TYPE_CHECKLIST, text, callback)

}