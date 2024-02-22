package com.example.colorphone.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckList(var body: String, var checked: Boolean = false, var isUpdate: Boolean, var token: String) : Parcelable