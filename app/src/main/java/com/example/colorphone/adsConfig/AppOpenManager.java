package com.example.colorphone.adsConfig;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.colorphone.R;
import com.example.colorphone.model.RemoteConfigAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private String AD_UNIT_ID = "";
    private AppOpenAd appOpenAd = null;
    private static boolean isShowingAd = false;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private long loadTime = 0;
    private Context context;
    private Application myApplication;
    private Activity currentActivity;

    /**
     * Constructor
     */
    public AppOpenManager(Application application) {
        this.myApplication = application;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AD_UNIT_ID = application.getString(R.string.no1_app_open_default);
    }

    boolean check = false;

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    RemoteConfigAds remoteIdAppReturn = null;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        showAdIfAvailable();
    }

    public void fetchAd() {
        if (isAdAvailable()) {
            return;
        }
        if (AdsConstants.INSTANCE.getCanRequestAds() != null && AdsConstants.INSTANCE.getCanRequestAds()) {
            loadCallback =
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        /**
                         * Called when an app open ad has loaded.
                         *
                         * @param ad the loaded app open ad.
                         */
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            AppOpenManager.this.appOpenAd = ad;
                            AppOpenManager.this.loadTime = (new Date()).getTime();
                        }

                        /**
                         * Called when an app open ad has failed to load.
                         *
                         * @param loadAdError the error.
                         */
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            // Handle the error.
                        }
                    };
            AdRequest request = getAdRequest();
            try {
                if (remoteIdAppReturn != null) {
                    if (remoteIdAppReturn.isOn()) {
                        AppOpenAd.load(
                                myApplication, remoteIdAppReturn.getId(), request,
                                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
                    }
                } else {
                    AppOpenAd.load(
                            myApplication, AD_UNIT_ID, request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
                }
            } catch (Exception e) {
                AppOpenAd.load(
                        myApplication, AD_UNIT_ID, request,
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
            }
        }

    }

    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        remoteIdAppReturn = AdsConstants.INSTANCE.getMapRemoteConfigAds().get(PlacementAds.PLACEMENT_RETURN_APP);
        if (!isShowingAd && !AdsConstants.INSTANCE.isClickAds() && !AdsConstants.INSTANCE.isShowAdsInter() && remoteIdAppReturn != null && remoteIdAppReturn.isOn()) {

            if (!check) {
                if (appOpenAd != null) {
                    AdsConstants.INSTANCE.setShowOpenAds(true);
                    FullScreenContentCallback fullScreenContentCallback =
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Set the reference to null so isAdAvailable() returns false.
                                    AppOpenManager.this.appOpenAd = null;
                                    isShowingAd = false;
                                    AdsConstants.INSTANCE.setShowOpenAds(false);
                                    fetchAd();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    AdsConstants.INSTANCE.setShowOpenAds(false);
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    isShowingAd = true;
                                    AdsConstants.INSTANCE.setShowOpenAds(true);
                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }
                            };
                    appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                    appOpenAd.show(currentActivity);
                } else {
                    fetchAd();
                }
                check = true;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    check = false;
                }
            }, 3000);

        } else {
            AdsConstants.INSTANCE.setClickAds(false);
            fetchAd();
        }
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

}
