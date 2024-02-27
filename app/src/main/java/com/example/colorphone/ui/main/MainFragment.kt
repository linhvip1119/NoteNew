package com.example.colorphone.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentMainBinding
import com.example.colorphone.databinding.RowBinding
import com.example.colorphone.model.ColorItem
import com.example.colorphone.model.NoteType
import com.example.colorphone.ui.MainActivity
import com.example.colorphone.ui.bottomDialogColor.viewmodel.BottomSheetViewModel
import com.example.colorphone.ui.main.adapter.DashBoardPagerAdapter
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.TYPE_ITEM_EDIT
import com.example.colorphone.util.Const.currentType
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.hideKeyboard
import com.wecan.inote.util.mapIdColor
import com.wecan.inote.util.px
import com.wecan.inote.util.setOnClickAnim
import com.wecan.inote.util.setPreventDoubleClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Locale
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    @Inject
    lateinit var prefUtil: PrefUtil

    private val _noteTypeViewModel: BottomSheetViewModel by viewModels()

     var currentNote = TEXT_FM

    private var isInitialization = false

    private val listCountRate = arrayListOf(1, 7, 21, 45)

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

    override fun init(view: View) {
        addListColorType()
        initBottomBar()
        initViewPager()
        onListener()
        onSearchNote()
    }

    private fun initView() {
        binding.ivAllBox.isSelected = false
        // binding.ivDown.gone()
        showHideSortMenu(false)
        showHideViewMenu(false)
        currentType?.let {
            mapIdColor(it, true) { idColor: Int, _: Int, _: Int, _: Int, _ ->
                binding.ivAllBox.setImageResource(idColor)
            }
        }
    }

    private fun showHideSortMenu(isShow: Boolean) {
        binding.apply {
//            viewSupportMenu.isVisible = isShow
//            iclMenuSort.root.isVisible = isShow
//            if (isShow) {
//                logEvent("Dialog_Sort_Show")
//            }
        }
    }

    private fun showHideViewMenu(isShow: Boolean) {
//        binding.apply {
//            viewSupportMenu.isVisible = isShow
//            iclMenu.root.isVisible = isShow
//        }
//        if (isShow) {
//            logEvent("Dialog_Main_Extend_Show")
//        }
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
//            ivProfile.setOnClickListener {
//                navigate(R.id.settingsScreen)
//            }
//            viewSupportMenu.setOnClickListener {
//                showHideSortMenu(false)
//                showHideViewMenu(false)
//            }
//            iclMenu.root.forEachIndexed { index, view ->
//                view.setOnClickListener {
//                    when (index) {
//                        0 -> {
//                            navToSelectScreen()
//                        }
//
//                        1 -> {
//                            openDialogView()
//                        }
//
//                        2 -> {
//                            handleDialogSoft()
//                        }
//
//                        3 -> {
//                            //   navigationWithAnim()
//                            navController?.navigate(R.id.iapFragment)
//                        }
//                    }
//                    showHideViewMenu(false)
//                }
//            }

            ivSync.setPreventDoubleClick {
//                Log.d("TABNGMMM", prefUtil.statusEmailUser.toString() + "mm")
//                if (prefUtil.statusEmailUser.isNullOrEmpty()) {
//                    startGoogleDriveSignIn()
//                } else {
//                    handleSyncData {
//                        prefUtil.lastSync =
//                            System.currentTimeMillis()
//                        navigationWithAnim(R.id.settingsScreen)
//                        putListenLoadData()
//                    }
//                }
            }

            binding.ivAllBox.setOnClickAnim {
                showBottomSheet(true, currentType, Const.TEXT_SCREEN) {
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

    private var mDropdown: PopupWindow? = null
    private var mInflater: LayoutInflater? = null

    private fun initiatePopupMenu(): PopupWindow? {
        try {
            mInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val menuBinding = RowBinding.inflate(mInflater!!)

            menuBinding.root.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            menuBinding.apply {
                tvSelect.setPreventDoubleClick {
                    navToSelectScreen()
                    mDropdown?.dismiss()
                }
                tvView.setPreventDoubleClick {
                    openDialogView()
                    mDropdown?.dismiss()
                }
                tvSort.setPreventDoubleClick {
                    handleDialogSoft()
                    mDropdown?.dismiss()
                }
            }
            mDropdown = PopupWindow(
                menuBinding.root, FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, true
            )
            mDropdown?.showAsDropDown(binding.ivSync, (-20).px, (-5).px)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return mDropdown
    }

    private fun onSearchNote() {
        binding.edtSearch.doOnTextChanged { char, _, _, _ ->
            val query = char.toString().lowercase(Locale.getDefault())
            shareViewModel.setSearchText(query)
        }
    }

    private fun navigateToCreateNote() {
        val currentType = if (binding.vp2.currentItem == 0) Const.TYPE_NOTE else Const.TYPE_CHECKLIST
        navigationWithAnim(
            R.id.editFragment,
            bundleOf(
//                TextFragment.ARG_CREATE_NOTE to isCreateNote,
//                BaseListFragment.CURRENT_TYPE to type,
                TYPE_ITEM_EDIT to currentType
            )
        )
    }

    override fun onSubscribeObserver(view: View) {
    }

    private fun initBottomBar() {
        binding.apply {
            val isSelectNote = currentNote == TEXT_FM
            flNote.setBackgroundResource(if (isSelectNote) R.drawable.bg_button_note_bar_selected else Color.TRANSPARENT)
            flChecklist.setBackgroundResource(if (!isSelectNote) R.drawable.bg_button_note_bar_selected else Color.TRANSPARENT)
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