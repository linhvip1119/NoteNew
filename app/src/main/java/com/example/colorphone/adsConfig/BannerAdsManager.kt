package com.example.colorphone.adsConfig

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.colorphone.R
import com.example.colorphone.ui.settings.googleDriver.GoogleSignInFragment
import com.example.colorphone.util.Const
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.skydoves.androidveil.VeilLayout
import com.wecan.inote.util.getAdSize
import com.wecan.inote.util.gone
import timber.log.Timber

object BannerAdsManager {
    fun loadAndShowBannerAds(
        activity: Activity,
        placement: String,
        layoutBanner: FrameLayout,
        placementCollapsible: String = "",
        layoutLoading: View? = null,
        fragment: GoogleSignInFragment? = null
    ) {
        fun stopLoading() {
            layoutLoading?.let {
                (it as VeilLayout).apply {
                    it.stopShimmer()
                    it.gone()
                }
            }
        }

        fun hideBannerAds() {
            stopLoading()
            layoutBanner.gone()
        }

        if (AdsConstants.canRequestAds == null) {
            hideBannerAds()
            return
        }
        if (AdsConstants.canRequestAds == true) {
            val adView = AdView(activity)
            activity.getAdSize().let { adView.setAdSize(it) }
            val bannerRmConfig = AdsConstants.mapRemoteConfigAds[placement]

            val adListener = object : AdListener() {
                override fun onAdLoaded() {
                    stopLoading()
                    layoutBanner.removeAllViews()
                    layoutBanner.addView(adView)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdsConstants.isClickAds = true
                }

                override fun onAdClosed() {
                    AdsConstants.isClickAds = false
                    super.onAdClosed()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    hideBannerAds()
                }
            }
            adView.adListener = adListener

            val lifecycleListener = object : FragmentLifecycleListener {

                override fun onFragmentPause(fragment: Fragment) {
//                    adView.pause()
                    Timber.d("3422342234...lifecycleListener pause")
                }

                override fun onFragmentCreated(fragment: Fragment, savedInstanceState: Bundle?) {
                }

                override fun onFragmentResumed(fragment: Fragment) {
                    Timber.d("3422342234...lifecycleListener resumed")
//                    adView.resume()
                }

            }

            fragment?.lifecycleListener = lifecycleListener

            bannerRmConfig?.let { bannerConfig ->
                Const.checking(bannerConfig.spaceName)
                if (bannerConfig.isOn) {
                    if (bannerConfig.isCollapse) {
                        val extras = Bundle()
                        extras.putString("collapsible", placementCollapsible)
                        val adRequest = AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                                .build()
                        if (bannerConfig.id.isNotEmpty()) {
                            adView.adUnitId = bannerConfig.id
                            adView.loadAd(adRequest)
                        } else {
                            adView.adUnitId =
                                activity.getString(R.string.no1_banner_collapsible_default)
                            adView.loadAd(adRequest)
                        }
                    } else {
                        val adRequest = AdRequest.Builder().build()
                        if (bannerConfig.id.isNotEmpty()) {
                            adView.adUnitId = bannerRmConfig.id
                            adView.loadAd(adRequest)
                        } else {
                            adView.adUnitId = activity.getString(R.string.no1_banner_default)
                            adView.loadAd(adRequest)
                        }
                    }
                } else {
                    hideBannerAds()
                }
            }
        } else {
            hideBannerAds()
        }
    }
}