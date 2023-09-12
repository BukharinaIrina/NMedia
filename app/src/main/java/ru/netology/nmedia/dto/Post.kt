package ru.netology.nmedia.dto

import ru.netology.nmedia.util.Attachment

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val attachment: Attachment? = null
) {
    val shares: Long = 0
    val views: Long = 0
    val video: String? = null
}
