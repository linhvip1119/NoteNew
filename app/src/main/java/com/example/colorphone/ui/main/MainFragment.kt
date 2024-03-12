package com.example.colorphone.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentMainBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.model.NoteModel
import com.example.colorphone.model.NoteType
import com.example.colorphone.ui.MainActivity
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.ui.main.adapter.DashBoardPagerAdapter
import com.example.colorphone.ui.settings.googleDriver.helper.GoogleDriveApiDataRepository
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.TYPE_ITEM_EDIT
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.TypeItem
import com.example.colorphone.util.ext.getCurrentTimeToLong
import com.example.colorphone.util.ext.hideKeyboard
import com.example.colorphone.util.ext.loadUrl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.api.services.drive.Drive
import com.wecan.inote.util.getBgBottomBarMain
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.px
import com.wecan.inote.util.setOnClickAnim
import com.wecan.inote.util.setPreventDoubleClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    private val _noteTypeViewModel: BottomSheetViewModel by viewModels()

    var currentNote = TEXT_FM

    private var isInitialization = false

    private val listCountRate = arrayListOf(1, 7, 21, 45)

    var mDropdown: PopupWindow? = null

    var mInflater: LayoutInflater? = null

    private var fromToolsWidget: String? = null

    private var idFromStickerWidget: Int = -1

    private var typeItemFromWidget: String = TypeItem.TEXT.name

    override fun onGoogleDriveSignedInFailed(exception: ApiException?) {

    }

    override fun onGoogleDriveSignedInSuccess(driveApi: Drive?) {
        repository = GoogleDriveApiDataRepository(driveApi)
        prefUtil.statusEmailUser?.avatar?.let { binding.ivProfile.loadUrl(it, 120.px) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return if (!isInitialization) {
            isInitialization = true
//            updateApp()
            super.onCreateView(inflater, container, savedInstanceState)

        } else {
            context?.theme?.applyStyle(MainActivity.themesList[MainActivity.themeIndex], true)
            binding.root
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(Const.KEY_CREATE_NOTE_TOOLS_WIDGET)?.let {
            fromToolsWidget = it
        }
        arguments?.getInt(Const.ID_NAVIGATE_EDIT_FROM_ITEM_WIDGET, -1)?.let {
            idFromStickerWidget = it
        }
        arguments?.getString(TYPE_ITEM_EDIT)?.let {
            typeItemFromWidget = it
        }
        initToolsWidget()
        initFromStickerWidget()
    }

    private fun initFromStickerWidget() {
        if (idFromStickerWidget != -1) {
            val typeEdit = if (typeItemFromWidget == TypeItem.TEXT.name) Const.TYPE_NOTE else Const.TYPE_CHECKLIST
            navigationWithAnim(R.id.editFragment, bundleOf(TYPE_ITEM_EDIT to typeEdit, Const.KEY_ID_DATA_NOTE to idFromStickerWidget))
        }
    }

    private fun initToolsWidget() {
        if (!fromToolsWidget.isNullOrEmpty()) {
            when (fromToolsWidget) {
                Const.TEXT_SCREEN -> {
                    currentNote = TEXT_FM
                    navigationWithAnim(R.id.editFragment, bundleOf(TYPE_ITEM_EDIT to Const.TYPE_NOTE))
                }

                Const.CHECK_LIST_SCREEN -> {
                    currentNote = CHECKLIST_FM
                    navigationWithAnim(R.id.editFragment, bundleOf(TYPE_ITEM_EDIT to Const.TYPE_CHECKLIST))
                }

                Const.SETTING_SCREEN -> navigationWithAnim(R.id.action_mainFragment_to_settingFragment)
            }
            fromToolsWidget = null
        }
    }

    override fun init(view: View) {
        initView()
        addListColorType()
        initBottomBar()
        initViewPager()
        onListener()
        onSearchNote()
        setUpGoogle()
    }

    private fun initView() {
        binding.ivAllBox.isSelected = false
        currentType?.let {
            mapIdColor(it, true) { idColor: Int, _: Int, _: Int, _: Int, _ ->
                binding.ivAllBox.setImageResource(idColor)
            }
        }
        getBgBottomBarMain(prefUtil.themeColor) {
            bgSelected = it
        }
        val account = context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account == null) {
            binding.ivProfile.setImageResource(R.drawable.ic_profile)
        } else {
            initializeDriveClient(account)
        }
    }

    private fun onListener() {
        binding.apply {
            flNote.setOnClickListener {
                if (currentNote != TEXT_FM) {
                    currentNote = TEXT_FM
                    initBottomBar()
                }
            }
            flChecklist.setOnClickListener {
                if (currentNote != CHECKLIST_FM) {
                    currentNote = CHECKLIST_FM
                    initBottomBar()
                }
            }

            binding.apply {
                ivCloseSearch.setPreventDoubleClick {
                    edtSearch.text?.clear()
                    edtSearch.clearFocus()
                    activity?.hideKeyboard()
                }
            }

            ivMenu.setOnClickAnim {
                initiatePopupMenu()
            }
            ivProfile.setOnClickListener {
                navigationWithAnim(R.id.action_mainFragment_to_settingFragment)
            }

            ivSync.setPreventDoubleClick {
                if (prefUtil.statusEmailUser == null) {
                    startGoogleDriveSignIn()
                } else {
                    handleSyncData {
                        prefUtil.lastSync =
                            System.currentTimeMillis()
                        navigationWithAnim(R.id.action_mainFragment_to_settingFragment)
                    }
                }
            }

            binding.ivAllBox.setOnClickAnim {
                showBottomSheet(currentType, Const.MAIN_SCREEN) {
                    mapIdColor(nameColor = it, isGetIcon = true) { icon, _, _, _, _ ->
                        binding.ivAllBox.setImageResource(icon)
                    }
                    shareViewModel.setFilterColor(it)
                }
            }

            ivFloatButton.setOnClickAnim {
                navigateToCreateNote()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            activity?.hideKeyboard()
            activity?.finish()
        }
    }

    private fun onSearchNote() {
        binding.edtSearch.doOnTextChanged { char, _, _, _ ->
            val query = char.toString().lowercase(Locale.getDefault())
            shareViewModel.setSearchText(query)
        }
    }

    private fun navigateToCreateNote() {
        val currentType = if (binding.vp2.currentItem == 0) Const.TYPE_NOTE else Const.TYPE_CHECKLIST
        navigationWithAnim(R.id.editFragment, bundleOf(TYPE_ITEM_EDIT to currentType))
    }

    override fun onSubscribeObserver(view: View) {
    }

    private var bgSelected = R.drawable.bg_bottom_bar_blue

    private fun initBottomBar() {
        binding.apply {
            val isSelectNote = currentNote == TEXT_FM
            flNote.setBackgroundResource(if (isSelectNote) bgSelected else Color.TRANSPARENT)
            flChecklist.setBackgroundResource(if (!isSelectNote) bgSelected else Color.TRANSPARENT)
            tvNote.isSelected = isSelectNote
            tvNote.text = if (isSelectNote) getString(R.string.note) else ""
            tvChecklist.isSelected = !isSelectNote
            tvChecklist.text = if (!isSelectNote) getString(R.string.checklistLabel) else ""
            binding.vp2.currentItem = if (isSelectNote) 0 else 1
        }
    }

    private fun initViewPager() {
        try {
            val dashBoardAdapter = activity?.let {
                DashBoardPagerAdapter(it, binding.edtSearch.text.toString().lowercase(Locale.getDefault())) {
                    clearFocusEditText()
                }
            }
            binding.vp2.apply {
                adapter = dashBoardAdapter
                isSaveEnabled = false
            }

            binding.vp2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentNote = if (position == 0) TEXT_FM else CHECKLIST_FM
                    initBottomBar()
                    super.onPageSelected(position)
                }
            })
            if (isChangeData) {
//                putListenLoadData()
                isChangeData = false
            }
        } catch (e: Exception) {

        }

    }

    private fun addListColorType() {
        if (!prefUtil.isLoggedIn) {

            viewModelTextNote.addNote(
                NoteModel(
                    title = getString(R.string.welcome),
                    content = getString(R.string.simpleButElegant),
                    dateCreateNote = getCurrentTimeToLong(),
                    isPinned = true,
                    modifiedTime = getCurrentTimeToLong()
                )
            ) {}

            val listTypeNote: ArrayList<ColorItem> = arrayListOf(
                ColorItem(
                    tittle = getString(R.string.allNotesLabel),
                    color = TypeColorNote.DEFAULT.name
                ),
                ColorItem(
                    tittle = getString(R.string.personLabel),
                    color = TypeColorNote.BLUE.name
                ),
                ColorItem(
                    tittle = getString(R.string.workLabel),
                    color = TypeColorNote.A_ORANGE.name
                ),
                ColorItem(
                    tittle = getString(R.string.otherLabel),
                    color = TypeColorNote.B_GREEN.name
                ),
                ColorItem(
                    tittle = "",
                    color = TypeColorNote.F_PRIMARY.name
                ),
                ColorItem(
                    tittle = "",
                    color = TypeColorNote.D_RED.name
                ),
                ColorItem(
                    tittle = "",
                    color = TypeColorNote.BLINK.name
                ),
                ColorItem(
                    tittle = "",
                    color = TypeColorNote.GRAY.name
                )
            )
            _noteTypeViewModel.addType(NoteType(listColor = listTypeNote))
            prefUtil.isLoggedIn = true
        }
    }

    companion object {
        const val TEXT_FM = 0
        const val CHECKLIST_FM = 1

        var isChangeData = false
    }

}