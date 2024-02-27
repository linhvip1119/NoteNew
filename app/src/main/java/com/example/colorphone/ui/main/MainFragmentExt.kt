package com.example.colorphone.ui.main

import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.util.Const.CURRENT_TYPE_ITEM
import com.example.colorphone.util.SortType
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.hideKeyboard
import com.example.colorphone.util.showDialogOptionSoft
import com.example.colorphone.util.showDialogOptionView
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
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