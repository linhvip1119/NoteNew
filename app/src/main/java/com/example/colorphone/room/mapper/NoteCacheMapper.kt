package com.example.colorphone.room.mapper

import com.example.colorphone.model.NoteModel
import com.example.colorphone.room.entities.NoteInfoEntity
import com.example.colorphone.util.EntityMapper
import javax.inject.Inject

class NoteCacheMapper @Inject constructor() : EntityMapper<NoteInfoEntity, NoteModel> {

    override fun mapFromEntity(entity: NoteInfoEntity): NoteModel {
        entity.apply {
            return NoteModel(
                ids!!,
                token,
                isUpdate,
                bodyNote,
                titleNote,
                typeItem,
                listCheckList,
                dateNote ?: 0,
                isPinned,
                datePinned,
                typeColor,
                modifiedTime,
                isArchive,
                isDelete,
                dateReminder,
                typeRepeat,
                valueRepeat,
                isAlarm,
                isLock
            )
        }
    }

    override fun mapToEntity(domainModel: NoteModel): NoteInfoEntity {
        domainModel.apply {
            return NoteInfoEntity(
                ids,
                token,
                isUpdate,
                content,
                title,
                typeItem,
                listCheckList,
                dateCreateNote,
                isPinned,
                datePinned,
                typeColor,
                isArchive,
                modifiedTime,
                isDelete, dateReminder, typeRepeat, repeatValue, isAlarm, isLock
            )
        }
    }

    override fun mapFromListEntity(list: List<NoteInfoEntity>): List<NoteModel> {
        return list.map {
            mapFromEntity(it)
        }
    }

    override fun mapFromListLocal(list: List<NoteModel>): List<NoteInfoEntity> {
        return list.map {
            mapToEntity(it)
        }
    }
}