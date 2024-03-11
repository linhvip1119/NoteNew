package com.example.colorphone.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.colorphone.databinding.SplashActivityBinding
import com.example.colorphone.ui.MainActivity

class SplashActivity : AppCompatActivity() {

    private var binding: SplashActivityBinding? = null

    private var currentProcess = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        init()
    }

    fun init() {
        binding?.progressBar?.max = 100
        val i = TIME_SHOW / 100
        val j = 100 / i

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentProcess += j
                binding?.progressBar?.progress = currentProcess
                if (currentProcess == 100) {
                    handler.removeCallbacksAndMessages(null)
                    val myIntent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(myIntent)
                    finish()
                } else {
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
    }

    companion object {
        const val TIME_SHOW = 5000
    }
}