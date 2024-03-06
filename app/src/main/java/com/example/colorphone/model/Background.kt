package com.example.colorphone.model

class Background(
    val id: Int? = null,
    val category: Int,
    val url: String,
    val thumb: String,
    @Transient var isSelected: Boolean = false
)