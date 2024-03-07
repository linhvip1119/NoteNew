package com.example.colorphone.ui.bottomDialogColor.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemDialogBottomBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.model.NoteType
import com.example.colorphone.ui.bottomDialogColor.adapter.BottomSheetColorAdapter
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.util.Const.EDIT_NOTE_SCREEN
import com.example.colorphone.util.Const.SELECTED_SCREEN
import com.example.colorphone.util.Const.SETTING_SCREEN
import com.example.colorphone.util.Const.TEXT_SCREEN
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeClick
import com.example.colorphone.util.TypeColorNote
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wecan.inote.util.getWidthDevice
import com.wecan.inote.util.gone
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NoteBottomSheetDialog(
    private var isNotShowDefaultColor: Boolean = false,
    private var currentColor: String? = null,
    private var fromScreen: String,
    private var colorClick: (String) -> Unit
) :
    BottomSheetDialogFragment() {

    @Inject
    lateinit var prefUtil: PrefUtil

    private var _binding: ItemDialogBottomBinding? = null

    private val adapterBottom by lazy { BottomSheetColorAdapter() }

    private val _noteTypeViewModel: BottomSheetViewModel by viewModels()

    private var colorItem: NoteType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val elevation = resources.getDimension(R.dimen.dp1) * 2
        adapterBottom.apply {
            mElevation = elevation
            mWidthDevice = activity?.getWidthDevice()
            mIsCanSelected = (fromScreen == SELECTED_SCREEN)
            isCurrentSelect = when (fromScreen) {
                TEXT_SCREEN -> currentType
                EDIT_NOTE_SCREEN -> currentColor ?: currentType
                SETTING_SCREEN -> context?.let { prefUtil.themeColor }
                else -> currentType
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ItemDialogBottomBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        initView()
        initRecyclerView()
        observer()
        onListener()
    }

    override fun onResume() {
        super.onResume()
        _noteTypeViewModel.getColorType()
    }

    private fun initView() {
        when (fromScreen) {
            SETTING_SCREEN -> {
                _binding?.tvEdit?.gone()
                _binding?.tvFilter?.text = context?.getString(R.string.defaultColor)
            }

            EDIT_NOTE_SCREEN -> {
                _binding?.tvEdit?.gone()
                _binding?.tvFilter?.text = context?.getString(R.string.label)
            }
        }

    }

    private fun onListener() {
        _binding?.apply {
            tvEdit.setOnClickListener {
                handleViewEdit(tvEdit.text.toString() == context?.getString(R.string.editLabel))
            }
        }
        adapterBottom.onClick = { item, type ->
            when (type) {
                TypeClick.CLICK_SELECTED -> {
                    lifecycleScope.launch {
                        colorClick(item?.color!!)
                        dismiss()
                    }
                }

                TypeClick.CLICK_CHANGE_COLOR_ITEM -> {
                    colorClick(item?.color!!)
                    dismiss()
                }
            }
        }
    }

    private fun handleViewEdit(isEditColor: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            adapterBottom.isEdited = isEditColor
            delay(200)
            _binding?.tvEdit?.text =
                if (isEditColor) getString(R.string.doneLabel) else getString(R.string.editLabel)
            if (isEditColor) {
                isNotShowDefaultColor = true
                _noteTypeViewModel.getColorType()
            } else {
                isNotShowDefaultColor = false
                _noteTypeViewModel.updateType(colorItem?.apply {
                    listColor = adapterBottom.listColor.addShowAll()
                })
                delay(200)
                _noteTypeViewModel.getColorType()
            }
            delay(100)
            _binding?.rvItemType?.scrollToPosition(0)
        }
    }

    private fun ArrayList<ColorItem>.addShowAll(): ArrayList<ColorItem> {
        val itemShowAll = ColorItem(
            getString(R.string.allNotesLabel),
            TypeColorNote.DEFAULT.name,
            currentType == TypeColorNote.DEFAULT.name
        )
        val list = arrayListOf(itemShowAll)
        list.addAll(this)
        return list
    }

    private fun initRecyclerView() {
        _binding?.rvItemType?.adapter = adapterBottom
    }

    private fun observer() {
        _noteTypeViewModel.colorLD.observe(viewLifecycleOwner) { item ->
            lifecycleScope.launch {
                val list = if (isNotShowDefaultColor) item.listColor else item.listColor?.filter { it.color != TypeColorNote.DEFAULT.name }
                colorItem = item.apply {
                    listColor = ArrayList(list ?: arrayListOf())
                }
                adapterBottom.setListColorType(colorItem?.listColor)
                delay(100)
                _binding?.rvItemType?.scrollToPosition(0)
            }
        }
    }

    companion object {
        fun newInstance(
            showColorDefault: Boolean,
            currentColor: String? = null,
            fromScreen: String,
            colorClick: (String) -> Unit
        ): NoteBottomSheetDialog {
            return NoteBottomSheetDialog(showColorDefault, currentColor, fromScreen, colorClick)
        }

        const val KEY_STATUS_TYPE = "KEY_STATUS_TYPE"
        const val KEY_SELECTED_TYPE = "KEY_SELECTED_TYPE"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}