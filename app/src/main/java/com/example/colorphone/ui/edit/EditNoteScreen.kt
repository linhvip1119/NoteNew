package com.example.colorphone.ui.edit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentEditNoteBinding
import com.example.colorphone.model.Background
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.edit.adapter.MakeListAdapter
import com.example.colorphone.ui.edit.bottomBackgroundEdit.BottomSheetBackground
import com.example.colorphone.ui.edit.utils.ListItemListener
import com.example.colorphone.ui.edit.utils.TextViewUndoRedo
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.KEY_ID_DATA_NOTE
import com.example.colorphone.util.RequestPinWidget
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.hideKeyboard
import com.wecan.inote.util.changeBackgroundColor
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.setOnNextAction
import com.wecan.inote.util.setPreventDoubleClick
import com.wecan.inote.util.setPreventDoubleClickScaleView
import com.wecan.inote.util.showCustomToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class EditNoteScreen : BaseFragment<FragmentEditNoteBinding>(FragmentEditNoteBinding::inflate) {

    lateinit var adapter: MakeListAdapter

    var model: NoteModel = NoteModel()

    var modelOld: NoteModel? = null

    var currentColor: String = TypeColorNote.BLUE.name

    var isTypeText: Boolean = true

    private var _isAddNote: Boolean = false

    private var idNoteEdited: Int = -1

    private var isFromWidget: Boolean? = false

    var isFocusTittle = false

    var isFocusContent = false

    var idReCreateNoteWidget: Int? = null

    var helperTittle: TextViewUndoRedo? = null
    var helperContent: TextViewUndoRedo? = null

    private var isBackPress = false

    var listCheckList = mutableListOf<CheckList>()

    var onReadMode = false

    private var jobAddWidget: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArgumentsListener()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun handleArgumentsListener() {
//        isFromCreateNote = arguments?.getBoolean(TextFragment.ARG_CREATE_NOTE)
        _isAddNote = arguments?.getInt(KEY_ID_DATA_NOTE, -1) == -1
        idNoteEdited = arguments?.getInt(KEY_ID_DATA_NOTE, -1) ?: -1


//        isFromWidget = arguments?.getBoolean("ARG_FROM_WIDGET")

        isTypeText = arguments?.getString(Const.TYPE_ITEM_EDIT) == Const.TYPE_NOTE

//        val idsFromWidget = arguments?.getInt(KEY_IDS_NOTE_FROM_WIDGET)
//
//        if (isFromWidget == true) {
//            idsFromWidget?.let {
//                viewModelTextNote.getNoteWithIds(it)
//            }
//        }
//
//        val id = arguments?.getInt(DATA_ITEM_NOTE)
//        if (id != null) {
//            viewModelTextNote.getNoteWithIds(id)
//        }
//
//        currentColor = arguments?.getString(CURRENT_TYPE) ?: TypeColorNote.A_ORANGE.name
//
//        arguments?.getInt(UPDATE_WIDGET_IDS).apply {
//            idReCreateNoteWidget = if (this == 0) null else this
//        }
    }

    override fun init(view: View) {
        handleViewText()
        if (idNoteEdited != -1) {
            viewModelTextNote.getNoteWithIds(idNoteEdited)
        } else {
            setupRecyclerView()
            bindViewEditNote(model)
        }
        onListener()
        handleRedoUndo()
        handleEnableIconDo()
    }

    private fun onListener() {
        binding.apply {

            ivTypeBox.setPreventDoubleClickScaleView {
                showBottomSheet(currentColor, Const.EDIT_NOTE_SCREEN) {
                    currentColor = it
                    mapIdColor(
                        nameColor = it, isGetIcon = true
                    ) { icon, _, _, idColorBody, idBgTopBar ->
                        binding.apply {
                            ivTypeBox.setImageResource(icon)
                            if (model?.background != null) {
                                try {
                                    llItem.setBackgroundResource(model.background!!)
                                    appBar.setBackgroundColor(Color.TRANSPARENT)
                                    clTopBarMenu.setBackgroundColor(Color.TRANSPARENT)
                                } catch (e: Exception) {
                                    llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                                    clTopBarMenu.setBackgroundResource(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)

                                }
                            } else {
                                llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                                clTopBarMenu.setBackgroundResource(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                            }
                        }
                    }
                }
            }

            ivMenu.setPreventDoubleClickScaleView {
                initiatePopupMenu()
            }

            tvAddItem.setPreventDoubleClickScaleView {
                addListItem()
            }

            etTittle.setOnClickListener {

            }

            etTittle.setOnNextAction {
                if (isTypeText) {
                    etContent.requestFocus()
                } else {
                    moveToNext(-1)
                }
            }

            etContent.setOnClickListener {
            }

            etContent.doAfterTextChanged {
                handleEnableIconDo()
            }

            ivBackground.setPreventDoubleClick {
                showBottomSheetBg() {
                    if (it.url != -1) {
                        model?.background = it.url
                        binding.apply {
                            try {
                                llItem.setBackgroundResource(it.url)
                                appBar.setBackgroundColor(Color.TRANSPARENT)
                                clTopBarMenu.setBackgroundColor(Color.TRANSPARENT)
                            } catch (_: Exception) {
                            }
                        }
                    } else {
                        model.background = null
                        mapIdColor(model.typeColor, true) { idIcon, _, _, idColorBody, idBgTopBar ->
                            ivTypeBox.setImageResource(idIcon)
                            llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                            clTopBarMenu.setBackgroundResource(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                        }
                    }
                }
            }

            activity?.onBackPressedDispatcher?.addCallback(this@EditNoteScreen, true) {
                jobAddWidget?.cancel()
                navController?.popBackStack()
            }
        }

        binding.etTittle.doAfterTextChanged { text ->
            model.title = requireNotNull(text).trim().toString()
            handleEnableIconDo()
        }

        binding.ivVComplete.setOnClickListener {
            binding.apply {
                etContent.clearFocus()
                etTittle.clearFocus()
                activity?.hideKeyboard()
            }
            isBackPress = true
            handleSaveNote {
//                    showAds()
                navController?.popBackStack()
                jobAddWidget?.cancel()
            }
        }

        binding.ivReadMode.setOnClickListener {
            onReadMode = !onReadMode
            handeReadMode()
        }

    }

    private fun showBottomSheetBg(colorClick: (Background) -> Unit) {
        val addPhotoBottomDialogFragment: BottomSheetBackground = BottomSheetBackground.newInstance(colorClick)
        activity?.supportFragmentManager?.let {
            addPhotoBottomDialogFragment.show(
                it, "TAG"
            )
        }
    }

    fun handleSaveNote(onComplete: () -> Unit) {
        lifecycleScope.launch {
            saveNote()
            delay(200)
            onComplete.invoke()
        }
    }

    private fun saveNote() {
        val typeDefault = if (isTypeText) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name
        if (_isAddNote) {
            viewModelTextNote.addNote(getDataNote("0", "-1", typeDefault)) {
                model?.ids = it
            }
//            idReCreateNoteWidget?.let {
//                updateWidgetWithId(getDataNote("0", "0", model?.typeItem ?: typeDefault))
//            }
            _isAddNote = false
        } else {
            handleUpdateNote(typeDefault)
        }
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.itemWithIdsLD.observe(viewLifecycleOwner) {
            model = it
            setupRecyclerView()
            bindViewEditNote(it)
        }
    }

    private fun bindViewEditNote(item: NoteModel) {
        binding.apply {
            currentColor = item.typeColor
            mapIdColor(currentColor, true) { idIcon, _, _, idColorBody, idBgTopBar ->
                ivTypeBox.setImageResource(idIcon)
                if (item.background != null) {
                    try {
                        llItem.setBackgroundResource(item.background!!)
                        appBar.setBackgroundColor(Color.TRANSPARENT)
                        clTopBarMenu.setBackgroundColor(Color.TRANSPARENT)
                    } catch (e: Exception) {
                        llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                        clTopBarMenu.setBackgroundResource(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                    }
                } else {
                    llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                    clTopBarMenu.setBackgroundResource(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                }
            }
            if (item?.title.isNullOrEmpty()) {
                etTittle.hint = getString(R.string.headingLabel)
            } else {
                etTittle.setText(item?.title)
            }
            etContent.setText(item?.content)
            if (item?.listCheckList?.isEmpty() == true) {
                addListItem(true)
            }
            if (_isAddNote) {
                etContent.requestFocus()
            }
        }
    }

    fun addPhotoWidget(note: NoteModel) {
        if (jobAddWidget?.isActive == true) {
            jobAddWidget?.cancel()
        }
        jobAddWidget = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RequestPinWidget.noteWidgetSuccess.filter { state -> state }.take(1).collectLatest {
                    delay(400)
                    context?.let { ct ->
                        Toast(context).showCustomToast(ct, ct.getString(R.string.widgetAddSuccess))
                    }
                    delay(200)
                    updateWidgetWithId(model)
                }
            }
        }

        addWidget(note) { isSuccess ->
            if (!isSuccess) {
                jobAddWidget?.cancel()
            }
        }
    }

    fun setupRecyclerView() {

        model.typeItem = if (isTypeText) TypeItem.TEXT.name else TypeItem.CHECK_LIST.name
        val unit = resources.getDimension(R.dimen.dp1)
        val elevation = unit * 2

        adapter = MakeListAdapter(elevation, model.listCheckList ?: arrayListOf(), object : ListItemListener {

            override fun delete(position: Int) {
                adapter.notifyItemRemoved(position)
                if (position > -1 && position < (model.listCheckList?.size ?: 0)) {
                    model.listCheckList?.removeAt(position)
                }
            }

            override fun moveToNext(position: Int) {
                this@EditNoteScreen.moveToNext(position)
            }

            override fun textChanged(position: Int, text: String) {
                model.listCheckList?.get(position)?.body = text
            }

            override fun checkedChanged(position: Int, checked: Boolean) {
                try {
                    model.listCheckList?.get(position)?.checked = checked
                } catch (_: Exception) {
                }
            }

            override fun clickView(viewClick: String) {

            }
        })

        binding.rvCheckList.adapter = adapter
    }

    companion object {

    }
}