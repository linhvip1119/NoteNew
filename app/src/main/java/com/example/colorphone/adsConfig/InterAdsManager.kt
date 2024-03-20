package com.example.colorphone.adsConfig

import android.app.Activity
import android.util.Log
import com.example.colorphone.R
import com.example.colorphone.model.RemoteConfigAds
import com.example.colorphone.util.Const
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.wecan.inote.util.haveNetworkConnection

class InterAdsManager(
    private val activity: Activity?,
    private val remoteConfigAds: RemoteConfigAds?,
    private val isAppPurchased: Boolean,
    private val mListener: InterstitialOnLoadCallBack
) {
    private val AD_TAG = "AdsInformation"

    private var mInterstitialAd: InterstitialAd? = null

    private var isInterstitialLoading = false

    init {
        loadInterWhenCreate()
    }

    private fun loadInterWhenCreate() {
        if (!isInterstitialLoaded()) {
            activity?.let {
                if (it.haveNetworkConnection()) {
                    loadInterstitialAd()
                }
            }
        }
    }

    private fun loadInterstitialAd() {
        activity?.let { mActivity ->
            if (!isAppPurchased && !isInterstitialLoading) {
                if (mInterstitialAd == null) {
                    isInterstitialLoading = true
                    remoteConfigAds?.let {
                        if (it.isOn) {
                            if (it.id.isNotEmpty()) {
                                loadAds(mActivity, it.id, mListener)
                            } else {
                                loadAds(
                                    mActivity,
                                    mActivity.getString(R.string.no1_interstitial_default),
                                    mListener
                                )
                            }
                        }
                    }

                } else {
                    Log.d(AD_TAG, "admob Interstitial onPreloaded")
                    mListener.onPreloaded()
                }

            } else {
                Log.e(AD_TAG, " isAppPurchased = $isAppPurchased")
                mListener.onAdFailedToLoad("isAppPurchased = $isAppPurchased")
            }
        }
    }

    private fun loadAds(
        mActivity: Activity,
        idAds: String,
        mListener: InterstitialOnLoadCallBack
    ) {
        InterstitialAd.load(
            mActivity,
            idAds,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(AD_TAG, "admob Interstitial onAdFailedToLoad")
                    isInterstitialLoading = false
                    mInterstitialAd = null
                    mListener.onAdFailedToLoad(adError.toString())
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(AD_TAG, "admob Interstitial onAdLoaded")
                    isInterstitialLoading = false
                    mInterstitialAd = interstitialAd
                    mListener.onAdLoaded()

                }
            })
    }

    fun showInterstitialAd(
        activity: Activity?,
        onActiveWhenNotShowAds: () -> Unit,
        mListener: InterstitialOnShowCallBack
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            if (remoteConfigAds == null || !isInterstitialLoaded() || AdsConstants.isShowOpenAds) {
                onActiveWhenNotShowAds.invoke()
            } else {
                Const.checking(remoteConfigAds.spaceName)
                if (currentTime - AdsConstants.timeBaseCount >= AdsConstants.TIME_BASE_DEFAULT && remoteConfigAds.isOn) {
                    activity?.let { mActivity ->
                        if (mInterstitialAd != null) {
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        Log.d(
                                            AD_TAG,
                                            "admob Interstitial onAdDismissedFullScreenContent"
                                        )
                                        AdsConstants.isShowAdsInter = false
                                        AdsConstants.timeBaseCount = System.currentTimeMillis()
                                        mListener.onAdDismissedFullScreenContent()
                                        mInterstitialAd = null
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        Log.e(
                                            AD_TAG,
                                            "admob Interstitial onAdFailedToShowFullScreenContent"
                                        )
                                        mListener.onAdFailedToShowFullScreenContent()
                                        AdsConstants.isShowAdsInter = false
                                        mInterstitialAd = null
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        Log.d(
                                            AD_TAG,
                                            "admob Interstitial onAdShowedFullScreenContent"
                                        )
                                        mListener.onAdShowedFullScreenContent()
                                        AdsConstants.isShowAdsInter = true
                                        mInterstitialAd = null
                                    }

                                    override fun onAdImpression() {
                                        Log.d(AD_TAG, "admob Interstitial onAdImpression")
                                        mListener.onAdImpression()
                                    }

                                    override fun onAdClicked() {
                                        super.onAdClicked()
                                        AdsConstants.isClickAds = true
                                        mListener.onAdClick()
                                    }
                                }
                            mInterstitialAd?.show(mActivity)
                        }
                    }
                } else {
                    onActiveWhenNotShowAds.invoke()
                }
            }
        } catch (_: Exception) {
            onActiveWhenNotShowAds.invoke()
        }

    }

    private fun isInterstitialLoaded(): Boolean {
        return mInterstitialAd != null
    }

}