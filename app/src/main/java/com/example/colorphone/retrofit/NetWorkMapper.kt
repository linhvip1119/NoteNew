package com.example.colorphone.retrofit

import com.example.colorphone.model.Post
import com.example.colorphone.util.EntityMapper
import javax.inject.Inject

class NetWorkMapper @Inject constructor() : EntityMapper<PostNewsItem, Post> {
    override fun mapFromEntity(entity: PostNewsItem): Post {
        return Post(
            entity.id,
            body = entity.body,
            title = entity.title,
            id = entity.id,
            userId = entity.userId
        )
    }

    override fun mapToEntity(domainModel: Post): PostNewsItem {
        return PostNewsItem(
            body = domainModel.body,
            id = domainModel.id,
            title = domainModel.title,
            userId = domainModel.userId
        )
    }

    override fun mapFromListEntity(list: List<PostNewsItem>): List<Post> {
        return list.map {
            mapFromEntity(it)
        }
    }
}