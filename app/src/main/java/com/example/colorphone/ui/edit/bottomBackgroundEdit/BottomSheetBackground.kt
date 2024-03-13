package com.example.colorphone.ui.edit.bottomBackgroundEdit

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.colorphone.databinding.BottomSheetBackgroundEditBinding
import com.example.colorphone.model.Background
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter.FragmentPagerAdapter
import com.example.colorphone.util.Const
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wecan.inote.util.getDisplayWidth
import com.wecan.inote.util.px
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class BottomSheetBackground(
    private var isNoteText: Boolean = false,
    private var currentBg: Int,
    private var bgClick: (Background) -> Unit
) :
    BottomSheetDialogFragment() {

    private var _binding: BottomSheetBackgroundEditBinding? = null

    private val bottomViewModel: BottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetBackgroundEditBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleRadiusTop(view)
        initView()
        checking("EditNote_Background_Show", "EditList_Background_Show")
    }

    private fun initView() {
        _binding?.llBottom?.layoutParams?.apply {
            context?.getDisplayWidth()?.div(2)?.let { height = it + 60.px }
        }

        val adapter = FragmentPagerAdapter(this, currentBg) {
            bgClick(it)
        }
        _binding?.viewPager?.adapter = adapter

        context?.let {
            bottomViewModel.getCategoryBg(it) {
                _binding?.apply {
                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = it.getOrNull(position)?.name
                    }.attach()
                }
            }
        }
        _binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab) {
                    _binding?.tabLayout?.getTabAt(0) -> {
                        checking("EditNote_Background_Color_Click", "EditList_Background_Color_Click")
                    }

                    _binding?.tabLayout?.getTabAt(1) -> {
                        checking("EditNote_Background_Paper_Click", "EditList_Background_Paper_Click")
                    }

                    _binding?.tabLayout?.getTabAt(2) -> {
                        checking("EditNote_Background_Cute_Click", "EditList_Background_Cute_Click")
                    }

                    _binding?.tabLayout?.getTabAt(3) -> {
                        checking("EditNote_Background_Dark_Click", "EditList_Background_Dark_Click")
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun handleRadiusTop(view: View) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun checking(key1: String, key2: String) {
        Const.checking(if (isNoteText) key1 else key2)
    }

    companion object {
        fun newInstance(
            isNoteText: Boolean = false,
            currentBg: Int,
            colorClick: (Background) -> Unit
        ): BottomSheetBackground {
            return BottomSheetBackground(isNoteText, currentBg, colorClick)
        }
    }
}