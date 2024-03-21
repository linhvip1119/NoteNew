package com.example.colorphone.ui.settings.advanced

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.FragmentSettingDetailBinding
import com.example.colorphone.ui.settings.advanced.bottomLanguage.BottomFragmentLanguage
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.APPLICATION_ID
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.PrefUtils
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.ext.getTextLanguage
import com.example.colorphone.util.ext.showAlertDialogTip
import com.wecan.inote.util.mapIdColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdvancedSetting : BaseFragment<FragmentSettingDetailBinding>(FragmentSettingDetailBinding::inflate) {
    override fun init(view: View) {
        initView()
        onListener()
        check("Advanced_Show")
    }

    private fun initView() {
        val isNightMode = prefUtil.isDarkMode
        val defaultColor = prefUtil.themeColor
        val isNotificationBar = prefUtil.statusNotificationBar
        binding.apply {
            switchDarkMode.isChecked = isNightMode
            ivColor.isSelected = false
            tvValueLanguage.text = context?.getTextLanguage(PrefUtils.languageApp(requireContext()) ?: getString(R.string.english))
            mapIdColor(nameColor = defaultColor, isGetIcon = true) { icon, _, _, _, _ ->
                ivColor.setImageResource(icon)
            }
            switchNotifiBar.isChecked = isNotificationBar
        }
    }

    private fun onListener() {
        binding.apply {

            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                check("Advanced_DarkModeSwitch_Click")
                changeDarkMode(isChecked)
            }

            tvSettings.setOnClickListener {
                check("Advanced_Back_Click")
                navController?.popBackStack()
            }

            tvPrivacyPolicy.setOnClickListener {
                check("Advanced_PrivacyPolicy_Click")
                openPolicy()
            }

            tvShare.setOnClickListener {
                check("Advanced_ShareApp_Click")
                shareApp()
            }

            switchNotifiBar.setOnCheckedChangeListener { _, isChecked ->
                check("Advanced_NotificationBar_Click")
                changeNotificationBar(isChecked)
            }

            ivColor.setOnClickListener {
                check("Advanced_ThemeColor_Click")
                check("Advanced_ThemeColor_Show")
                showBottomSheet(fromScreen = Const.SETTING_SCREEN) {
                    saveTheme(it)
                    check(
                        when (it) {
                            TypeColorNote.A_ORANGE.name -> "Advanced_ThemeColor_Orange_Click"
                            TypeColorNote.B_GREEN.name -> "Advanced_ThemeColor_Green_Click"
                            TypeColorNote.BLUE.name -> "Advanced_ThemeColor_Blue_Click"
                            TypeColorNote.F_PRIMARY.name -> "Advanced_ThemeColor_Purple_Click"
                            TypeColorNote.BLINK.name -> "Advanced_ThemeColor_Pink_Click"
                            TypeColorNote.GRAY.name -> "Advanced_ThemeColor_Gray_Click"
                            TypeColorNote.D_RED.name -> "Advanced_ThemeColor_Red_Click"
                            else -> ""
                        }
                    )
                }
            }

            tvValueLanguage.setOnClickListener {
                check("Advanced_Language_Click")
                showBottomLanguage()
            }

        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "VPN Master")
            var shareMessage = """Simple but Elegant & Super Useful Note App. Try this out 👉 """
            shareMessage =
                (shareMessage + "https://play.google.com/store/apps/details?id=" + APPLICATION_ID).trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTheme(themeColor: String) {
        activity?.apply {
            prefUtil.themeColor = themeColor
            //for restarting app
            finish()
            intent?.let { startActivity(it) }
        }

    }

    private fun showBottomLanguage() {
        val addPhotoBottomDialogFragment: BottomFragmentLanguage =
            BottomFragmentLanguage.newInstance()
        activity?.supportFragmentManager?.let {
            addPhotoBottomDialogFragment.show(
                it, addPhotoBottomDialogFragment.tag
            )
        }
    }

    private fun changeNotificationBar(isChecked: Boolean) {
        if (!isChecked) {
            check("Advanced_TipsReminder_Show")
            context?.showAlertDialogTip(context?.getString(R.string.reminderNotBeInTime).toString(),
              onBack = {
                  check("Advanced_TipsReminder_Cancel_Click")
                binding.switchNotifiBar.isChecked = true
            }, onContinue = {
                check("Advanced_TipsReminder_Continue_Click")
                prefUtil.statusNotificationBar = false
            })
        } else {
            prefUtil.statusNotificationBar = true
        }
    }

    private fun openPolicy() {
        try {
            activity?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://keego.dev/policy/")
                )
            )
        } catch (_: Exception) {
        }
    }

    private fun changeDarkMode(isChecked: Boolean) {
        lifecycleScope.launch {
            prefUtil.isDarkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            prefUtil.putValueMode(isChecked)

            activity?.apply {
                prefUtil.isShowOpenAdsWhenChangeMode = false
                finish()
                intent?.let { startActivity(it) }
            }
        }
    }

    override fun onSubscribeObserver(view: View) {
    }

}