package com.example.colorphone.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentSettingBinding
import com.example.colorphone.ui.settings.googleDriver.helper.GoogleDriveApiDataRepository
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.EMAIL_FEEDBACK
import com.example.colorphone.util.Const.KEY_SCREEN_RECYCLER_BIN
import com.example.colorphone.util.ext.loadUrl
import com.example.colorphone.util.ext.showAlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.api.services.drive.Drive
import com.wecan.inote.util.gone
import com.wecan.inote.util.px
import com.wecan.inote.util.sendEmailMore
import com.wecan.inote.util.setDrawableLeft
import com.wecan.inote.util.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    @Inject
    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpGoogle()
    }

    override fun init(view: View) {
        initView()
        onListener()
        onListenWidget()
        getData()
        Const.checking("Setting_Show")
    }

    private fun onListenWidget() {
//        if (isConfigWidget == true) {
//            navController?.navigate(R.id.selectNoteToWidget)
//            isConfigWidget = false
//        }
    }

    private fun initView() {
        binding.apply {
            ivArchive.isEnabled = true
            ivRecycleBin.isEnabled = true
            context?.let {
                llSetting.setBackgroundColor(
                    ContextCompat.getColor(
                        it,
                        R.color.neutral0
                    )
                )
            }
        }

        val account = context?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account == null) {
            initViewNoAccount()
        } else {
            initializeDriveClient(account)
        }

        if (prefUtil.lastSync != 0L) {
            try {
                val simpleDateFormat = SimpleDateFormat("dd/MM/yyy HH:mm:ss")
                binding.tvLastSync.text = simpleDateFormat.format(prefUtil.lastSync)
            } catch (e: Exception) {

            }
        }
    }

    private fun initViewNoAccount() {
        binding.apply {
            ivAvatarUser.loadUrl(R.drawable.icons_google_svg, 120.px)
            tvNameAccount.text = getString(R.string.accountLabel)
            tvEmailAccount.text = getString(R.string.signInAndSync)
            tvLastSync.gone()
            tvButtonSync.apply {
                setBackgroundResource(R.drawable.bg_button_login)
                text = getString(R.string.loginLabel)
                setDrawableLeft(0)
                setPadding(
                    resources.getDimensionPixelOffset(R.dimen.dp45),
                    resources.getDimensionPixelOffset(R.dimen.dp9),
                    resources.getDimensionPixelOffset(R.dimen.dp45),
                    resources.getDimensionPixelOffset(R.dimen.dp9)
                )
            }
            tvLogOut.gone()
        }
    }

    private fun signOut() {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            initViewNoAccount()
        }
    }

    private fun initViewInfoAccount() {
        val user = prefUtil.statusEmailUser
        binding.apply {
            try {
                user?.avatar?.let { ivAvatarUser.loadUrl(it, 120.px) }
            } catch (e: Exception) {
                ivAvatarUser.loadUrl(R.drawable.icons_google_svg, 120.px)
            }
            tvNameAccount.text = user?.name
            tvEmailAccount.text = user?.email
            tvButtonSync.apply {
                setBackgroundResource(R.drawable.bg_button_sync)
                text = getString(R.string.sync)
                setPadding(
                    resources.getDimensionPixelOffset(R.dimen.dp28),
                    resources.getDimensionPixelOffset(R.dimen.dp9),
                    resources.getDimensionPixelOffset(R.dimen.dp34),
                    resources.getDimensionPixelOffset(R.dimen.dp9)
                )
                setDrawableLeft(R.drawable.ic_sync_setting)
            }
            tvLastSync.isVisible = if (prefUtil.lastSync != 0L) {
                val simpleDateFormat = SimpleDateFormat("dd/MM/yyy HH:mm:ss")
                tvLastSync.text = simpleDateFormat.format(prefUtil.lastSync)
                true
            } else {
                false
            }
            tvLogOut.show()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onListener() {
        binding.apply {
            tvSetting.setOnClickListener {
                Const.checking("Setting_Back_Click")
                navController?.popBackStack()
            }

            clArchive.setOnClickListener {
                Const.checking("Setting_Archive_Click")
                goToListRecycleBin(true)
            }

            clRecyclerBin.setOnClickListener {
                Const.checking("Setting_Trash_Click")
                goToListRecycleBin(false)
            }

            clSettings.setOnClickListener {
                Const.checking("Setting_Advanced_Click")
                navigationWithAnim(R.id.action_settingFragment_to_advancedFragment)
            }

            clWidgets.setOnClickListener {
                Const.checking("Setting_Widgets_Click")
                navigationWithAnim(R.id.action_settingFragment_to_widgetFragment)
            }

            clFeedback.setOnClickListener {
                Const.checking("Setting_Feedback_Click")
                context?.sendEmailMore(arrayOf(EMAIL_FEEDBACK))
            }

            tvButtonSync.setOnClickListener {
                if (prefUtil.statusEmailUser == null) {
                    Const.checking("Setting_LogIn_Click")
                    startGoogleDriveSignIn()
                } else {
                    Const.checking("Setting_Sync_Click")
                    handleSyncData() {
                        viewModelTextNote.getListRecycleArchive(false, prefUtil.sortType)
                        viewModelTextNote.getListRecycleArchive(true, prefUtil.sortType)
                        prefUtil.lastSync =
                            System.currentTimeMillis()
                        binding.tvLastSync.show()
                        val simpleDateFormat = SimpleDateFormat("dd/MM/yyy HH:mm:ss")
                        binding.tvLastSync.text = simpleDateFormat.format(prefUtil.lastSync)
                    }
                }
            }

            tvLogOut.setOnClickListener {
                Const.checking("Setting_LogOut_Click")
                Const.checking("Setting_diaLogOut_Show")
                context?.showAlertDialog(
                    getString(R.string.logout),
                    getString(R.string.areYouSureLogout),
                    getString(R.string.logout),
                    back = {
                        Const.checking("Setting_diaLogOut_Cancel")
                    }
                ) {
                    Const.checking("Setting_diaLogOut_Click")
                    signOut()
                    prefUtil.statusEmailUser = null
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(this, true) {
            navController?.popBackStack()
        }
    }

    private fun goToListRecycleBin(isToArchiveFm: Boolean) {
        navigationWithAnim(R.id.action_settingFragment_to_recyclerBinFragment, bundleOf(KEY_SCREEN_RECYCLER_BIN to isToArchiveFm))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getData() {
        viewModelTextNote.getListRecycleArchive(false, prefUtil.sortType)
        viewModelTextNote.getListRecycleArchive(true, prefUtil.sortType)
    }

    override fun onGoogleDriveSignedInSuccess(driveApi: Drive?) {
        repository = GoogleDriveApiDataRepository(driveApi)
        initViewInfoAccount()
    }

    override fun onGoogleDriveSignedInFailed(exception: ApiException?) {
        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onSubscribeObserver(view: View) {
        with(viewModelTextNote) {
            archiveLiveData.observe(viewLifecycleOwner) {
                Log.d("TAGNMMM", "a")
                binding.tvSizeArchive.text = it.size.toString()
            }
            recycleBinLiveData.observe(viewLifecycleOwner) {
                Log.d("TAGNMMM", "b")
                binding.tvSizeRecycleBin.text = it.size.toString()
            }
        }
    }

}