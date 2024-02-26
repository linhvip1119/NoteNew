package com.example.colorphone.ui.main.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.colorphone.model.NoteModel
import com.example.colorphone.repository.MailRepository
import com.example.colorphone.repository.NoteRepository
import com.example.colorphone.util.DataState
import com.example.colorphone.util.SortType
import com.example.colorphone.util.getCurrentTimeToLong
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
}