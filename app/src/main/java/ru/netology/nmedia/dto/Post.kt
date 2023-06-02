package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    var likes: Long = 999,
    var likedByMe: Boolean = false,
    var shares: Long = 363099998,
    var shareByMe: Boolean = false,
    var views: Long = 0
)
