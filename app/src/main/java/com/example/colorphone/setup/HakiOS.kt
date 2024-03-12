package com.example.colorphone.setup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.example.colorphone.BuildConfig
import com.example.colorphone.R
import dev.keego.haki.Haki
import dev.keego.haki.ads.HakiAppOpen
import dev.keego.haki.ads.HakiBanner
import dev.keego.haki.ads.HakiBannerCollapsible
import dev.keego.haki.ads.HakiBannerNative
import dev.keego.haki.ads.HakiInterstitial
import dev.keego.haki.ads.HakiNative
import dev.keego.haki.ads.HakiRewarded
import dev.keego.haki.ads.HakiRewardedInterstitial
import dev.keego.haki.ads.adapter.DefaultNative
import dev.keego.haki.ads.adapter.admob.AdmobAppOpen
import dev.keego.haki.ads.adapter.admob.AdmobBanner
import dev.keego.haki.ads.adapter.admob.AdmobBannerCollapsible
import dev.keego.haki.ads.adapter.admob.AdmobBannerNative
import dev.keego.haki.ads.adapter.admob.AdmobInterstitial
import dev.keego.haki.ads.adapter.admob.AdmobNative
import dev.keego.haki.ads.adapter.admob.AdmobRewarded
import dev.keego.haki.ads.adapter.admob.AdmobRewardedInterstitial
import dev.keego.haki.ads.adapter.applovin.ApplovinAppOpen
import dev.keego.haki.ads.adapter.applovin.ApplovinBanner
import dev.keego.haki.ads.adapter.applovin.ApplovinBannerCollapsible
import dev.keego.haki.ads.adapter.applovin.ApplovinInterstitial
import dev.keego.haki.ads.adapter.applovin.ApplovinNative
import dev.keego.haki.ads.adapter.applovin.ApplovinRewarded
import dev.keego.haki.ads.adapter.empty.EmptyBanner
import dev.keego.haki.ads.adapter.empty.EmptyBannerCollapsible
import dev.keego.haki.ads.adapter.empty.EmptyBannerNative
import dev.keego.haki.ads.adapter.empty.EmptyInterstitial
import dev.keego.haki.ads.adapter.empty.EmptyNative
import dev.keego.haki.ads.adapter.empty.EmptyRewardedInterstitial
import dev.keego.haki.ads.adapter.gam.GamBanner
import dev.keego.haki.ads.adapter.gam.GamInterstitial
import dev.keego.haki.ads.adapter.offline.OfflineAds
import dev.keego.haki.ads.adapter.offline.OfflineBanner
import dev.keego.haki.ads.adapter.offline.OfflineInterstitial
import dev.keego.haki.ads.adapter.offline.OfflineReward
import dev.keego.haki.ads.adapter.offline.OfflineRewardInterstitial
import dev.keego.haki.plugin.TimberPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.seconds


class HakiOS : Initializer<Unit> {
    override fun create(context: Context) {

        Haki.config(
            context as Application,
            version = BuildConfig.VERSION_NAME,
            debug = BuildConfig.DEBUG,
            enable = MutableStateFlow(true)
        )
                .plugin {
                    register(TimberPlugin) // for logging, must have
                    /**
                     * 2 plugin ở dưới thì không cần thiết, chỉ cần khi có yêu cầu
                     */
//                    register(BillingV6Plugin)
//                    register(
//                        AdjustPlugin(
//                            "your_adjust_token",
//                            if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX
//                            else AdjustConfig.ENVIRONMENT_PRODUCTION
//                        )
//                    )
                }
                .ads {

                    /**
                     * [Không cần thiết] Config App Open
                     */
                    appOpenManager.apply {
                        // Bỏ qua các activity không muốn hiển thị quảng cáo App Open khi quay lại app
                        ignore(R.id.splashFragment)
//                        ignore(R.id.editFragment)
                    }
                    /**
                     * [Không cần thiết] Config Consent
                     */
                    consent.apply {
                        // Bỏ qua các activity không muốn hiển thị Consent GDPR, tránh khó chịu cho người dùng
                        ignore(R.id.splashFragment)
                    }

                    /**
                     * [Không cần thiết] Config Fullscreen Ad Strategy
                     */
                    config {
                        fullscreen_timeout = 10.seconds.inWholeMilliseconds
                    }

                    /**
                     * [PHẢI_CÓ] Config Ads
                     */
                    default {
                        // Offline ads sử dụng khi không có mạng
//                        val offlineAds = OfflineAds(
//                            "Title ads",
//                            "Content Keego ads",
//                            4.0,
//                            icon = R.drawable.icon_test,
//                            thumbnail = R.drawable.icon_test,
//                            packageId = "com.keego.voicechanger.voiceeffects.soundeffects"
//                        )

                        interstitial = HakiInterstitial(
                            networks = mutableListOf(
                                AdmobInterstitial(context.getString(R.string.no1_interstitial_default)),
                                ApplovinInterstitial(),
                            ),
//                            fallback = OfflineInterstitial(offlineAds)
                            fallback = EmptyInterstitial()
                        )

                        banner = HakiBanner(
                            networks = mutableListOf(
                                AdmobBanner(context.getString(R.string.no1_banner_default)),
                                ApplovinBanner()
                            ),
//                            fallback = OfflineBanner(offlineAds)
                            fallback = EmptyBanner()
                        )

                        bannerCollapsible = HakiBannerCollapsible(
                            mutableListOf(
                                AdmobBannerCollapsible(context.getString(R.string.no1_banner_collapsible_default)),
                            ),
//                            fallback = OfflineBanner(ads = offlineAds)
                            fallback = EmptyBannerCollapsible()
                        )
                        bannerNative = HakiBannerNative(
                            mutableListOf(
                                AdmobBannerNative(defaultLayout = DefaultNative.SM1.layout().id, unitId = "")
                            ),
//                            fallback = OfflineBanner(ads = offlineAds)
                            fallback = EmptyBannerNative()
                        )
                        appOpen = HakiAppOpen(
                            networks = mutableListOf(
                                AdmobAppOpen(context.getString(R.string.no1_app_open_default)),
                                ApplovinAppOpen()
                            ),
                        )

                        rewarded = HakiRewarded(
                            mutableListOf(
                                AdmobRewarded(context.getString(R.string.no1_rewarded_default)),
                                ApplovinRewarded(),
                            )
                        )

                        native = HakiNative(
                            mutableListOf(
                                AdmobNative(context.getString(R.string.no1_native_default)),
                                ApplovinNative()
                            ),
//                            fallback = OfflineNative(ads = offlineAds)
                            fallback = EmptyNative()
                        )
                        rewardedInterstitial = HakiRewardedInterstitial(
                            mutableListOf(
                                AdmobRewardedInterstitial(context.getString(R.string.no1_rewarded_interstitial_default))
                            ),
                            fallback = EmptyRewardedInterstitial()
                        )
                    }
                }

        Haki.start(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}