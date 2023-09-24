package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val attachment: Attachment? = null,
    val hidden: Boolean = false,
) {
    val shares: Long = 0
    val views: Long = 0
    val video: String? = null
}
data class Attachment(
    val url: String,
    val description: String,
    val type: TypeAttachment = TypeAttachment.IMAGE
)
enum class TypeAttachment {
    IMAGE
}
