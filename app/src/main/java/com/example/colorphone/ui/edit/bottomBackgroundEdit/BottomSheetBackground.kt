package com.example.colorphone.ui.edit.bottomBackgroundEdit

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.colorphone.databinding.BottomSheetBackgroundEditBinding
import com.example.colorphone.model.NoteType
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter.BackgroundAdapter
import com.example.colorphone.ui.edit.bottomBackgroundEdit.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class BottomSheetBackground(
    private var colorClick: (String) -> Unit
) :
    BottomSheetDialogFragment() {

    private var _binding: BottomSheetBackgroundEditBinding? = null

    private lateinit var adapterCategory: CategoryAdapter

    private lateinit var adapterBackground: BackgroundAdapter

    private val bottomViewModel: BottomSheetViewModel by viewModels()

    private var colorItem: NoteType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetBackgroundEditBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleRadiusTop(view)
        initRecyclerCategory()
        observer()
        getData()
    }

    private fun getData() {
        bottomViewModel.getColorType()
    }

    private fun handleRadiusTop(view: View) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun initRecyclerCategory() {
        adapterCategory = CategoryAdapter {

        }
        _binding?.rvCategory?.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = adapterCategory
        }
    }

    private fun initView() {

    }

    private fun observer() {

    }

    companion object {
        fun newInstance(
            colorClick: (String) -> Unit
        ): BottomSheetBackground {
            return BottomSheetBackground(colorClick)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}