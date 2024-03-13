package com.example.colorphone.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.colorphone.R
import com.example.colorphone.databinding.ActivityMainBinding
import com.example.colorphone.util.Const
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.PrefUtils
import com.example.colorphone.util.TypeColorNote
import com.example.colorphone.util.custom.ContextUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.haki.ads.base.HakiNavHost
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), HakiNavHost {
    @Inject
    lateinit var prefUtil: PrefUtil

    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()
        themeIndex = getIndexTheme(prefUtil.themeColor)
        setTheme(themesList[themeIndex])
        setContentView(binding?.root)
        Const.notificationOn = prefUtil.statusNotificationBar
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeUpdatedContext: ContextWrapper? =
            newBase?.let {
                ContextUtils.updateLocale(it, Locale(PrefUtils.languageApp(it) ?: Locale.getDefault().language))
            }
        super.attachBaseContext(localeUpdatedContext)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var fa: Activity? = null
        var themeIndex = 0
        val themesList = arrayOf(
            R.style.coolPrimaryNav,
            R.style.coolGreenNav,
            R.style.coolOrangeNav,
            R.style.coolRedNav,
            R.style.coolBlueNav,
            R.style.coolBlinkNav,
            R.style.coolGrayNav
        )

        fun getIndexTheme(colorTheme: String): Int {
            return when (colorTheme) {
                TypeColorNote.F_PRIMARY.name -> 0
                TypeColorNote.B_GREEN.name -> 1
                TypeColorNote.A_ORANGE.name -> 2
                TypeColorNote.D_RED.name -> 3
                TypeColorNote.BLUE.name -> 4
                TypeColorNote.BLINK.name -> 5
                TypeColorNote.GRAY.name -> 6
                else -> 4
            }
        }
    }

    override fun navController(): NavController {
        return findNavController(R.id.fragmentContainer)
    }
}