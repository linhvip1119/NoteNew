package com.example.colorphone.ui.edit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentEditNoteBinding
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.example.colorphone.ui.edit.adapter.MakeListAdapter
import com.example.colorphone.ui.edit.adapter.MakeListVH
import com.example.colorphone.ui.edit.utils.ListItemListener
import com.example.colorphone.ui.edit.utils.TextViewUndoRedo
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.KEY_ID_DATA_NOTE
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.showKeyboard
import com.wecan.inote.util.changeBackgroundColor
import com.wecan.inote.util.mapIdColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class BaseEditNote : BaseFragment<FragmentEditNoteBinding>(FragmentEditNoteBinding::inflate) {

    lateinit var adapter: MakeListAdapter

    var model: NoteModel = NoteModel()

    private var modelOld: NoteModel? = null

    var currentColor: String = TypeColorNote.BLUE.name

    var isNoteText: Boolean = true

    var isFromCreateNote: Boolean? = false

    private var _isAddNote: Boolean? = false

    private var idNoteEdited: Int = -1

    private var isFromWidget: Boolean? = false

    private var isFocusTittle = false

    private var isFocusContent = false

    var idReCreateNoteWidget: Int? = null

    private var helperTittle: TextViewUndoRedo? = null
    private var helperContent: TextViewUndoRedo? = null

    private var isBackPress = false

    var listCheckList = mutableListOf<CheckList>()

    @Inject
    lateinit var prefUtil: PrefUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArgumentsListener()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun handleArgumentsListener() {
//        isFromCreateNote = arguments?.getBoolean(TextFragment.ARG_CREATE_NOTE)
        _isAddNote = arguments?.getInt(KEY_ID_DATA_NOTE) == null
        idNoteEdited = arguments?.getInt(KEY_ID_DATA_NOTE, -1) ?: -1


//        isFromWidget = arguments?.getBoolean("ARG_FROM_WIDGET")

        isNoteText = arguments?.getString(Const.TYPE_ITEM_EDIT) == Const.TYPE_NOTE

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
            bindViewEditNote(model)
        }
        setupRecyclerView()
    }

    override fun onSubscribeObserver(view: View) {
        viewModelTextNote.itemWithIdsLD.observe(viewLifecycleOwner) {
            model = it
            bindViewEditNote(it)
        }
    }

    private fun bindViewEditNote(item: NoteModel?) {
        binding.apply {
            currentColor = item?.typeColor ?: TypeColorNote.A_ORANGE.name
            mapIdColor(currentColor, true) { idIcon, _, _, idColorBody, idBgTopBar ->
                ivTypeBox.setImageResource(idIcon)
                llItem.changeBackgroundColor(if (prefUtil.isDarkMode) R.color.bgEditNoteDark else idColorBody)
                llAppBar.setBackgroundResource(idColorBody)
                clTopBarMenu.setBackgroundResource(idBgTopBar)
            }
//            if (prefUtil.isPremium) {
//                ivLock.setImageResource(R.drawable.ic_lock_edit_custom)
//                ivLock.isSelected = item?.isLock() == true
//            } else {
//                ivLock.setImageResource(R.drawable.ic_lock_vip)
//            }
            if (item?.title.isNullOrEmpty()) {
                etTittle.hint = getString(R.string.headingLabel)
            } else {
                etTittle.setText(item?.title)
            }
            etContent.setText(item?.content)
            if (item?.listCheckList?.isNotEmpty() == true) {
                addListItem(true)
            }
        }
    }

    private fun setupRecyclerView() {
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
                this@BaseEditNote.moveToNext(position)
            }

            override fun textChanged(position: Int, text: String) {
                model.listCheckList?.get(position)?.body = text
            }

            override fun checkedChanged(position: Int, checked: Boolean) {
                try {
                    model.listCheckList?.get(position)?.checked = checked
                } catch (e: Exception) {
                    Log.i("TAG", "checkedChanged: fail")
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