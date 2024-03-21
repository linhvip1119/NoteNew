package com.example.colorphone.adsConfig

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.colorphone.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.skydoves.androidveil.VeilLayout
import com.wecan.inote.util.getAdSize
import com.wecan.inote.util.gone

object BannerAdsManager {
    fun loadAndShowBannerAds(
        activity: Activity,
        placement: String,
        layoutBanner: FrameLayout,
        placementCollapsible: String = "",
        layoutLoading: View? = null
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

        fun showAdsWhenItOn(isOn: Boolean?) {
            isOn?.let { layoutBanner.isVisible = it }

        }
        if (AdsConstants.canRequestAds == null) {
            hideBannerAds()
            return
        }
        if (AdsConstants.canRequestAds == true) {
            val adView = AdView(activity)
            activity.getAdSize().let { adView.setAdSize(it) }
            val bannerRmConfig = AdsConstants.mapRemoteConfigAds[placement]
            showAdsWhenItOn(bannerRmConfig?.isOn)
            val adListener = object : AdListener() {
                override fun onAdLoaded() {
                    bannerRmConfig?.let {
                        stopLoading()
                        if (it.isOn) {
                            layoutBanner.removeAllViews()
                            layoutBanner.addView(adView)
                        } else {
                            hideBannerAds()
                        }
                    }

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
            bannerRmConfig?.let { bannerConfig ->
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
            }
        } else {
            hideBannerAds()
        }
    }
}