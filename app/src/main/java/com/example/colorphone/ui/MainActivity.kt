package com.example.colorphone.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.colorphone.R
import com.example.colorphone.util.PrefUtil
import com.example.colorphone.util.TypeColorNote
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var prefUtil: PrefUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()
        themeIndex = getIndexTheme(prefUtil.themeColor)
        setTheme(themesList[themeIndex])
        setContentView(R.layout.activity_main)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
            R.style.coolBlueNav
        )

        fun getIndexTheme(colorTheme: String): Int {
            return when (colorTheme) {
                TypeColorNote.F_PRIMARY.name -> 0
                TypeColorNote.B_GREEN.name -> 1
                TypeColorNote.A_ORANGE.name -> 2
                TypeColorNote.D_RED.name -> 3
                TypeColorNote.C_BLUE.name -> 4
                else -> 0
            }
        }

    }
}