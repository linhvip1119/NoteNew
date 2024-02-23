package com.example.colorphone.ui.main.listNote

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentBaseListBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.main.listNote.adapter.TextAdapter
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.KEY_ID_DATA_NOTE
import com.example.colorphone.util.Const.TYPE_ITEM_EDIT
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.TypeView
import com.example.colorphone.util.hideKeyboard
import com.wecan.inote.util.setPreventDoubleClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class BaseListFragment(type: String) : BaseFragment<FragmentBaseListBinding>(FragmentBaseListBinding::inflate) {

    @Inject
    lateinit var prefUtil: PrefUtil

    private lateinit var mAdapterText: TextAdapter

    private var isNoteType = type == Const.TYPE_NOTE

    private var mListLocal: List<NoteModel>? = null

    companion object {
        fun newInstance(type: String): BaseListFragment {
            return BaseListFragment(type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun init(view: View) {
        initAdapter()
        setTypeRecyclerView()
        setUpRecyclerView(prefUtil.typeView)
        onListener()
        loadData()
    }

    private fun initAdapter() {
        mAdapterText = TextAdapter()
        mAdapterText.isDarkTheme = prefUtil.isDarkMode
        mAdapterText.typeItem = if (isNoteType) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name
    }

    private fun loadData() {
        viewModelTextNote.getListTextNote(if (isNoteType) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name, prefUtil.sortType)
    }

    private fun onListener() {
        binding.apply {
            ivCloseSearch.setPreventDoubleClick {
                edtSearch.clearFocus()
                edtSearch.text?.clear()
                activity?.hideKeyboard()
            }
        }

        mAdapterText.onLongClickItem = { it, pos ->
//            navController?.navigate(
//                R.id.selectedNoteScreen,
//                bundleOf(
//                    CURRENT_TYPE_ITEM to if (isText) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name,
//                    NOTE_FROM_LONG_CLICK to it.ids,
//                    POSITION_SELECTED to pos
//                )
//            )
        }

        mAdapterText.mOnClickItem = {
//                navigateToEditNote(false, it, it.typeColor)
        }
    }

    private fun onFragmentListener() {
//        activity?.supportFragmentManager?.setFragmentResultListener(
//            KEY_BOTTOM_TO_TEXT_SCREEN, viewLifecycleOwner
//        ) { _, bundle ->
//            bundle.getString(NoteBottomSheetDialog.KEY_STATUS_TYPE)?.let {
//                typeNoteShareVM.setCurrentTypeNote(it)
//                mapIdColor(nameColor = it, isGetIcon = true) { icon, _, _, _, _ ->
//                    binding.ivAllBox.setImageResource(icon)
//                }
//            }
//            mListLocal = getListSortType(viewModelTextNote.textNoteLiveData.value)
//            mAdapterText.submitList(mListLocal)
//            clearFocusEditText()
//            toggleRecyclerView(mListLocal ?: listOf())
//        }
    }

    private fun setTypeRecyclerView() {
        val currentTypeValue = prefUtil.typeView
        mAdapterText.typeView = when (currentTypeValue) {
            TypeView.List.value -> TypeView.List.type
            TypeView.Details.value -> TypeView.Details.type
            else -> TypeView.Grid.type
        }
    }

    private fun setUpRecyclerView(type: String) {
        binding.rv.apply {
            layoutManager = when (type) {
                TypeView.List.value -> GridLayoutManager(context, 1)
                TypeView.Grid.value -> GridLayoutManager(context, 2)
                TypeView.Details.value -> StaggeredGridLayoutManager(
                    2,
                    StaggeredGridLayoutManager.VERTICAL
                )

                else -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            adapter = mAdapterText
        }
    }

    private fun onSearchNote() {
        binding.edtSearch.doOnTextChanged { char, _, _, _ ->
            val query = char.toString().lowercase(Locale.getDefault())
            filterWithQuery(query, true)
        }
    }

    private fun filterWithQuery(query: String, isSearch: Boolean) {
        if (query.isNotEmpty()) {
            val filteredList: List<NoteModel> = onFilterChanged(query)
            mAdapterText.submitList(filteredList)
            toggleRecyclerView(filteredList, isSearch)
        } else if (query.isEmpty()) {
            mAdapterText.submitList(mListLocal)
            toggleRecyclerView(mListLocal ?: listOf(), isSearch)
        }
    }

    private fun toggleRecyclerView(noteList: List<NoteModel>, isSearch: Boolean = false) {
        binding.apply {
            rv.isVisible = noteList.isNotEmpty()
            binding.clNoData.isVisible = noteList.isEmpty()
            if (noteList.isEmpty()) {
                ivNoData.isVisible = !isSearch
                tvEmptyData.isVisible = !isSearch
                tvEmptyContent.isVisible = !isSearch
                ivSearchNoData.isVisible = isSearch
                tvEmptySearch.isVisible = isSearch
            }
        }
    }

    private fun onFilterChanged(filterQuery: String): List<NoteModel> {
        val filteredList = ArrayList<NoteModel>()
        for (currentSport in mListLocal ?: listOf()) {
            if (currentSport.title.lowercase(Locale.getDefault())
                            ?.contains(filterQuery) == true || currentSport.content?.lowercase(Locale.getDefault())
                            ?.contains(filterQuery) == true
            ) {
                filteredList.add(currentSport)
            }
        }
        return filteredList
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.textNoteLiveData.observe(viewLifecycleOwner) {
            mListLocal = getListSortType(it)
            val query = binding.edtSearch.text.toString().lowercase(Locale.getDefault())
            filterWithQuery(query, false)
        }
    }

    private fun getListSortType(list: List<NoteModel>?): List<NoteModel>? {
        return if (currentType == TypeColorNote.DEFAULT.name) list else list?.filter { data -> data.typeColor == currentType }
    }

    private fun navigateToEdit(idNote: Int? = null, currentType: String?) {
        val type =
            if (currentType == TypeColorNote.DEFAULT.name) TypeColorNote.A_ORANGE.name else currentType
        navigationWithAnim(
            R.id.editFragment, bundleOf(
                KEY_ID_DATA_NOTE to idNote,
                TYPE_ITEM_EDIT to if (isNoteType) Const.TYPE_NOTE else Const.TYPE_CHECKLIST,
            )
        )
    }
}