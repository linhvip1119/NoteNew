package com.example.colorphone.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RequestPinWidget {
    private val _toolWidgetSuccess = MutableSharedFlow<Boolean>()
    val toolWidgetSuccess = _toolWidgetSuccess.asSharedFlow()

    suspend fun publishEventTools(state: Boolean) {
        _toolWidgetSuccess.emit(state)
    }

}