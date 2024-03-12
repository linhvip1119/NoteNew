package com.example.colorphone.ui.bottomDialogColor.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.colorphone.R
import com.example.colorphone.databinding.ItemDialogBottomBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.model.NoteType
import com.example.colorphone.ui.bottomDialogColor.adapter.BottomEditColorAdapter
import com.example.colorphone.ui.bottomDialogColor.adapter.BottomTypeColorAdapter
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.ui.edit.utils.ListItemListener
import com.example.colorphone.util.Const.EDIT_NOTE_SCREEN
import com.example.colorphone.util.Const.MAIN_SCREEN
import com.example.colorphone.util.Const.SETTING_SCREEN
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeColorNote
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NoteBottomSheetDialog(
    private var currentColor: String? = null,
    private var fromScreen: String,
    private var colorClick: (String) -> Unit
) : BottomSheetDialogFragment() {

    @Inject
    lateinit var prefUtil: PrefUtil

    private var _binding: ItemDialogBottomBinding? = null

    private lateinit var adapterEdit: BottomEditColorAdapter

    private lateinit var adapterType: BottomTypeColorAdapter

    private val _noteTypeViewModel: BottomSheetViewModel by viewModels()

    private var colorModel: NoteType = NoteType()

    private var isEdited = false

    private var isOnMainScreen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ItemDialogBottomBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRadius(view)
        initView()
        initAdapter()
        initRecyclerView()
        observer()
        onListener()
        getData()
    }

    private fun getData() {
        _noteTypeViewModel.getColorType()
    }

    private fun initAdapter() {

        val currentColor = when (fromScreen) {
            MAIN_SCREEN -> currentType
            EDIT_NOTE_SCREEN -> currentColor ?: currentType
            SETTING_SCREEN -> context?.let { prefUtil.themeColor }
            else -> currentType
        }

        val listShow = if (isOnMainScreen) colorModel.listColor.addShowAll() else colorModel.listColor

        adapterType = BottomTypeColorAdapter(listShow, currentColor) { item ->
            colorClick(item?.color!!)
            dismiss()
        }

        val elevation = resources.getDimension(R.dimen.dp1) * 2
        adapterEdit = BottomEditColorAdapter(colorModel.listColor, elevation, object : ListItemListener {

            override fun delete(position: Int) {}

            override fun moveToNext(position: Int) {}

            override fun textChanged(position: Int, text: String) {
                colorModel.listColor.getOrNull(position)?.tittle = text
            }

            override fun checkedChanged(position: Int, checked: Boolean) {}

            override fun clickView(viewClick: String) {}
        })
    }

    private fun initRadius(view: View) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun initView() {

        isOnMainScreen = fromScreen == MAIN_SCREEN

        _binding?.apply {

            tvEdit.isVisible = isOnMainScreen

            tvFilter.text = if (fromScreen == SETTING_SCREEN) context?.getString(R.string.defaultColor) else context?.getString(R.string.label)

            rvItemType.isVisible = !isEdited
            rvItemEdit.isVisible = isEdited
        }
    }

    private fun onListener() {

        _binding?.apply {
            tvEdit.setOnClickListener {
                isEdited = !isEdited
                handleClickEdit()
            }
        }
    }

    private fun handleClickEdit() {
        lifecycleScope.launch(Dispatchers.Main) {
            _binding?.tvEdit?.text = if (isEdited) getString(R.string.doneLabel) else getString(R.string.editLabel)
            if (!isEdited) {
                _binding?.rvItemEdit?.isVisible = false
                _binding?.rvItemType?.isVisible = true
                _noteTypeViewModel.updateType(colorModel.apply {
                    listColor.addShowAll()
                })
                delay(100)
                _noteTypeViewModel.getColorType()
            } else {
                _binding?.rvItemEdit?.isVisible = true
                _binding?.rvItemType?.isVisible = false
                _noteTypeViewModel.getColorType()
            }
        }
    }

    private fun ArrayList<ColorItem>.addShowAll(): ArrayList<ColorItem> {
        val itemShowAll = ColorItem(
            getString(R.string.allNotesLabel), TypeColorNote.DEFAULT.name, currentType == TypeColorNote.DEFAULT.name
        )
        val list = arrayListOf(itemShowAll)
        list.addAll(this)
        return list
    }

    private fun initRecyclerView() {
        _binding?.rvItemType?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = adapterType
        }
        _binding?.rvItemEdit?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = adapterEdit
        }
    }

    private fun observer() {
        _noteTypeViewModel.colorLD.observe(viewLifecycleOwner) { item ->
            lifecycleScope.launch {
                colorModel = item.apply {
                    listColor = ArrayList(listColor.filter { it.color != TypeColorNote.DEFAULT.name })
                }
                initAdapter()
                initRecyclerView()
//                adapterEdit.setListColorType(ArrayList(item?.listColor?.filter { it.color != TypeColorNote.DEFAULT.name } ?: listOf()))
//                adapterType.setListColorType(item.listColor)
                _binding?.rvItemEdit?.scrollToPosition(0)
            }
        }
    }

    companion object {
        fun newInstance(
            currentColor: String? = null, fromScreen: String, colorClick: (String) -> Unit
        ): NoteBottomSheetDialog {
            return NoteBottomSheetDialog(currentColor, fromScreen, colorClick)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}