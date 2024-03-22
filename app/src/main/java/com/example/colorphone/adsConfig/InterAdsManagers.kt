package com.example.colorphone.adsConfig

import android.app.Activity
import android.util.Log
import com.example.colorphone.R
import com.example.colorphone.util.Const
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.wecan.inote.util.haveNetworkConnection

object InterAdsManagers {
    const val TYPE = "inter"

    private val AD_TAG = "AdsInformation"

    private var mInterstitialAd: InterstitialAd? = null

    private var isInterstitialLoading = false
    private fun isInterstitialLoaded(): Boolean {
        return mInterstitialAd != null
    }

    var idInterAdsUnit = ""

    fun loadInterAds(
        activity: Activity?,
    ) {
        if (AdsConstants.canRequestAds != null) {
            if (AdsConstants.canRequestAds == true) {
                if (!isInterstitialLoaded()) {
                    activity?.let {
                        if (it.haveNetworkConnection()) {
                            if (!isInterstitialLoading) {
                                isInterstitialLoading = true
                                if (idInterAdsUnit.isNotEmpty()) {
                                    load(it,
                                        idInterAdsUnit,
                                        object : InterstitialOnLoadCallBack {})
                                } else {
                                    load(it,
                                        it.getString(R.string.no1_interstitial_default),
                                        object : InterstitialOnLoadCallBack {})
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun load(
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
            }
        )
    }

    fun showAndReloadInterAds(
        activity: Activity?,
        placementAds: String,
        onActiveWhenNotShowAds: () -> Unit,
        mListener: InterstitialOnShowCallBack? = null
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            Const.checking(placementAds)
            val remoteConfigAds = AdsConstants.mapRemoteConfigAds[placementAds]
            if (remoteConfigAds == null || !isInterstitialLoaded() || AdsConstants.isShowOpenAds) {
                onActiveWhenNotShowAds.invoke()
            } else {
                if (currentTime - AdsConstants.timeBaseCount >= AdsConstants.TIME_BASE_DEFAULT && remoteConfigAds.isOn) {
                    activity?.let { mActivity ->
                        mInterstitialAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    Log.d(
                                        AD_TAG,
                                        "admob Interstitial onAdDismissedFullScreenContent"
                                    )
                                    AdsConstants.isShowAdsInter = false
                                    AdsConstants.isClickAds = false
                                    AdsConstants.timeBaseCount = System.currentTimeMillis()
                                    mListener?.onAdDismissedFullScreenContent()
                                    mInterstitialAd = null
                                    loadInterAds(activity)
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    Log.e(
                                        AD_TAG,
                                        "admob Interstitial onAdFailedToShowFullScreenContent"
                                    )
                                    mListener?.onAdFailedToShowFullScreenContent()
                                    AdsConstants.isShowAdsInter = false
                                    mInterstitialAd = null
                                }

                                override fun onAdShowedFullScreenContent() {
                                    Log.d(
                                        AD_TAG,
                                        "admob Interstitial onAdShowedFullScreenContent"
                                    )
                                    onActiveWhenNotShowAds()
                                    mListener?.onAdShowedFullScreenContent()
                                    AdsConstants.isShowAdsInter = true
                                    mInterstitialAd = null
                                }

                                override fun onAdImpression() {
                                    Log.d(AD_TAG, "admob Interstitial onAdImpression")
                                    mListener?.onAdImpression()
                                }

                                override fun onAdClicked() {
                                    super.onAdClicked()
                                    AdsConstants.isClickAds = true
                                    mListener?.onAdClick()
                                }
                            }
                        mInterstitialAd?.show(mActivity)
                    }
                } else {
                    onActiveWhenNotShowAds.invoke()
                }
            }
        } catch (_: Exception) {
            onActiveWhenNotShowAds.invoke()
        }

    }
}