package com.example.colorphone.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteType(
    @SerializedName("ids") var ids: Int? = null,
    @SerializedName("listColor") var listColor: ArrayList<ColorItem>? = arrayListOf()
) : Parcelable

@Parcelize
data class ColorItem(
    var tittle: String,
    var color: String,
    var isSelected: Boolean? = false,
) : Parcelable