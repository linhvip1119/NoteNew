package com.example.colorphone.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.databinding.RowBinding
import com.example.colorphone.util.Const.CURRENT_TYPE_ITEM
import com.example.colorphone.util.SortType
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.hideKeyboard
import com.example.colorphone.util.ext.showDialogOptionSoft
import com.example.colorphone.util.ext.showDialogOptionView
import com.wecan.inote.util.px
import com.wecan.inote.util.setPreventDoubleClick
import kotlinx.coroutines.launch

fun MainFragment.navToSelectScreen() {
    navigationWithAnim(
        R.id.selectFragment,
        bundleOf(CURRENT_TYPE_ITEM to if (currentNote == MainFragment.TEXT_FM) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name)
    )
}

fun MainFragment.openDialogView() {
    context?.showDialogOptionView(prefUtil.typeView) {
        lifecycleScope.launch {
            prefUtil.typeView = it.value
            shareViewModel.setChangeViewList(it)
        }
    }
}

fun MainFragment.handleDialogSoft() {
    context?.showDialogOptionSoft(prefUtil.sortType.toString()) {
        when (it) {
            SortType.MODIFIED_TIME.name -> {
                prefUtil.sortType = SortType.MODIFIED_TIME.name
                shareViewModel.setSortText(SortType.MODIFIED_TIME.name)
            }

            SortType.CREATE_TIME.name -> {
                prefUtil.sortType = SortType.CREATE_TIME.name
                shareViewModel.setSortText(SortType.CREATE_TIME.name)
            }

            SortType.REMINDER_TIME.name -> {
                prefUtil.sortType = SortType.REMINDER_TIME.name
                shareViewModel.setSortText(SortType.REMINDER_TIME.name)
            }

            SortType.COLOR.name -> {
                prefUtil.sortType = SortType.COLOR.name
                shareViewModel.setSortText(SortType.COLOR.name)
            }
        }
    }
}

fun MainFragment.clearFocusEditText() {
    binding.edtSearch.apply {
        text?.clear()
        clearFocus()
    }
    activity?.hideKeyboard()
}

fun MainFragment.initiatePopupMenu(): PopupWindow? {
    try {
        mInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val menuBinding = RowBinding.inflate(mInflater!!)

        menuBinding.root.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        menuBinding.apply {
            tvSelect.setPreventDoubleClick {
                navToSelectScreen()
                mDropdown?.dismiss()
            }
            tvView.setPreventDoubleClick {
                openDialogView()
                mDropdown?.dismiss()
            }
            tvSort.setPreventDoubleClick {
                handleDialogSoft()
                mDropdown?.dismiss()
            }
        }
        mDropdown = PopupWindow(
            menuBinding.root, FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT, true
        )
        mDropdown?.showAsDropDown(binding.ivSync, (-20).px, (-5).px)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return mDropdown
}