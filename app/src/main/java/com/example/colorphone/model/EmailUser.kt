package com.example.colorphone.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmailUser(
    var name: String? = null,
    var email: String? = null,
    var avatar: String? = null
) : Parcelable