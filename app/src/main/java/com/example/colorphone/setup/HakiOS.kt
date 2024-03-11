//package com.example.colorphone.setup
//
//import android.app.Application
//import android.content.Context
//import androidx.startup.Initializer
//import dev.keego.haki.Haki
//
//class HakiOS : Initializer<Unit> {
//    override fun create(context: Context) {
//
//        Haki.config(
//            context as Application,
//            version = BuildConfig.VERSION_NAME,
//            debug = BuildConfig.DEBUG,
//            enable = MutableStateFlow(true)
//        )
//                .plugin {
//                    register(TimberPlugin) // for logging, must have
//                    /**
//                     * 2 plugin ở dưới thì không cần thiết, chỉ cần khi có yêu cầu
//                     */
//                    register(BillingV6Plugin)
//                    register(
//                        AdjustPlugin(
//                            "your_adjust_token",
//                            if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX
//                            else AdjustConfig.ENVIRONMENT_PRODUCTION
//                        )
//                    )
//                }
//                .ads {
//
//                    /**
//                     * [Không cần thiết] Config App Open
//                     */
//                    appOpenManager.apply {
//                        // Bỏ qua các activity không muốn hiển thị quảng cáo App Open khi quay lại app
//                        ignore(SplashActivity::class.java)
//                        ignore(R.id.fragment_home)
//                    }
//                    /**
//                     * [Không cần thiết] Config Consent
//                     */
//                    consent.apply {
//                        // Bỏ qua các activity không muốn hiển thị Consent GDPR, tránh khó chịu cho người dùng
//                        ignore(SplashActivity::class.java)
//                        ignore(R.id.navigation_home)
//                    }
//
//                    /**
//                     * [Không cần thiết] Config Fullscreen Ad Strategy
//                     */
//                    config {
//                        fullscreen_timeout = 10.seconds.inWholeMilliseconds
//                    }
//
//                    /**
//                     * [PHẢI_CÓ] Config Ads
//                     */
//                    default {
//                        // Offline ads sử dụng khi không có mạng
//                        val offlineAd = OfflineAds(
//                            "Offline ad title",
//                            "Offline ad content",
//                            icon = R.mipmap.ic_launcher,
//                            thumbnail = R.mipmap.ic_launcher,
//                            packageId = "com.keego.voicechanger.voiceeffects.soundeffects"
//                        )
//
//                        interstitial = HakiInterstitial(
//                            networks = mutableListOf(
//                                AdmobInterstitial(context.getString(R.string.ad_interstitial_general)),  // must have
//                                ApplovinInterstitial(), // should have
//                                GamInterstitial() // optional
//                            ),
//                            fallback = OfflineInterstitial(offlineAd)
//                        )
//                        appOpen = HakiAppOpen(
//                            networks = mutableListOf(
//                                AdmobAppOpen(),
//                                ApplovinAppOpen()
//                            )
//                        )
//                        rewarded = HakiRewarded(
//                            networks = mutableListOf(
//                                AdmobRewarded(),
//                                ApplovinRewarded(),
//                            ),
//                            fallback = OfflineReward(offlineAd)
//                        )
//                        rewardedInterstitial = HakiRewardedInterstitial(
//                            networks = mutableListOf(
//                                AdmobRewardedInterstitial()
//                            ),
//                            fallback = OfflineRewardInterstitial(offlineAd)
//                        )
//                        banner = HakiBanner(
//                            networks = mutableListOf(
//                                AdmobBanner(),
//                                ApplovinBanner(),
//                                GamBanner()
//                            ),
//                            fallback = OfflineBanner(offlineAd)
//                        )
//
//                        bannerCollapsible = HakiBannerCollapsible(
//                            networks = mutableListOf(
//                                AdmobBannerCollapsible(),
//                                ApplovinBannerCollapsible()
//                            ),
//                            fallback = OfflineBanner(offlineAd)
//                        )
//                        bannerNative = HakiBannerNative(
//                            networks = mutableListOf(
//                                AdmobBannerNative(defaultLayout = DefaultNative.SM1.layoutId)
//                            ),
//                            fallback = OfflineBanner(offlineAd)
//                        )
//                        native = HakiNative(
//                            networks = mutableListOf(
//                                AdmobNative(),
//                                ApplovinNative()
//                            ),
//                            fallback = OfflineBanner(offlineAd)
//                        )
//                    }
//                }
//
//        Haki.start(context)
//    }
//
//    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
//}