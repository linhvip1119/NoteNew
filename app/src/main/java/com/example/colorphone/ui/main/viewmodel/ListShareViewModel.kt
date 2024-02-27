package com.example.colorphone.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.colorphone.util.TypeView
import dagger.hilt.android.lifecycle.HiltViewModel

class ListShareViewModel : ViewModel() {

    val filterColorLiveData: MutableLiveData<String> = MutableLiveData()

    val changeViewListLiveData: MutableLiveData<TypeView> = MutableLiveData()

    val searchLiveData: MutableLiveData<String> = MutableLiveData()

    val sortLiveData: MutableLiveData<String> = MutableLiveData()

    fun setFilterColor(color: String) {
        filterColorLiveData.postValue(color)
    }

    fun setChangeViewList(view: TypeView) {
        changeViewListLiveData.postValue(view)
    }

    fun setSearchText(text: String) {
        searchLiveData.postValue(text)
    }

    fun setSortText(text: String) {
        sortLiveData.postValue(text)
    }
}