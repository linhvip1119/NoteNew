package com.example.colorphone.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

class ListShareViewModel : ViewModel() {

    val filterColorLiveData: MutableLiveData<String> = MutableLiveData()

    fun setFilterColor(color: String) {
        filterColorLiveData.postValue(color)
    }
}