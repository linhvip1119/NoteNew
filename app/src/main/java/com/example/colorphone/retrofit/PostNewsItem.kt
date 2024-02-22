package com.example.colorphone.retrofit


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PostNewsItem(
    @SerializedName("body")
    @Expose
    var body: String,
    @SerializedName("id")
    var id: Int,
    @Expose
    @SerializedName("title")
    var title: String,
    @Expose
    @SerializedName("userId")
    var userId: Int
)