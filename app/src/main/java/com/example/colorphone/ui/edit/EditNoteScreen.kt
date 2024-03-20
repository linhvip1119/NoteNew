package com.example.colorphone.ui.edit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.colorphone.R
import com.example.colorphone.adsConfig.AdsConstants
import com.example.colorphone.adsConfig.BannerAdsManager
import com.example.colorphone.adsConfig.InterAdsManager
import com.example.colorphone.adsConfig.InterstitialOnLoadCallBack
import com.example.colorphone.adsConfig.InterstitialOnShowCallBack
import com.example.colorphone.adsConfig.PlacementAds
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
import com.wecan.inote.util.inv
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.setOnNextAction
import com.wecan.inote.util.setPreventDoubleClick
import com.wecan.inote.util.setPreventDoubleClickScaleView
import com.wecan.inote.util.show
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

    private var interBackClick: InterAdsManager? = null

    private var interSaveClick: InterAdsManager? = null

    private var interBackCheckListClick: InterAdsManager? = null

    private var interSaveCheckListClick: InterAdsManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArgumentsListener()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun handleArgumentsListener() {

        _isAddNote = arguments?.getInt(KEY_ID_DATA_NOTE, -1) == -1

        idNoteEdited = arguments?.getInt(KEY_ID_DATA_NOTE, -1) ?: -1

        isTypeText = arguments?.getString(Const.TYPE_ITEM_EDIT) == Const.TYPE_NOTE
    }

    override fun init(view: View) {
        loadAdsBanner()
        initInterAds()
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
        checking("EditNote_Show", "EditList_Show")
    }

    private fun initInterAds() {
        activity?.let {
            interBackClick = InterAdsManager(
                it,
                AdsConstants.mapRemoteConfigAds[PlacementAds.PLACEMENT_EDIT_BACK],
                false,
                object : InterstitialOnLoadCallBack {}
            )

            interSaveClick = InterAdsManager(
                it,
                AdsConstants.mapRemoteConfigAds[PlacementAds.PLACEMENT_EDIT_SAVE],
                false,
                object : InterstitialOnLoadCallBack {}
            )

            interBackCheckListClick = InterAdsManager(
                it,
                AdsConstants.mapRemoteConfigAds[PlacementAds.PLACEMENT_EDIT_CHECK_BACK],
                false,
                object : InterstitialOnLoadCallBack {}
            )

            interSaveCheckListClick = InterAdsManager(
                it,
                AdsConstants.mapRemoteConfigAds[PlacementAds.PLACEMENT_EDIT_CHECK_SAVE],
                false,
                object : InterstitialOnLoadCallBack {}
            )
        }
    }

    private fun loadAdsBanner() {
        activity?.let {
            BannerAdsManager.loadBannerAds(
                it,
                PlacementAds.PLACEMENT_EDIT_INLINE_TOP,
                binding.iclBanner.flBanner,
                AdsConstants.POSITION_TOP_BANNER
            )

        }
    }

    private fun onListener() {
        binding.apply {

            ivTypeBox.setPreventDoubleClickScaleView {
                checking("EditNote_Label_Click", "EditList_Label_Click")
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
                    check(
                        if (isTypeText) {
                            when (it) {
                                TypeColorNote.A_ORANGE.name -> "EditNote_Label_Orange_Show"
                                TypeColorNote.B_GREEN.name -> "EditNote_Label_Green_Show"
                                TypeColorNote.BLUE.name -> "EditNote_Label_Blue_Show"
                                TypeColorNote.F_PRIMARY.name -> "EditNote_Label_Purple_Show"
                                TypeColorNote.BLINK.name -> "MainLabel_Pink_Click"
                                TypeColorNote.GRAY.name -> "EditNote_Label_Gray_Show"
                                TypeColorNote.D_RED.name -> "EditNote_Label_Red_Show"
                                else -> ""
                            }
                        } else {
                            when (it) {
                                TypeColorNote.A_ORANGE.name -> "EditList_Label_Orange_Click"
                                TypeColorNote.B_GREEN.name -> "EditList_Label_Green_Click"
                                TypeColorNote.BLUE.name -> "EditList_Label_Blue_Click"
                                TypeColorNote.F_PRIMARY.name -> "EditList_Label_Purple_Click"
                                TypeColorNote.BLINK.name -> "EditList_Label_Pink_Click"
                                TypeColorNote.GRAY.name -> "EditList_Label_Gray_Click"
                                TypeColorNote.D_RED.name -> "EditList_Label_Red_Click"
                                else -> ""
                            }
                        }
                    )
                }
                checking("EditNote_Label_Show", "EditList_Label_Show")
            }

            ivMenu.setPreventDoubleClickScaleView {
                checking("EditNote_Extend_Click", "EditList_Extend_Click")
                initiatePopupMenu()
            }

            tvAddItem.setPreventDoubleClickScaleView {
                addListItem()
            }

            etTittle.setPreventDoubleClick {
                checking("EditNote_Heading_Click", "EditList_Heading_Click")
            }

            etTittle.setOnNextAction {
                if (isTypeText) {
                    etContent.requestFocus()
                } else {
                    moveToNext(-1)
                }
            }

            etContent.setPreventDoubleClick {
                check("EditNote_Filing_Click")
            }

            etContent.doAfterTextChanged {
                handleEnableIconDo()
            }

            ivBackground.setPreventDoubleClick {
                checking("EditNote_Background_Click", "EditList_Background_Click")
                showBottomSheetBg(model.background ?: -1) {
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
                    Const.checking(
                        if (isTypeText) "EditNote_Background_Item_Click" else "EditList_Background_Item_Click",
                        bundleOf(
                            "EditNote_Background_Item_Click" to it.name
                        )
                    )
                }
            }

            activity?.onBackPressedDispatcher?.addCallback(this@EditNoteScreen, true) {
                showInter(
                    interBackClick,
                    interBackCheckListClick,
                    TYPE_BACK
                ) {
                    jobAddWidget?.cancel()
                    navController?.popBackStack()
                }
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
                showInter(
                    interSaveClick,
                    interSaveCheckListClick,
                    TYPE_SAVE
                ) {
                    jobAddWidget?.cancel()
                    navController?.popBackStack()
                }
            }
        }

        binding.ivReadMode.setOnClickListener {
            checking("EditNote_ReadMode_Click", "EditList_ReadMode_Click")
            onReadMode = !onReadMode
            handeReadMode()
        }
    }

    private fun showInter(
        interBack: InterAdsManager?,
        interSave: InterAdsManager?,
        type: Int,
        call: () -> Unit
    ) {
        activity?.let {
            if (model.typeItem == TypeItem.TEXT.name) {
                checkTypeAndShowAds(it, type, interBack, interSave, call)
            } else {
                checkTypeAndShowAds(it, type, interBack, interSave, call)
            }
        }
    }

    private fun checkTypeAndShowAds(
        it: FragmentActivity,
        type: Int,
        interBack: InterAdsManager?,
        interSave: InterAdsManager?,
        call: () -> Unit,
    ) = if (type == TYPE_BACK) {
        showAds(interBack, it, call)
    } else {
        showAds(interSave, it, call)
    }

    private fun showAds(
        interAds: InterAdsManager?,
        it: FragmentActivity,
        call: () -> Unit
    ) = interAds?.showInterstitialAd(
        it,
        {
            call.invoke()
        },
        object : InterstitialOnShowCallBack {
            override fun onAdDismissedFullScreenContent() {}

            override fun onAdFailedToShowFullScreenContent() {}

            override fun onAdShowedFullScreenContent() {
                call.invoke()
            }

        }
    )

    private fun showBottomSheetBg(currentBg: Int, colorClick: (Background) -> Unit) {
        val addPhotoBottomDialogFragment: BottomSheetBackground =
            BottomSheetBackground.newInstance(isTypeText, currentBg, colorClick)
        activity?.supportFragmentManager?.let {
            addPhotoBottomDialogFragment.show(
                it, "TAG"
            )
            checking("EditNote_Background_Show", "EditList_Background_Show")
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

        adapter = MakeListAdapter(
            elevation,
            model.listCheckList ?: arrayListOf(),
            object : ListItemListener {

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

    override fun onResume() {
        super.onResume()
        if (AdsConstants.isShowOpenAds) {
            binding.iclBanner.flBanner.inv()

        } else {
            binding.iclBanner.flBanner.show()
        }
    }

    companion object {
        const val TYPE_BACK = 0
        const val TYPE_SAVE = 1
    }
}