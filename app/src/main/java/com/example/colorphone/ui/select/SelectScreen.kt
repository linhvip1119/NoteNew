package com.example.colorphone.ui.select

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentSelectScreenBinding
import com.example.colorphone.databinding.PopupExtendSelectedBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.main.listNote.adapter.TextAdapter
import com.example.colorphone.util.Const.CURRENT_TYPE_ITEM
import com.example.colorphone.util.Const.NOTE_FROM_LONG_CLICK
import com.example.colorphone.util.Const.POSITION_SELECTED
import com.example.colorphone.util.Const.SELECTED_SCREEN
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.TypeView
import com.example.colorphone.util.ext.getCurrentTimeToLong
import com.example.colorphone.util.ext.hideKeyboard
import com.example.colorphone.util.ext.isNotNullOfEmpty
import com.wecan.inote.util.gone
import com.wecan.inote.util.px
import com.wecan.inote.util.setOnClickAnim
import com.wecan.inote.util.setPreventDoubleClick
import com.wecan.inote.util.show
import com.wecan.inote.util.showCustomToast
import com.wecan.inote.util.showCustomToastPinned
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.Locale

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SelectScreen() :
    BaseFragment<FragmentSelectScreenBinding>(FragmentSelectScreenBinding::inflate) {

    private lateinit var _adapterText: TextAdapter

    private var _listLocal: ArrayList<NoteModel>? = arrayListOf()

    private var isTextType: String = TypeItem.TEXT.name

    private var listJustArchived = arrayListOf<NoteModel>()

    private var listJustDelete = arrayListOf<NoteModel>()

    private var listChangeColor = arrayListOf<NoteModel>()

    private var typeChange: String? = null

    private var idsFromLongClickItem: Int? = null

    private var posScrollSelected: Int? = null

    private var mDropdown: PopupWindow? = null

    private var mInflater: LayoutInflater? = null


    override fun init(view: View) {
        _adapterText = TextAdapter(true)
        _adapterText.isDarkTheme = prefUtil.isDarkMode
        _adapterText.typeItem = isTextType
        initView()
        setTypeRecyclerView()
        setUpRecyclerView(prefUtil.typeView)
        onBackPressHandle()
        onSearchNote()
        onListener()
        viewModelTextNote.getListTextNote(isTextType, prefUtil.sortType)
        /*        val account = context?.let { GoogleSignIn.getLastSignedInAccount(it) }
                if (account != null) {
                    initializeDriveClient(account)
                }
                if (prefUtil.isPremium) {
                    binding.llTextLockNow.gone()
                } else {
                    binding.llTextLockNow.show()
                }*/
    }

    private fun setTypeRecyclerView() {
        val currentTypeValue = prefUtil.typeView
        _adapterText.typeView = when (currentTypeValue) {
            TypeView.List.value -> TypeView.List.type
            TypeView.Details.value -> TypeView.Details.type
            else -> TypeView.Grid.type
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTextType =
            arguments?.getString(CURRENT_TYPE_ITEM) ?: TypeItem.TEXT.name
        idsFromLongClickItem = arguments?.getInt(NOTE_FROM_LONG_CLICK)
        posScrollSelected = arguments?.getInt(POSITION_SELECTED)
    }

    private fun onBackPressHandle() {
        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            activity?.hideKeyboard()
            jobAddWidget?.cancel()
            navController?.popBackStack()
        }
    }

    private fun onListener() {
        binding.apply {
            ivPinned.setOnClickAnim {
                handlePinnedNote()
            }
            ivSelectAll.setOnClickAnim {
                _adapterText.selectAllItem(!ivSelectAll.isSelected)
            }
            tvSelected.setOnClickListener {
                activity?.hideKeyboard()
                navController?.popBackStack()
            }

            ivCloseSearch.setOnClickAnim {
                edtSearch.text?.clear()
                activity?.hideKeyboard()
            }

            ivArchive.setOnClickAnim {
                handleArchive()
            }

            ivDelete.setOnClickAnim {
                handleDelete()
            }

            ivColor.setOnClickAnim {
                showBottomSheet(false, fromScreen = SELECTED_SCREEN) {
                    handleChangeColors(it)
                }
            }

            ivExtend.setOnClickAnim {
                initPopupExtendMenu()
            }

            iclToastCustom.tvUndo.setOnClickListener {
                handleClickUndo()
            }

            ivReminder.setOnClickAnim {
                handleClickReminder()
            }
        }

        _adapterText.onSelectItem = { size, item ->
            handleEnableIconButton(size, item)
        }
        _adapterText.onClickSelectedEvent = {
        }
    }

    private fun initPopupExtendMenu(): PopupWindow? {
        try {
            mInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val menuBinding = PopupExtendSelectedBinding.inflate(mInflater!!)

            menuBinding.root.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )

            menuBinding.apply {
                item1.apply {
                    ivIcon.setImageResource(R.drawable.ic_add_to_home)
                    tvText.text = getString(R.string.addToHome)
                    root.setPreventDoubleClick {
                        mDropdown?.dismiss()
                        _adapterText.getListSelected().firstOrNull()?.let { it1 -> addPhotoWidget(it1) }
                    }
                }
                item2.apply {
                    ivIcon.setImageResource(R.drawable.ic_share)
                    tvText.text = getString(R.string.share)
                    mDropdown?.dismiss()
                    root.setPreventDoubleClick {
                        mDropdown?.dismiss()
                        shareNote()
                    }
                }
            }

            mDropdown = PopupWindow(
                menuBinding.root, FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, true
            )
            mDropdown?.showAsDropDown(binding.ivExtend, (-100).px, (-150).px)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return mDropdown
    }

    private var jobAddWidget: Job? = null

    private fun addPhotoWidget(note: NoteModel) {

        if (jobAddWidget?.isActive == true) {
            jobAddWidget?.cancel()
        }

        jobAddWidget = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RequestPinWidget.noteWidgetSuccess.filter { state -> state }.take(1).collectLatest {
                    context?.let { ct ->
                        Toast(context).showCustomToast(ct, ct.getString(R.string.widgetAddSuccess))
                    }
                }
            }
        }

        addWidget(note) {
            if (!it) {
                jobAddWidget?.cancel()
            }
        }

    }

    private fun handleChangeColors(typeColor: String) {
        lifecycleScope.launch {
            typeChange = IS_CHANGE_COLOR
            val listBefore = arrayListOf<NoteModel>()
            for (item in _adapterText.currentList) {
                listBefore.add(
                    NoteModel(
                        item.ids,
                        item.token,
                        item.isUpdate,
                        item.content,
                        item.title,
                        item.typeItem, item.listCheckList,
                        item.dateCreateNote,
                        item.isPinned,
                        item.datePinned,
                        item.typeColor,
                        item.modifiedTime,
                        item.isArchive,
                        item.isDelete,
                        item.dateReminder,
                        item.typeRepeat,
                        item.repeatValue,
                        item.isAlarm,
                        item.isLock,
                        item.isSelected
                    )
                )
            }
            for (item in _adapterText.getListSelected()) {
                listChangeColor.add(
                    NoteModel(
                        item.ids,
                        item.token,
                        item.isUpdate,
                        item.content,
                        item.title, item.typeItem, item.listCheckList,
                        item.dateCreateNote,
                        item.isPinned,
                        item.datePinned,
                        item.typeColor,
                        item.modifiedTime,
                        item.isArchive,
                        item.isDelete,
                        item.dateReminder,
                        item.typeRepeat,
                        item.repeatValue,
                        item.isAlarm,
                        item.isLock,
                        item.isSelected
                    )
                )
            }
            _adapterText.getListSelected().forEach {
                viewModelTextNote.updateColorNote(it, typeColor)
            }
            _adapterText.updateListAfterChangeColor(typeColor, listBefore)
            showToast(getString(R.string.colorChangeLabel).plus("."))
        }
    }

    /*    private fun lockNote(noteModel: NoteModel) {
            binding.apply {
                lifecycleScope.launch {
                    viewModelTextNote.updateNote(noteModel) {}
                    _adapterText.notifyDataSetChanged()
                    updateWidgetWithId(noteModel)
                    context?.let { ct ->
                        Toast(context).showCustomToastLocked(ct, noteModel.isLock())
                    }
                }
            }
        }*/

    private fun handleClickReminder() {
        lifecycleScope.launch {
            navigationWithAnim(
                R.id.action_selectFragment_to_reminderFragment, bundleOf(
                    ITEM_FROM_SELECTED_SCREEN to _adapterText.getListSelected()
                            .firstOrNull()?.ids
                )
            )
        }
    }

    private fun handleArchive() {
        lifecycleScope.launch {
            typeChange = IS_ARCHIVED
            _adapterText.getListSelected().forEach {
                viewModelTextNote.archiveNote(it)
                listJustArchived.add(it)
            }
            _adapterText.updateListAfterArchive()
            showToast(getString(R.string.noteArchiveLabel).plus("."))
        }
    }

    private fun isSelectNoteLock(): Boolean {
        return _adapterText.getListSelected().any { it.isLock() }
    }

    /*private fun handleNoteLocked(isChange: Boolean = false, onCompleted: () -> Unit) {
        if (isSelectNoteLock() && prefUtil.isSetPin || isChange) {
            context?.showDialogChangePass(prefUtil,
                                          onConfirm = {
                                              onCompleted.invoke()
                                          }, onForgot = {
                    context?.showDialogVerify(prefUtil) {
                        val random = (1000..9999).random()
                        viewModelTextNote.sendMail(
                            random.toString(),
                            prefUtil.email.toString()
                        )
                        context?.showDialogOtp(random.toString()) {
                            context?.showDialogSetPassword {
                                prefUtil.pin = it
                                context?.showDialogComplete(getString(R.string.set_password_complete))
                            }
                        }
                    }
                })
        } else {
            onCompleted.invoke()
        }
    }*/

    private fun handleDelete() {
        lifecycleScope.launch {
            typeChange = IS_DELETE
            _adapterText.getListSelected().forEach {
                viewModelTextNote.deleteLocalNote(it)
                listJustDelete.add(it)
            }
            _adapterText.updateListAfterDelete()
            showToast(getString(R.string.noteDeletedLabel).plus("."))
        }
    }

    private fun handleEnableIconButton(size: Int, item: NoteModel?) {
        binding.apply {
            tvSelected.text = "$size ${context?.getString(R.string.selectedLabel)}"

            ivReminder.isVisible = size == 1
            ivPinned.isVisible = size == 1
            ivPinned.isSelected = item?.isPinned == true

            ivSelectAll.isVisible = size > 1
            isSelectAll(size == _adapterText.currentList.size)

            ivArchive.handleSelected(size > 0)
            ivDelete.handleSelected(size > 0)
            ivColor.handleSelected(size > 0)
            ivExtend.handleSelected(size == 1)
        }
    }

    private fun isSelectAll(isSelect: Boolean) {
        binding.ivSelectAll.apply {
            isSelected = isSelect
            setImageResource(if (isSelect) R.drawable.ic_select_all_on else R.drawable.ic_select_all_off)
        }
    }

    private fun TextView.handleSelected(isSelect: Boolean) {
        isEnabled = isSelect
        isSelected = isSelect
        alpha = if (isSelect) 1F else 0.4F
    }

    private fun handleClickUndo() {
        binding.iclToastCustom.root.isVisible = false
        when (typeChange) {
            IS_ARCHIVED -> {
                lifecycleScope.launch {
                    listJustArchived.forEach {
                        viewModelTextNote.updateNote(it.apply { isArchive = false }) {}
                    }
                    _adapterText.revertListArchive()
                }
            }

            IS_DELETE -> {
                lifecycleScope.launch {
                    listJustDelete.forEach {
                        viewModelTextNote.updateNote(it.apply { isDelete = false }) {}
                    }
                    _adapterText.revertListArchive()
                }
            }

            IS_CHANGE_COLOR -> {
                lifecycleScope.launch {
                    listChangeColor.forEach {
                        viewModelTextNote.updateNote(it) {}
                    }
                    _adapterText.revertListChangeColor()
                    delay(300)
                    _adapterText.notifyItemRangeChanged(0, _adapterText.itemCount)
                }
            }
        }
    }

    private fun handlePinnedNote() {
        binding.apply {
            lifecycleScope.launch {
                ivPinned.isSelected = !ivPinned.isSelected
                _adapterText.getListSelected().firstOrNull { it.isSelected }?.let { item ->
                    viewModelTextNote.updateNote(item.apply {
                        isPinned = ivPinned.isSelected
                        datePinned = getDatePinned(item, ivPinned.isSelected)
                    }) {}
                }
                _adapterText.notifyDataSetChanged()
                activity?.let { act ->
                    Toast(context).showCustomToastPinned(
                        act,
                        ivPinned.isSelected
                    )
                }
            }
        }
    }

    private fun getDatePinned(model: NoteModel, isPinned: Boolean): Long? {
        return if (isPinned) {
            if (model.datePinned.toString().isNotNullOfEmpty()) {
                model.datePinned
            } else {
                getCurrentTimeToLong()
            }
        } else {
            null
        }
    }

    private fun shareNote() {
        val item = _adapterText.getListSelected().firstOrNull()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, item?.title?.plus("\n").plus(item?.content))
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun showToast(message: String) {
        binding.apply {
            lifecycleScope.launch {
                iclToastCustom.tvContent.text = message
                iclToastCustom.root.isVisible = true
                delay(2500)
                iclToastCustom.root.isVisible = false
                listChangeColor.clear()
                listJustArchived.clear()
                listJustDelete.clear()
            }

        }
    }

    private fun initView() {
        binding.apply {
            ivReminder.gone()
            llBottomSelected.show()
            tvSelected.text = "0 ${context?.getString(R.string.selectedLabel)}"
            TextViewCompat.setTextAppearance(tvSelected, R.style.fontSelected)
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
            adapter = _adapterText
        }
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.textNoteLiveData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                _listLocal?.clear()
                it?.forEach { note ->
                    _listLocal?.add(if (note.ids == idsFromLongClickItem) note.apply {
                        isSelected = true
                    } else note)
                }
                idsFromLongClickItem = null
                val query = binding.edtSearch.text.toString().lowercase(Locale.getDefault())
                filterWithQuery(query)
            }
        }
    }

    private fun onSearchNote() {
        binding.edtSearch.doOnTextChanged { char, _, _, _ ->
            val query = char.toString().lowercase(Locale.getDefault())
            filterWithQuery(query)
        }
    }

    private fun filterWithQuery(query: String) {
        if (query.isNotEmpty()) {
            val filteredList: List<NoteModel> = onFilterChanged(query)
            _adapterText.submitList(filteredList)
            toggleRecyclerView(filteredList)
        } else if (query.isEmpty()) {
            _adapterText.submitList(_listLocal)
            _adapterText.notifyDataSetChanged()
            toggleRecyclerView(_listLocal ?: listOf())
        }
        posScrollSelected?.let {
            binding.rv.scrollToPosition(it)
            posScrollSelected = null
        }
        handleEnableIconButton(
            _adapterText.getListSelected().size,
            _adapterText.getListSelected().firstOrNull()
        )
    }

    private fun onFilterChanged(filterQuery: String): List<NoteModel> {
        val filteredList = ArrayList<NoteModel>()
        for (currentSport in _listLocal ?: listOf()) {
            if (currentSport.title?.lowercase(Locale.getDefault())
                            ?.contains(filterQuery) == true || currentSport.content?.lowercase(Locale.getDefault())
                            ?.contains(filterQuery) == true
            ) {
                filteredList.add(currentSport)
            }
        }
        return filteredList
    }

    private fun toggleRecyclerView(noteList: List<NoteModel>) {
        binding.rv.isVisible = noteList.isNotEmpty()
        binding.clNoData.isVisible = noteList.isEmpty()
    }

    companion object {
        const val IS_ARCHIVED = "IS_CHANGE_ARCHIVE"
        const val IS_DELETE = "IS_DELETE"
        const val IS_CHANGE_COLOR = "IS_CHANGE_COLOR"
        const val ITEM_FROM_SELECTED_SCREEN = "ITEM_FROM_SELECTED_SCREEN"
    }
}