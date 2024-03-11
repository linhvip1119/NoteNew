package com.example.colorphone.ui.splash

import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.SplashFragmentBinding

class SplashFragment : BaseFragment<SplashFragmentBinding>(SplashFragmentBinding::inflate) {

    private var currentProcess = 0
    override fun init(view: View) {
        init()
    }

    override fun onSubscribeObserver(view: View) {

    }

    fun init() {
        binding.progressBar?.max = 100
        val i = TIME_SHOW / 100
        val j = 100 / i

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentProcess += j
                binding.progressBar?.progress = currentProcess
                if (currentProcess == 100) {
                    handler.removeCallbacksAndMessages(null)
                    navigationWithAnim(R.id.mainFragment)
                } else {
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
    }

    companion object {
        const val TIME_SHOW = 500
    }
}