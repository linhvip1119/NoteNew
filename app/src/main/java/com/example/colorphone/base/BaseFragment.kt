package com.example.colorphone.base

import android.app.PendingIntent
import android.app.ProgressDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.colorphone.R
import com.example.colorphone.model.NoteModel
import com.example.colorphone.room.DataConverter
import com.example.colorphone.ui.bottomDialogColor.ui.NoteBottomSheetDialog
import com.example.colorphone.ui.main.viewmodel.ListShareViewModel
import com.example.colorphone.ui.main.viewmodel.TextNoteViewModel
import com.example.colorphone.ui.settings.googleDriver.GoogleSignInFragment
import com.example.colorphone.ui.settings.googleDriver.helper.GoogleDriveApiDataRepository
import com.example.colorphone.ui.settings.widget.provider.NoteProvider
import com.example.colorphone.util.Const
import com.example.colorphone.util.TypeItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseFragment<B : ViewBinding>(val inflate: Inflate<B>) : GoogleSignInFragment() {

    var navController: NavController? = null
    private lateinit var _binding: B
    val binding get() = _binding

    val viewModelTextNote: TextNoteViewModel by viewModels()
    val shareViewModel: ListShareViewModel by activityViewModels()
    private val navBuilder = NavOptions.Builder()

    var repository: GoogleDriveApiDataRepository? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        onSubscribeObserver(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
//        if (navController?.currentDestination?.id == R.id.themeFragment) {
//            if (viewPreview == null) {
//                viewPreview = binding.root
//                return viewPreview
//            } else {
//                return viewPreview
//            }
//        }
        navBuilder.setEnterAnim(android.R.anim.fade_in).setExitAnim(android.R.anim.fade_out)
                .setPopEnterAnim(android.R.anim.fade_in)
                .setPopExitAnim(android.R.anim.fade_out)
        return binding.root
    }

    fun navigationWithAnim(des: Int, bundle: Bundle? = null) {
        navController?.navigate(des, bundle, navBuilder.build())
    }

    fun putFragmentListener(key: String, bundle: Bundle = bundleOf()) {
        activity?.supportFragmentManager?.setFragmentResult(key, bundle)
    }

    fun getFragmentListener(key: String, callback: (Bundle) -> Unit) {
        activity?.supportFragmentManager?.setFragmentResultListener(key, viewLifecycleOwner) { _, result ->
            callback(result)
        }
    }

    abstract fun init(view: View)
    abstract fun onSubscribeObserver(view: View)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    fun isConnectedViaWifi(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return mWifi!!.isConnected || mMobile!!.isConnected
    }

    open fun setUpGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
                .requestScopes(Scope(Scopes.DRIVE_FILE)).requestEmail().build()

        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }
    }

    fun showBottomSheet(
        showDefaultColor: Boolean = true,
        currentColor: String? = null,
        fromScreen: String,
        colorClick: (String) -> Unit
    ) {
        val addPhotoBottomDialogFragment: NoteBottomSheetDialog =
            NoteBottomSheetDialog.newInstance(showDefaultColor, currentColor, fromScreen, colorClick)
        activity?.supportFragmentManager?.let {
            addPhotoBottomDialogFragment.show(
                it, "TAG"
            )
        }
    }

    fun addWidget(idNote: Int, callSuccess: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.let { ct ->
                val appWidgetManager = ct.getSystemService(
                    AppWidgetManager::class.java
                )
                val myProvider = ComponentName(ct, NoteProvider::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    val intent = Intent(activity, NoteProvider::class.java)
                    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = appWidgetManager.getAppWidgetIds(myProvider)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    intent.putExtra(Const.KEY_ID_NOTE_ADD_WIDGET, idNote)
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                    callSuccess.invoke(true)
                }
            }
        } else {
            callSuccess.invoke(false)
        }
    }

    fun handleSyncData(onComplete: () -> Unit) {
        val dialogProgress = ProgressDialog(context)
        dialogProgress.setTitle(getString(R.string.processing))
        dialogProgress.setMessage(getString(R.string.please_wait))
        dialogProgress.setCancelable(false)
        if (!dialogProgress.isShowing) {
            dialogProgress.show()
        }
        var noteData: String? = ""
        var idNote = 0
        var countUpdate = 0
        var countThread = 0
        viewModelTextNote.getAllData() {
            val listNoteLocal = mutableListOf<NoteModel>()
            listNoteLocal.addAll(it)
            noteData = DataConverter().fromListNote(ArrayList(listNoteLocal))
            try {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val id = repository?.query()
                        if (id.isNullOrEmpty()) {
                            try {
                                val idFile = repository?.createFile(
                                    "text/plain",
                                    ""
                                )?.id.toString()
                                withContext(Dispatchers.IO) {
                                    repository?.uploadFile(idFile, "iNote", noteData.toString())
                                    withContext(Dispatchers.Main) {
                                        dialogProgress.dismiss()
                                        showMessage(getString(R.string.synced_successfully))
                                    }
                                }
                            } catch (e: Exception) {

                            }
                        } else {
                            withContext(Dispatchers.IO) {
                                val json = repository?.readFile(id)
                                val list = DataConverter().toListNote(json)
                                var count = 0

                                if ((list ?: listOf()).isNotEmpty()) {
                                    list?.get(list.size - 1)?.ids?.let { idNote = it }
                                }
                                if (list != null) {
                                    Log.d("TGBMMMM", listNoteLocal.toString())
                                    for (i in listNoteLocal.size - 1 downTo 0) {
                                        val model = list.find { it.token == listNoteLocal[i].token }
                                        if (model == null) {
                                            if (listNoteLocal[i].isUpdate != "0") {
                                                Log.d("TGBMMMM", "a")
                                                listNoteLocal[i].ids?.let { viewModelTextNote.delete(it) {} }
                                                listNoteLocal.removeAt(i)
                                            }
                                        }
                                    }

                                    for (item in list) {
                                        val model = listNoteLocal.find { it.token == item.token }
                                        if (model == null) {
                                            idNote++
                                            count++
                                            item.ids = idNote
                                            countUpdate++
                                            viewModelTextNote.addNote(item) {
                                                countThread++
                                            }
                                        } else {
                                            if (model.typeItem == TypeItem.TEXT.name) {
                                                if (model.isUpdate != "0") { // khong co su thay doi o local
                                                    model.content = item.content
                                                    model.typeColor = item.typeColor
                                                    model.typeItem = item.typeItem
                                                    model.listCheckList = item.listCheckList
                                                    model.isPinned = item.isPinned
                                                    model.title = item.title
                                                    model.datePinned = item.datePinned
                                                    model.modifiedTime = item.modifiedTime
                                                    model.isArchive = item.isArchive
                                                    model.isDelete = item.isDelete
                                                    model.datePinned = item.datePinned
                                                    model.isLock = item.isLock
                                                    countUpdate++

                                                    viewModelTextNote.updateNote(model) {
                                                        countThread++
                                                    }
                                                }
                                            } else {
                                                if (model.isUpdate != "0") { // khong co su thay doi o local
                                                    model.content = item.content
                                                    model.typeColor = item.typeColor
                                                    model.typeItem = item.typeItem
                                                    model.listCheckList = item.listCheckList
                                                    model.isPinned = item.isPinned
                                                    model.title = item.title
                                                    model.datePinned = item.datePinned
                                                    model.modifiedTime = item.modifiedTime
                                                    model.isArchive = item.isArchive
                                                    model.isDelete = item.isDelete
                                                    model.datePinned = item.datePinned
                                                    model.isLock = item.isLock
                                                    countUpdate++
                                                    viewModelTextNote.updateNote(model) {
                                                        countThread++
                                                    }
                                                } else {
                                                    item.listCheckList?.let {
                                                        for (itemCheck in it) {
                                                            val modelCheck =
                                                                model.listCheckList?.find { it.token == itemCheck.token }
                                                            if (modelCheck == null) {
                                                                model.listCheckList?.add(itemCheck)
                                                            } else {
                                                                if (!modelCheck.isUpdate) {
                                                                    modelCheck.body = itemCheck.body
                                                                    modelCheck.checked =
                                                                        itemCheck.checked
                                                                }
                                                            }
                                                            countUpdate++
                                                            viewModelTextNote.updateNote(model) {
                                                                countThread++
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        val handler = Handler(Looper.getMainLooper())
                                        handler.postDelayed(object : Runnable {
                                            override fun run() {
                                                if (countUpdate == countThread) {
                                                    handler.removeCallbacksAndMessages(null)
                                                    // dialogProgress.dismiss()

                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        try {
                                                            viewModelTextNote.getAllData() { listNoteAdded ->
                                                                noteData = DataConverter().fromListNote(
                                                                    ArrayList(listNoteAdded)
                                                                )
                                                                lifecycleScope.launch(Dispatchers.IO) {
                                                                    try {
                                                                        repository?.uploadFile(
                                                                            id,
                                                                            "iNote",
                                                                            noteData.toString()
                                                                        )
                                                                        withContext(Dispatchers.Main) {
                                                                            if (count > 0) {
                                                                                showMessage(
                                                                                    "$count " + getString(
                                                                                        R.string.notes_synced_successfully
                                                                                    )
                                                                                )
                                                                            } else {
                                                                                showMessage(getString(R.string.synced_successfully))
                                                                            }
                                                                            dialogProgress.dismiss()
                                                                            onComplete()
                                                                            for (item in listNoteAdded) {
                                                                                item.isUpdate = "-1"
                                                                                item.listCheckList?.let {
                                                                                    for (checkList in it) {
                                                                                        checkList.isUpdate =
                                                                                            false
                                                                                    }
                                                                                }
                                                                                viewModelTextNote.updateNote(
                                                                                    item
                                                                                ) {}
                                                                            }

                                                                        }
                                                                    } catch (e: Exception) {

                                                                    }
                                                                }
                                                            }
                                                        } catch (e: Exception) {

                                                        }
                                                    }
                                                } else {
                                                    handler.postDelayed(this, 1)
                                                }
                                            }
                                        }, 0)

                                    }

                                }

                            }

                        }
                    } catch (e: UserRecoverableAuthIOException) {
                        launcher.launch(e.intent)
                        //startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    }
                }

            } catch (e: IOException) {
                Log.i("TAG", "handleSyncData: $e")
            }
        }

    }
}