package com.example.colorphone.ui.main.listNote

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentBaseListBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.main.listNote.adapter.TextAdapter
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.CURRENT_TYPE_ITEM
import com.example.colorphone.util.Const.KEY_ID_DATA_NOTE
import com.example.colorphone.util.Const.NOTE_FROM_LONG_CLICK
import com.example.colorphone.util.Const.POSITION_SELECTED
import com.example.colorphone.util.Const.TYPE_ITEM_EDIT
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.SortType
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.TypeView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ListNoteScreen(type: String) : BaseFragment<FragmentBaseListBinding>(FragmentBaseListBinding::inflate) {

    private lateinit var mAdapterText: TextAdapter

    private var isNoteType = type == Const.TYPE_NOTE

    private var mListLocal: List<NoteModel>? = null

    private var textSearch: String = ""

    private var call: (() -> Unit)? = null

    companion object {
        fun newInstance(type: String, text: String, callback: () -> Unit): ListNoteScreen {
            return ListNoteScreen(type).apply {
                textSearch = text
                call = callback
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun init(view: View) {
        Const.checking(if (isNoteType) "Main_tabNote_Show" else "Main_tabChecklist_Show")
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
        mAdapterText.onLongClickItem = { it, pos ->
            Const.checking("Main_itemNoteList_Hold")
            navigationWithAnim(
                R.id.selectFragment,
                bundleOf(
                    CURRENT_TYPE_ITEM to if (isNoteType) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name,
                    NOTE_FROM_LONG_CLICK to it.ids,
                    POSITION_SELECTED to pos
                )
            )
        }

        mAdapterText.mOnClickItem = {
            Const.checking(if (isNoteType) "Main_ItemNote_Click" else "Main_ItemChecklist_Click")
            navigateToEdit(it.ids)
        }
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
            groupEmptyData.isVisible = noteList.isEmpty() && !isSearch
            groupEmptySearch.isVisible = noteList.isEmpty() && isSearch
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
            val query = textSearch
            filterWithQuery(query, false)
        }
        with(shareViewModel) {

            filterColorLiveData.observe(viewLifecycleOwner) {
                currentType = it
                mListLocal = getListSortType(viewModelTextNote.textNoteLiveData.value)
                mAdapterText.submitList(mListLocal)
                mAdapterText.notifyDataSetChanged()
                call?.invoke()
                toggleRecyclerView(mListLocal ?: listOf())
            }

            changeViewListLiveData.observe(viewLifecycleOwner) {
                setUpRecyclerView(it.value)
                mAdapterText.typeView = it.type
                mAdapterText.notifyDataSetChanged()
            }

            searchLiveData.observe(viewLifecycleOwner) {
                textSearch = it
                filterWithQuery(it, true)
            }
            sortLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    SortType.MODIFIED_TIME.name -> {
                        viewModelTextNote.sortModifiedTime()
                    }

                    SortType.CREATE_TIME.name -> {
                        viewModelTextNote.sortCreateTime()
                    }

                    SortType.REMINDER_TIME.name -> {
                        viewModelTextNote.sortReminderTime()
                    }

                    SortType.COLOR.name -> {
                        viewModelTextNote.sortByColor()
                    }
                }
            }

        }
    }

    private fun getListSortType(list: List<NoteModel>?): List<NoteModel>? {
        return if (currentType == TypeColorNote.DEFAULT.name) list else list?.filter { data -> data.typeColor == currentType }
    }

    private fun navigateToEdit(idNote: Int? = null) {
        navigationWithAnim(
            R.id.editFragment, bundleOf(
                KEY_ID_DATA_NOTE to idNote,
                TYPE_ITEM_EDIT to if (isNoteType) Const.TYPE_NOTE else Const.TYPE_CHECKLIST,
            )
        )
    }
}