package com.example.colorphone.ui.main.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.colorphone.R
import com.example.colorphone.model.Background
import com.example.colorphone.model.NoteModel
import com.example.colorphone.repository.MailRepository
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.util.Const
import com.example.colorphone.util.DataState
import com.example.colorphone.util.SortType
import com.example.colorphone.util.ext.getCurrentTimeToLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class TextNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val mailNoteRepository: MailRepository,
    private val app: Application
) :
    AndroidViewModel(app) {

    val textNoteLiveData: MutableLiveData<List<NoteModel>?> = MutableLiveData()

    val recycleBinLiveData: MutableLiveData<List<NoteModel>> = MutableLiveData()

    val archiveLiveData: MutableLiveData<List<NoteModel>> = MutableLiveData()

    val firstItemLD: MutableLiveData<NoteModel> = MutableLiveData()

    val itemWithIdsLD: MutableLiveData<NoteModel> = MutableLiveData()

    fun getListTextNote(key: String, typeSort: String? = SortType.MODIFIED_TIME.name) {
        var listNote = mutableListOf<NoteModel>()
        viewModelScope.launch() {
            val job = viewModelScope.launch {
                noteRepository.getAllNote(key).collect {
                    when (it) {
                        is DataState.Success -> {
                            Log.d("TAGBNVNVN", "x1mmm")
                            listNote = it.data.setSortData(typeSort)
                                    .filter { it.isArchive == false && it.isDelete == false }
                                    .toMutableList()
//                            textNoteLiveData.postValue(
//                                it.data.setSortData(typeSort)
//                                    .filter { it.isArchive == false && it.isDelete == false })
                        }

                        else -> {}
                    }
                }
            }
            job.join()
            textNoteLiveData.value = listNote
        }
    }

    fun getNoteWithIds(ids: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getNoteWithIds(ids).collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DataState.Success -> {
                            itemWithIdsLD.postValue(
                                it.data.first()
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun getAllData(call: (List<NoteModel>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getAllData().collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DataState.Success -> {
                            call.invoke(it.data)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun getFirstData() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getAllData().collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DataState.Success -> {
                            firstItemLD.postValue(
                                it.data.setSortData()
                                        .first { it.isArchive == false && it.isDelete == false }
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun getListRecycleArchive(
        isListArchive: Boolean,
        typeSort: String? = SortType.MODIFIED_TIME.name
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getListRecycleArchive(isListArchive).collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DataState.Success -> {
                            if (isListArchive) {
                                archiveLiveData.postValue(
                                    it.data.setSortData(typeSort)
                                )
                            } else {
                                recycleBinLiveData.postValue(
                                    it.data.setSortData(typeSort)
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun sortModifiedTime() {
        val listSort =
            textNoteLiveData.value?.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.modifiedTime })
        textNoteLiveData.postValue(listSort)
    }

    fun sortCreateTime() {
        val listSort =
            textNoteLiveData.value?.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.dateCreateNote })
        textNoteLiveData.postValue(listSort)
    }

    fun sortReminderTime() {
        val listSort =
            textNoteLiveData.value?.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.dateReminder })
        textNoteLiveData.postValue(listSort)
    }

    fun sortByColor() {
        val listSort =
            textNoteLiveData.value?.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenBy { it.typeColor })
        textNoteLiveData.postValue(listSort)
    }

    private fun List<NoteModel>.setSortData(typeSort: String? = SortType.MODIFIED_TIME.name): List<NoteModel> {
        return when (typeSort) {
            SortType.MODIFIED_TIME.name -> this.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.modifiedTime })
            SortType.COLOR.name -> this.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenBy { it.typeColor })
            SortType.CREATE_TIME.name -> this.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.dateCreateNote })
            else -> this.sortedWith(compareByDescending<NoteModel> { it.datePinned }.thenByDescending { it.dateReminder })
        }
    }

    fun addNote(note: NoteModel, newId: (Int) -> Unit) {
        if (note.title?.isNotEmpty() == true || note.content?.isNotEmpty() == true || note.listCheckList?.isNotEmpty() == true) {
            viewModelScope.launch {
                noteRepository.addNote(note) {
                    newId.invoke(it)
                }
            }
        }
    }


    suspend fun delete(id: Int, onComplete: () -> Unit) = withContext(Dispatchers.IO) {
        noteRepository.deleteNote(id)
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }

    suspend fun archiveNote(model: NoteModel) = withContext(Dispatchers.IO) {
        model.isUpdate = "0"
        noteRepository.updateNote(model.apply {
            isArchive = true
            modifiedTime = getCurrentTimeToLong()
        })
    }

    suspend fun updateColorNote(model: NoteModel, colorStr: String) = withContext(Dispatchers.IO) {
        model.isUpdate = "0"
        noteRepository.updateNote(model.apply {
            typeColor = colorStr
            modifiedTime = getCurrentTimeToLong()
        })
    }

    suspend fun unArchiveNote(model: NoteModel) = withContext(Dispatchers.IO) {
        model.isUpdate = "-1"
        noteRepository.updateNote(model.apply {
            isArchive = false
            modifiedTime = getCurrentTimeToLong()
        })
    }

    suspend fun deleteLocalNote(model: NoteModel) = withContext(Dispatchers.IO) {
        model.isUpdate = "0"
        noteRepository.updateNote(model.apply {
            isDelete = true
            modifiedTime = getCurrentTimeToLong()
        })
    }

    suspend fun unDeleteLocalNote(model: NoteModel) = withContext(Dispatchers.IO) {
        model.isUpdate = "-1"
        noteRepository.updateNote(model.apply {
            isDelete = false
            modifiedTime = getCurrentTimeToLong()
        })
    }


    fun updateNote(note: NoteModel, onComplete: () -> Unit) {
        if (note.title?.isNotEmpty() == true || note.content?.isNotEmpty() == true || note.listCheckList?.isNotEmpty() == true) {
            CoroutineScope(Dispatchers.IO).launch {
                noteRepository.updateNote(note)
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                note.ids?.let { noteRepository.deleteNote(it) }
                withContext(Dispatchers.Main) {
                    Log.d("TAVCNMMV", "v3")
                    onComplete()
                }
            }
        }
    }

    fun sendMail(otp: String, mail: String) {
        viewModelScope.launch {
            mailNoteRepository.sendmail(otp, mail)
        }
    }

    val backgroundLiveData: MutableLiveData<List<Background>> = MutableLiveData()

    fun getBackgroundNote() {
        val l = arrayListOf<Background>()
        l.add(Background(Const.COLOR, R.drawable.color01, "color01"))
        l.add(Background(Const.COLOR, R.drawable.color02, "color02"))
        l.add(Background(Const.COLOR, R.drawable.color03, "color03"))
        l.add(Background(Const.COLOR, R.drawable.color04, "color04"))
        l.add(Background(Const.COLOR, R.drawable.color05, "color05"))
        l.add(Background(Const.COLOR, R.drawable.color06, "color06"))
        l.add(Background(Const.COLOR, R.drawable.color07, "color07"))
        l.add(Background(Const.COLOR, R.drawable.color08, "color08"))
        l.add(Background(Const.COLOR, R.drawable.color09, "color09"))
        l.add(Background(Const.COLOR, R.drawable.color10, "color10"))
        l.add(Background(Const.COLOR, R.drawable.color11, "color11"))
        l.add(Background(Const.COLOR, R.drawable.color12, "color12"))

        l.add(Background(Const.PAPER, R.drawable.page01, "paper01"))
        l.add(Background(Const.PAPER, R.drawable.page02, "paper02"))
        l.add(Background(Const.PAPER, R.drawable.page03, "paper03"))
        l.add(Background(Const.PAPER, R.drawable.page04, "paper04"))
        l.add(Background(Const.PAPER, R.drawable.page05, "paper05"))
        l.add(Background(Const.PAPER, R.drawable.page06, "paper06"))
        l.add(Background(Const.PAPER, R.drawable.page07, "paper07"))
        l.add(Background(Const.PAPER, R.drawable.page08, "paper08"))

        l.add(Background(Const.CUTE, R.drawable.cute01, "cute01"))
        l.add(Background(Const.CUTE, R.drawable.cute02, "cute02"))
        l.add(Background(Const.CUTE, R.drawable.cute03, "cute03"))
        l.add(Background(Const.CUTE, R.drawable.cute04, "cute04"))
        l.add(Background(Const.CUTE, R.drawable.cute05, "cute05"))
        l.add(Background(Const.CUTE, R.drawable.cute06, "cute06"))
        l.add(Background(Const.CUTE, R.drawable.cute07, "cute07"))
        l.add(Background(Const.CUTE, R.drawable.cute08, "cute08"))

        l.add(Background(Const.DARK, R.drawable.dark01, "dark01"))
        l.add(Background(Const.DARK, R.drawable.dark02, "dark02"))
        l.add(Background(Const.DARK, R.drawable.dark03, "dark03"))
        l.add(Background(Const.DARK, R.drawable.dark04, "dark04"))
        l.add(Background(Const.DARK, R.drawable.dark05, "dark05"))
        l.add(Background(Const.DARK, R.drawable.dark06, "dark06"))
        l.add(Background(Const.DARK, R.drawable.dark07, "dark07"))
        l.add(Background(Const.DARK, R.drawable.dark08, "dark08"))

        backgroundLiveData.value = l

    }
}