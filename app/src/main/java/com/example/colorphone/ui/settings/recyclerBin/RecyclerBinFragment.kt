package com.example.colorphone.ui.settings.recyclerBin

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentRecyclerBinBinding
import com.example.colorphone.model.NoteModel
import com.example.colorphone.room.DataConverter
import com.example.colorphone.ui.main.MainFragment
import com.example.colorphone.ui.settings.googleDriver.helper.GoogleDriveApiDataRepository
import com.example.colorphone.ui.settings.recyclerBin.adapter.RecycleBinAdapter
import com.example.colorphone.util.Const
import com.example.colorphone.util.TypeView
import com.example.colorphone.util.ext.showAlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.Drive
import com.wecan.inote.util.showCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerBinFragment : BaseFragment<FragmentRecyclerBinBinding>(FragmentRecyclerBinBinding::inflate) {

    private val _adapterText by lazy { RecycleBinAdapter(true) }
    private var _listLocal: List<NoteModel>? = null
    private var isArchiveScreen = true
    private var isSelectedAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _adapterText.isDarkTheme = prefUtil.isDarkMode
        isArchiveScreen = arguments?.getBoolean(Const.KEY_SCREEN_RECYCLER_BIN) == true
    }

    private fun checking(key1: String, key2: String) {
        check(if (isArchiveScreen) key1 else key2)
    }

    override fun onGoogleDriveSignedInSuccess(driveApi: Drive?) {
        repository = GoogleDriveApiDataRepository(driveApi)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun init(view: View) {
        initView()
        onListener()
        setUpRecyclerView()
        onBackPressHandle()
        val account = context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account != null) {
            initializeDriveClient(account)
        }
        viewModelTextNote.getListRecycleArchive(isArchiveScreen, prefUtil.sortType)
        checking("Archived_Show", "Bin_Show")
    }

    private fun setUpRecyclerView() {
        val type = prefUtil.typeView
        _adapterText.typeView = when (type) {
            TypeView.List.value -> TypeView.List.type
            TypeView.Details.value -> TypeView.Details.type
            else -> TypeView.Grid.type
        }
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

    private fun changeCheckBox(isCheckedAll: Boolean) {
        isSelectedAll = isCheckedAll
    }

    private fun onBackPressHandle() {
        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            handleReloadSettingFm()
        }
    }

    private fun handleReloadSettingFm() {
        navController?.popBackStack()
        MainFragment.isChangeData = true
    }

    private fun onListener() {
        binding.apply {

            tvBack.setOnClickListener {
                checking("Archived_Back_Click", "Bin_Back_Click")
                handleReloadSettingFm()
            }

            ivUnArchive.setOnClickListener {
                handleUnArchiveNote()
            }

            ivDelete.setOnClickListener {
                handleDeleteNote()
                checking("Archived_Delete_Click", "Bin_Delete_Click")
            }

            ivSelectAll.setOnClickListener {
                changeCheckBox(!isSelectedAll)
                _adapterText.handleListSelectedAll(!isSelectedAll)
                checking("Archived_SelectAll_Click", "Bin_SelectAll_Click")
            }
        }

        _adapterText.onSelectItem = { size ->
            handleSelectItem(size)
            checking("Archived_Item_Click", "Bin_Item_Click")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleUnArchiveNote() {
        if (isArchiveScreen) {
            lifecycleScope.launch {
                _adapterText.getListSelected().forEach {
                    viewModelTextNote.unArchiveNote(it)
                    _adapterText.updateListAfterUnArchive()
                    delay(200)
                    toggleRecyclerView(_adapterText.currentList)
                }
                showToast(getString(R.string.noteUnArchiveLabel))
                check("Archived_Restore_Click")
            }
        } else {
            lifecycleScope.launch {
                _adapterText.getListSelected().forEach {
                    viewModelTextNote.unDeleteLocalNote(it)
                    _adapterText.updateListAfterUnRecycleBin()
                    delay(200)
                    toggleRecyclerView(_adapterText.currentList)
                }
                showToast(getString(R.string.noteUnDeleted))
                check("Bin_Restore_Click")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleDeleteNote() {
        checking("Archived_diaPermanentDel_Show", "Bin_diaPermanentDel_Show")
        checking("Archived_diaPermanentDel_Show", "Bin_diaPermanentDel_Show")
        context?.showAlertDialog(
            getString(R.string.permanentDelete), getString(R.string.areYouSureDelete), getString(R.string.deleteLabel),
            back = {
                checking("Archived_diaPermanentDel_Cancel_Click", "Bin_diaPermanentDel_Cancel_Click")
            }
        ) {
            if (prefUtil.statusEmailUser != null) {
                val dialogProgress = ProgressDialog(context)
                dialogProgress.setTitle(getString(R.string.processing))
                dialogProgress.setMessage(getString(R.string.please_wait))
                dialogProgress.setCancelable(false)
                if (!dialogProgress.isShowing) {
                    dialogProgress.show()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val id = repository?.query()
                        withContext(Dispatchers.IO) {
                            if (id.isNullOrEmpty()) {
                                _adapterText.getListSelected().forEach {
                                    lifecycleScope.launch {
                                        it.ids?.let { it1 ->
                                            viewModelTextNote.delete(it1) {}
                                        }
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    dialogProgress.dismiss()
                                    _adapterText.updateListDelete()
                                    showToast(getString(R.string.noteDeletedLabel).plus("."))
                                }
                            } else {
                                val json = repository?.readFile(id)
                                val list = DataConverter().toListNote(json)
                                _adapterText.getListSelected().forEach { listDelete ->
                                    Log.d("TAVBNNN", "vvv4")
                                    lifecycleScope.launch {
                                        listDelete.ids?.let { it1 ->
                                            viewModelTextNote.delete(it1) {

                                            }
                                        }
                                    }
                                    val model = list?.find { it.token == listDelete.token }
                                    if (model != null) {
                                        Log.d("TAVBNNN", "vvv3")
                                        list.remove(model)
                                    }
                                }

                                val noteData = DataConverter().fromListNote(list?.let { ArrayList(it) })
                                if (id.isNotEmpty()) {
                                    repository?.uploadFile(
                                        id, "iNote", noteData.toString()
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    dialogProgress.dismiss()
                                    _adapterText.updateListDelete()
                                    showToast(getString(R.string.noteDeletedLabel).plus("."))
                                }
                            }
                        }
                    } catch (e: UserRecoverableAuthIOException) {
                        launcher.launch(e.intent)
                    }

                }

            } else {
                _adapterText.getListSelected().forEach {
                    lifecycleScope.launch {
                        it.ids?.let { it1 ->
                            viewModelTextNote.delete(it1) {}
                        }
                    }
                }
                _adapterText.updateListDelete()
                showToast(getString(R.string.noteDeletedLabel).plus("."))
            }
            checking("Archived_diaPermanentDel_Delete_Click", "Bin_diaPermanentDel_Delete_Click")
        }
    }

    private fun handleSelectItem(size: Int) {
        binding.apply {
            ivSelectAll.isVisible = size > 0
            ivSelectAll.setImageResource(if (size == _adapterText.currentList.size) R.drawable.ic_select_all_on else R.drawable.ic_select_all_off)
            llBottomSelected.isVisible = size > 0
            changeCheckBox(size == _adapterText.itemCount)
            isSelectedAll = size == _adapterText.itemCount
            tvBack.text = if (size > 0) size.toString().plus(" ").plus(getText(R.string.selectedLabel)) else {
                if (isArchiveScreen) context?.getString(R.string.archiveLabel) else context?.getString(
                    R.string.recycleBin
                )
            }
        }
    }

    private fun showToast(message: String) {
        binding.apply {
            activity?.let { Toast(context).showCustomToast(it, message) }
        }
    }

    private fun initView() {
        binding.apply {
            tvBack.text = if (isArchiveScreen) context?.getString(R.string.archiveLabel) else context?.getString(
                R.string.recycleBin
            )
            ivUnArchive.text = if (isArchiveScreen) context?.getString(R.string.unArchiveLabel) else context?.getString(
                R.string.restore
            )
            ivUnArchive.isEnabled = true
            ivDelete.isEnabled = true

            tvEmptyData.text = if (isArchiveScreen) getString(R.string.yourArchiveNote) else getString(R.string.yourDeletedNote)
            ivNoData.setImageResource(if (isArchiveScreen) R.drawable.ic_empty_archive else R.drawable.ic_empty_recycler_bin)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onSubscribeObserver(view: View) {
        with(viewModelTextNote) {
            archiveLiveData.observe(viewLifecycleOwner) {
                _listLocal = it
                _adapterText.submitList(_listLocal)
                toggleRecyclerView(_listLocal ?: listOf())
            }
            recycleBinLiveData.observe(viewLifecycleOwner) {
                _listLocal = it
                _adapterText.submitList(_listLocal)
                toggleRecyclerView(_listLocal ?: listOf())
            }
        }
    }

    private fun toggleRecyclerView(noteList: List<NoteModel>) {
        binding.rvListText.isVisible = noteList.isNotEmpty()
        binding.clNoDataRecycler.isVisible = noteList.isEmpty()
    }

}