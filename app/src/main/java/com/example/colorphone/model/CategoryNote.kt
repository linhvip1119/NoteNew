package com.example.colorphone.model

class CategoryNote(
    val id: Int? = null,
    val name: String,
    @Transient var isSelected : Boolean = false
)