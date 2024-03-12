package com.example.colorphone.ui.settings.widget.listNoteAdd

import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentListNoteAddWidgetBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.settings.recyclerBin.adapter.RecycleBinAdapter
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.TypeView
import com.wecan.inote.util.gone
import com.wecan.inote.util.show
import com.wecan.inote.util.showCustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch


class ListNoteAddWidget : BaseFragment<FragmentListNoteAddWidgetBinding>(FragmentListNoteAddWidgetBinding::inflate) {

    private val _adapterText by lazy { RecycleBinAdapter() }
    private var typeListItem = TypeItem.TEXT.name

    override fun init(view: View) {
        setTypeRecyclerView()
        initView()
        setUpRecyclerView(prefUtil.typeView)
        onListener()
        getData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getData() {
        viewModelTextNote.getListTextNote(typeListItem, prefUtil.sortType)
    }

    private fun onListener() {
        binding.apply {
            tvBack.setOnClickListener {
                jobAddWidget?.cancel()
                navController?.popBackStack()
            }

            tvText.setOnClickListener {
                lifecycleScope.launch {
                    tvText.isSelected = true
                    tvChecklist.isSelected = false
                    _adapterText.typeItem = TypeItem.TEXT.name
                    typeListItem = TypeItem.TEXT.name
                    getData()
                }
            }
            tvChecklist.setOnClickListener {
                lifecycleScope.launch {
                    tvText.isSelected = false
                    tvChecklist.isSelected = true
                    _adapterText.typeItem = TypeItem.CHECK_LIST.name
                    typeListItem = TypeItem.CHECK_LIST.name
                    getData()
                }
            }
        }

        _adapterText.mOnClickItem = {
            lifecycleScope.launch(Dispatchers.Main) {
                setVisibleProgress()
                addPhotoWidget(it)
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            jobAddWidget?.cancel()
            navController?.popBackStack()
        }
    }

    private var jobAddWidget: Job? = null

    private fun addPhotoWidget(note: NoteModel) {
        if (jobAddWidget?.isActive == true) {
            jobAddWidget?.cancel()
        }
        jobAddWidget = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RequestPinWidget.noteWidgetSuccess.filter { state -> state }.take(1).collectLatest {
                    delay(1500)
                    binding.apply {
                        viewLoadWidget.gone()
                        progressBar.gone()
                    }
                    context?.let { ct ->
                        Toast(context).showCustomToast(ct, ct.getString(R.string.widgetAddSuccess))
                    }
                    delay(200)
                    updateWidgetWithId(note)
                }
            }
        }
        addWidget(note) {
            if (!it) {
                jobAddWidget?.cancel()
            }
        }
    }

    private fun setVisibleProgress() {
        binding.apply {
            viewLoadWidget.show()
            progressBar.show()
        }
    }

    private fun initView() {
        binding.apply {
            tvText.isSelected = true
        }
    }

    private fun setUpRecyclerView(type: String) {
        binding.rvListText.apply {
            layoutManager = when (type) {
                TypeView.List.value -> GridLayoutManager(context, 1)
                TypeView.Grid.value -> GridLayoutManager(context, 2)
                TypeView.Details.value -> StaggeredGridLayoutManager(
                    2, StaggeredGridLayoutManager.VERTICAL
                )

                else -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            adapter = _adapterText
        }
    }

    private fun setTypeRecyclerView() {
        val currentTypeValue = prefUtil.typeView
        _adapterText.typeView = when (currentTypeValue) {
            TypeView.List.value -> TypeView.List.type
            TypeView.Details.value -> TypeView.Details.type
            else -> TypeView.Grid.type
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.textNoteLiveData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                _adapterText.submitList(it)
                toggleRecyclerView(it ?: listOf())
            }
        }
    }

    private fun toggleRecyclerView(noteList: List<NoteModel>) {
        binding.rvListText.isVisible = noteList.isNotEmpty()
        binding.clNoDataRecycler.isVisible = noteList.isEmpty()
    }
}