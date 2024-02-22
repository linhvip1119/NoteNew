package com.example.colorphone.util

sealed class DataState<out R> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error<out T>(val ex: T) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}