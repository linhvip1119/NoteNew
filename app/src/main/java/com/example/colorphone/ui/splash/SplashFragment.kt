package com.example.colorphone.ui.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.colorphone.R
import com.example.colorphone.adsConfig.AdsConstants
import com.example.colorphone.adsConfig.InterAdsManagers
import com.example.colorphone.adsConfig.PlacementAds
import com.example.colorphone.base.BaseFragment
import com.example.colorphone.databinding.SplashFragmentBinding
import com.example.colorphone.util.Const
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.wecan.inote.util.haveNetworkConnection
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashFragmentBinding>(SplashFragmentBinding::inflate) {

    var handler: Handler? = null

    private var isTimeOut = false

    private var isShowAds = false

    private var isPause = false

    private var countTime = 0

    private var isAdsClose = false

    private var appOpenAd: AppOpenAd? = null

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    override fun init(view: View) {
        AdsConstants.isShowAdsInter = true
        Const.checking("Splash_Show")
        if (context?.haveNetworkConnection() != true) {
            Const.checking("Splash_NoInternet_Show")
        }
        initUmp()
    }

    private fun initUmp() {
        context?.haveNetworkConnection()?.let {
            if (it) {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    activity?.let { activity ->
                        MobileAds.initialize(activity) {}
                    }
                    requestAds()
                }
                Log.d("TAGVBNHJJSS", googleMobileAdsConsentManager.canRequestAds.toString() + "a")
                activity?.let {
                    googleMobileAdsConsentManager.gatherConsent(it) { consentError ->

                        Log.d(
                            "TAGVBNHJJJJKL",
                            googleMobileAdsConsentManager.canRequestAds.toString()
                        )
                        Log.d(
                            "TAGVBNHJJJJKL",
                            googleMobileAdsConsentManager.isPrivacyOptionsRequired.toString()
                        )
                        if (googleMobileAdsConsentManager.canRequestAds) {
                            Log.d(
                                "TAGVBNHJJJJKL",
                                googleMobileAdsConsentManager.canRequestAds.toString()
                            )
                            activity?.let { MobileAds.initialize(it) {} }
                            requestAds()
                        } else {
                            goToHomeScreen()
                        }

                    }
                }

            } else {
                timeOut(timeOut = 1000)
            }
        }
    }

    private fun timeOut(timeOut: Int) {
        handler = Handler(Looper.getMainLooper())
        handler!!.postDelayed(object : Runnable {
            override fun run() {
                if (countTime >= timeOut && !isShowAds) {
                    isTimeOut = true
                    appOpenAd = null
                    goToHomeScreen()
                } else {
                    countTime += 100
                    handler!!.postDelayed(this, 100)
                }
            }
        }, 0)
    }


    private fun requestAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        timeOut(timeOut = 15000)
        val request = AdRequest.Builder().build()
        activity?.let {
            val remoteConfigAds = AdsConstants.mapRemoteConfigAds[PlacementAds.PLACEMENT_SPLASH]
            if (remoteConfigAds != null) {
                if (remoteConfigAds.isOn) {
                    AppOpenAd.load(
                        it, remoteConfigAds.id, request, object :
                            AppOpenAd.AppOpenAdLoadCallback() {

                            override fun onAdLoaded(p0: AppOpenAd) {
                                super.onAdLoaded(p0)
                                if (!isTimeOut) {
                                    appOpenAd = p0
                                    handler!!.removeCallbacksAndMessages(null)
                                    if (!isPause) {
                                        showAds()
                                    }
                                }
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                super.onAdFailedToLoad(p0)
                                if (!isTimeOut) {
                                    appOpenAd = null
                                    handler!!.removeCallbacksAndMessages(null)
                                    if (!isPause) {
                                        goToHomeScreen()
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                AppOpenAd.load(
                    it,
                    resources.getString(R.string.no1_app_open_default),
                    request, object :
                        AppOpenAd.AppOpenAdLoadCallback() {

                        override fun onAdLoaded(p0: AppOpenAd) {
                            super.onAdLoaded(p0)
                            if (!isTimeOut) {
                                appOpenAd = p0
                                handler!!.removeCallbacksAndMessages(null)
                                if (!isPause) {
                                    showAds()
                                }
                            }
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            if (!isTimeOut) {
                                appOpenAd = null
                                handler!!.removeCallbacksAndMessages(null)
                                if (!isPause) {
                                    goToHomeScreen()
                                }
                            }
                        }
                    }
                )
            }

        }
    }

    private fun showAds() {
        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isAdsClose = true
                    goToHomeScreen()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    handler!!.removeCallbacksAndMessages(null)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowAds = true
                    handler!!.removeCallbacksAndMessages(null)
                }
            }
        appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
        if (!isTimeOut && !isPause) {
            activity?.let { appOpenAd!!.show(it) }
        }
    }


    private fun goToHomeScreen() {
        AdsConstants.isShowAdsInter = false
        try {
            if (navController?.currentDestination?.id == R.id.splashFragment){
                navigationWithAnim(R.id.action_splashFragment_to_mainFragment)
            } else {
                navigationWithAnim(R.id.mainFragment)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun gotoLoginOnResume() {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    goToHomeScreen()
                }

                else -> {
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (isPause) {
            isPause = false
            if (!isShowAds) {
                handler?.postDelayed(object : Runnable {
                    override fun run() {
                        if (appOpenAd != null) {
                            isTimeOut = false
                            showAds()
                        } else {
                            if (countTime >= 12000) {
                                isTimeOut = true
                                gotoLoginOnResume()
                            } else {
                                countTime += 100
                                handler?.postDelayed(this, 100)
                            }
                        }
                    }
                }, 0)
            } else {
                if (isAdsClose) {
                    gotoLoginOnResume()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }


    override fun onStop() {
        handler?.removeCallbacksAndMessages(null)
        super.onStop()
    }

    override fun onSubscribeObserver(view: View) {}
}