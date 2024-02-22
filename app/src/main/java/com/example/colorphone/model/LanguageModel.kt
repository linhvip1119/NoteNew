package com.example.colorphone.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LanguageModel(
    var name: String,
    var text: String,
) : Parcelable