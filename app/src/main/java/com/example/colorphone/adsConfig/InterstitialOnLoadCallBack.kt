package com.example.colorphone.adsConfig

interface InterstitialOnLoadCallBack {
    fun onAdFailedToLoad(adError:String){}
    fun onAdLoaded(){}
    fun onPreloaded(){}
}