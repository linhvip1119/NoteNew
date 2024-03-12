package com.example.colorphone.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RequestPinWidget {
    private val _toolWidgetSuccess = MutableSharedFlow<Boolean>()
    val toolWidgetSuccess = _toolWidgetSuccess.asSharedFlow()

    private val _noteWidgetSuccess = MutableSharedFlow<Boolean>()
    val noteWidgetSuccess = _noteWidgetSuccess

    private val _updateWidgetSuccess = MutableSharedFlow<Boolean>()
    val updateWidgetSuccess = _updateWidgetSuccess

    suspend fun publishEventTools(state: Boolean) {
        _toolWidgetSuccess.emit(state)
    }

    suspend fun publishEventNote(state: Boolean) {
        _noteWidgetSuccess.emit(state)
    }

    suspend fun publishUpdateNote(state: Boolean){
        _updateWidgetSuccess.emit(state)
    }

}