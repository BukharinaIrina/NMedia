package ru.netology.nmedia.service.notifications

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)