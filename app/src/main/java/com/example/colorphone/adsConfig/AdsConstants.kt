package com.example.colorphone.adsConfig

import com.example.colorphone.model.RemoteConfigAds

object AdsConstants {
    var isShowOpenAds = false
    var isClickAds = false
    var isShowAdsInter = false
    var timeBaseCount = 0L
    var TIME_BASE_DEFAULT = 20000L
    const val POSITION_BOTTOM_BANNER = "bottom"
    const val POSITION_TOP_BANNER = "top"
    val mapRemoteConfigAds = mutableMapOf<String, RemoteConfigAds>()

    var canRequestAds: Boolean? = null

}