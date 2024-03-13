package com.example.colorphone.ui.splash

import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.colorphone.R
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.SplashFragmentBinding
import com.example.colorphone.util.Const
import com.wecan.inote.util.haveNetworkConnection
import dev.keego.haki.Haki
import dev.keego.haki.appopen

class SplashFragment : BaseFragment<SplashFragmentBinding>(SplashFragmentBinding::inflate) {

    private var currentProcess = 0

    private var isFullProgress = false
    override fun init(view: View) {
        Const.checking("Splash_Show")
        init()
        if(context?.haveNetworkConnection() != true){
            Const.checking("Splash_NoInternet_Show")
        }
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
//                    if (!isFullProgress) {
//                        isFullProgress = true
//                        navigationWithAnim(R.id.action_splashFragment_to_mainFragment)
//                    }
                } else {
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
        Haki.placement("scSplash_OpenApp").appopen().forceShow(requireActivity(), dialog = null) {
//            if (isFullProgress) {
            Const.checking("Splash_Navigate")
            navigationWithAnim(R.id.action_splashFragment_to_mainFragment)
//            }
        }
    }

    companion object {
        const val TIME_SHOW = 5000
    }
}