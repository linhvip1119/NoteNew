package com.example.colorphone.room.mapper

import com.example.colorphone.model.NoteType
import com.example.colorphone.room.entities.NoteTypeEntity
import com.example.colorphone.util.EntityMapper
import javax.inject.Inject

class NoteTypeMapper @Inject constructor() : EntityMapper<NoteTypeEntity, NoteType> {

    override fun mapFromEntity(entity: NoteTypeEntity): NoteType {
        entity.apply {
            return NoteType(ids = id, listColor = listColor)
        }
    }

    override fun mapToEntity(domainModel: NoteType): NoteTypeEntity {
        domainModel.apply {
            return NoteTypeEntity(
                id = ids,
                listColor = listColor
            )
        }
    }

    override fun mapFromListEntity(list: List<NoteTypeEntity>): List<NoteType> {
        return list.map {
            mapFromEntity(it)
        }
    }

    override fun mapFromListLocal(list: List<NoteType>): List<NoteTypeEntity> {
        return list.map {
            mapToEntity(it)
        }
    }
}