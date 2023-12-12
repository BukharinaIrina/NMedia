package ru.netology.nmedia.dto

import java.time.OffsetDateTime

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: OffsetDateTime,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val hidden: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem {
    val shares: Long = 0
    val views: Long = 0
    val video: String? = null
}

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem

data class Attachment(
    val url: String,
    val type: TypeAttachment,
)

enum class TypeAttachment {
    IMAGE
}
