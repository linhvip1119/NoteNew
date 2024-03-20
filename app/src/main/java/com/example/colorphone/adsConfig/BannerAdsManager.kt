package com.example.colorphone.adsConfig

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import com.example.colorphone.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.wecan.inote.util.getAdSize

object BannerAdsManager {
    fun loadAndShowBannerAds(
        activity: Activity,
        placement: String,
        layoutBanner: FrameLayout,
        placementCollapsible: String = ""
    ) {
        val adView = AdView(activity)
        activity.getAdSize().let { adView.setAdSize(it) }
        val bannerRmConfig = AdsConstants.mapRemoteConfigAds[placement]
        bannerRmConfig?.let { bannerConfig ->
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
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                layoutBanner.removeAllViews()
                                layoutBanner.addView(adView)
                            }
                        }
                    } else {
                        adView.adUnitId = activity.getString(R.string.no1_banner_collapsible_default)
                        adView.loadAd(adRequest)
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                layoutBanner.removeAllViews()
                                layoutBanner.addView(adView)
                            }
                        }
                    }
                } else {
                    val adRequest = AdRequest.Builder().build()
                    if (bannerConfig.id.isNotEmpty()) {
                        adView.adUnitId = bannerRmConfig.id
                        adView.loadAd(adRequest)
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                layoutBanner.removeAllViews()
                                layoutBanner.addView(adView)
                            }
                        }
                    } else {
                        adView.adUnitId = activity.getString(R.string.no1_banner_default)
                        adView.loadAd(adRequest)
                        adView.adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                layoutBanner.removeAllViews()
                                layoutBanner.addView(adView)
                            }
                        }
                    }
                }

            }
        }

    }
}