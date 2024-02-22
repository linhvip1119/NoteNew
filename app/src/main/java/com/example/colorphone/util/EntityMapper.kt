package com.example.colorphone.util

interface EntityMapper<E,M> {
    fun mapFromEntity(entity :E):M
    fun mapToEntity(domainModel :M): E
    fun mapFromListEntity(list: List<E>):List<M>
    fun mapFromListLocal(list: List<M>): List<E>? = null
}