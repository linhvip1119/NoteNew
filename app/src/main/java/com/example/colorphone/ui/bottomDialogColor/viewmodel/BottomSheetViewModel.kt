package com.example.colorphone.ui.bottomDialogColor.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorphone.R
import com.example.colorphone.model.Background
import com.example.colorphone.model.CategoryNote
import com.example.colorphone.model.LanguageModel
import com.example.colorphone.model.NoteType
import com.example.colorphone.repository.NoteTypeRepository
import com.example.colorphone.util.Const
import com.example.colorphone.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class BottomSheetViewModel @Inject constructor(private val noteRepository: NoteTypeRepository) :
    ViewModel() {

    val colorLD: MutableLiveData<NoteType> = MutableLiveData()

    val languageModelLiveData: MutableLiveData<List<LanguageModel>> = MutableLiveData()

    fun getColorType() {
        viewModelScope.launch {
            noteRepository.getAllType().collect {
                withContext(Dispatchers.IO) {
                    when (it) {
                        is DataState.Success -> {
                            colorLD.postValue(it.data.first())
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun addType(type: NoteType) {
        viewModelScope.launch {
            noteRepository.addType(type)
        }
    }

    fun updateType(type: NoteType?) {
        viewModelScope.launch {
            type?.let { noteRepository.updateType(it) }
        }
    }

    fun Context.getListLanguage() {
        val list = arrayListOf(
            LanguageModel(getString(R.string.englishKey), getString(R.string.english)),
            LanguageModel(getString(R.string.spanishKey), getString(R.string.espanol)),
            LanguageModel(getString(R.string.frenchKey), getString(R.string.francais)),
            LanguageModel(getString(R.string.koreanKey), getString(R.string.koreanText)),
            LanguageModel(getString(R.string.japanKey), getString(R.string.japanText)),
            LanguageModel(getString(R.string.portugueseKey), getString(R.string.portugueseText)),
            LanguageModel(getString(R.string.germanKey), getString(R.string.germanText)),
            LanguageModel(getString(R.string.turkishKey), getString(R.string.turkishText)),
            LanguageModel(getString(R.string.indoKey), getString(R.string.indoText)),
            LanguageModel(getString(R.string.thaiKey), getString(R.string.thaiText)),
        )
        languageModelLiveData.postValue(list)
    }

    fun getCategoryBg(context: Context, call: (ArrayList<CategoryNote>) -> Unit) {
        context.apply {
            val list = arrayListOf(
                CategoryNote(Const.COLOR, getString(R.string.color), isSelected = true),
                CategoryNote(Const.PAPER, getString(R.string.paper)),
                CategoryNote(Const.CUTE, getString(R.string.cute)),
                CategoryNote(Const.DARK, getString(R.string.dark))
            )
            call(list)
        }
    }
}