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
import com.example.colorphone.util.PrefUtils
import com.example.colorphone.util.ext.getTextLanguage
import com.example.colorphone.util.ext.showAlertDialogTip
import com.wecan.inote.util.mapIdColor
import kotlinx.coroutines.launch

class AdvancedSetting : BaseFragment<FragmentSettingDetailBinding>(FragmentSettingDetailBinding::inflate) {
    override fun init(view: View) {
        initView()
        onListener()
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
                changeDarkMode(isChecked)
            }

            tvSettings.setOnClickListener {
                navController?.popBackStack()
            }

            tvPrivacyPolicy.setOnClickListener {
                openPolicy()
            }

            tvShare.setOnClickListener {
                shareApp()
            }

            switchNotifiBar.setOnCheckedChangeListener { _, isChecked ->
                changeNotificationBar(isChecked)
            }

            ivColor.setOnClickListener {
                showBottomSheet(false, fromScreen = Const.SETTING_SCREEN) {
                    saveTheme(it)
                }
            }

            tvValueLanguage.setOnClickListener {
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
            context?.showAlertDialogTip(context?.getString(R.string.reminderNotBeInTime)
                                                .toString(), onBack = {
                binding.switchNotifiBar.isChecked = true
            }, onContinue = {
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
                finish()
                intent?.let { startActivity(it) }
            }
        }
    }

    override fun onSubscribeObserver(view: View) {
    }

}