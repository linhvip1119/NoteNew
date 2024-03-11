package com.example.colorphone.model

class Background(
    val category: Int,
    val url: Int,
    val name: String,
    @Transient var isSelected: Boolean = false
)